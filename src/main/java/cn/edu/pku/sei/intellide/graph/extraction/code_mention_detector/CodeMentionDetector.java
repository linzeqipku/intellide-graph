package cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector;

import cn.edu.pku.sei.intellide.graph.extraction.docx_to_neo4j.DocxGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.git_to_neo4j.GitGraphBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeMentionDetector {

    public static final RelationshipType CODE_MENTION=RelationshipType.withName("codeMention");
    public static final RelationshipType ADD=RelationshipType.withName("add");
    public static final RelationshipType MODIFY=RelationshipType.withName("modify");
    public static final RelationshipType DELETE=RelationshipType.withName("delete");
    private GraphDatabaseService db = null;

    public static void process(String graphDir) throws IOException, ParseException {
        CodeMentionDetector codeMentionDetector = new CodeMentionDetector(graphDir);
        codeMentionDetector.detectCodeMentionInDocx();
        codeMentionDetector.detectCodeMentionInDiff();
        codeMentionDetector.db.shutdown();
    }

    private CodeMentionDetector(String graphDir) {
        db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
    }

    private void detectCodeMentionInDocx() throws IOException, ParseException {
        final String CONTENT_FIELD = "content";
        final String ID_FIELD = "id";
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        ResourceIterator<Node> docxNodes = null;
        try (Transaction tx = db.beginTx()) {
            docxNodes = db.findNodes(DocxGraphBuilder.DOCX);
            while (docxNodes.hasNext()) {
                Node docxNode = docxNodes.next();
                String content = (String) docxNode.getProperty(DocxGraphBuilder.CONTENT);
                content = content.replaceAll("\\W+", " ").toLowerCase();
                Document document = new Document();
                document.add(new StringField(ID_FIELD, "" + docxNode.getId(), Field.Store.YES));
                document.add(new TextField(CONTENT_FIELD, content, Field.Store.YES));
                iwriter.addDocument(document);
            }
            tx.success();
        }
        iwriter.close();
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser(CONTENT_FIELD, analyzer);
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> methodNodes = db.findNodes(JavaCodeGraphBuilder.METHOD);
            while (methodNodes.hasNext()) {
                Node methodNode = methodNodes.next();
                String name = (String) methodNode.getProperty(JavaCodeGraphBuilder.FULLNAME);
                String[] eles=name.substring(0, name.indexOf("(")).toLowerCase().split("\\.");
                String q = eles[eles.length-1]+" AND "+eles[eles.length-2];
                if (eles.length>2){
                    q+=" AND ( ";
                    for (int i=0;i<eles.length-2;i++)
                        q+=eles[i]+" ";
                    q+=")";
                }
                Query query = parser.parse(q);
                ScoreDoc[] hits = isearcher.search(query, 10000).scoreDocs;
                if (hits.length > 0 && hits.length<20) {
                    for (ScoreDoc hit:hits) {
                        Node docxNode = db.getNodeById(Long.parseLong(ireader.document(hit.doc).get(ID_FIELD)));
                        methodNode.createRelationshipTo(docxNode,CODE_MENTION);
                    }
                }
            }
            tx.success();
        }
    }

    private void detectCodeMentionInDiff(){
        Map<String,Node> classMap=new HashMap<>();
        Pattern pattern=Pattern.compile("(ADD|MODIFY|DELETE)\\s+(\\S+)\\s+to\\s+(\\S+)");
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> classNodes=db.findNodes(JavaCodeGraphBuilder.CLASS);
            while (classNodes.hasNext()){
                Node classNode=classNodes.next();
                String fullName = (String) classNode.getProperty(JavaCodeGraphBuilder.FULLNAME);
                String sig = fullName.replace('.','/')+".java";
                classMap.put(sig,classNode);
            }
            ResourceIterator<Node> commits=db.findNodes(GitGraphBuilder.COMMIT);
            while (commits.hasNext()){
                Node commit=commits.next();
                String diffSummary= (String) commit.getProperty(GitGraphBuilder.DIFF_SUMMARY);
                Matcher matcher=pattern.matcher(diffSummary);
                while (matcher.find()){
                    String relStr=matcher.group(1);
                    String srcPath=matcher.group(2);
                    String dstPath=matcher.group(3);
                    RelationshipType relType=relStr.equals("ADD")?ADD:relStr.equals("MODIFY")?MODIFY:DELETE;
                    for (String sig:classMap.keySet())
                        if (srcPath.contains(sig)||dstPath.contains(sig))
                            commit.createRelationshipTo(classMap.get(sig),relType);
                }
            }
            tx.success();
        }
    }

}
