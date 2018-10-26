package cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector;

import cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector.utils.CodeIndexes;
import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
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
import org.jsoup.Jsoup;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 建立代码实体和其它类型的实体之间的关联关系
 *     detectCodeMentionInFlossDocuments: 建立英文文档和代码之间的关联关系
 *     detectCodeMentionInDocx: 建立中文文档和代码之间的关联关系（文档里面提到了这个代码）
 *     detectCodeMentionInDiff: 建立commits和代码之间的关联关系（add, modify, delete）
 *
 * Preconditions: 已经运行过CodeTokenizer了。
 */

public class CodeMentionDetector {

    public static final RelationshipType CODE_MENTION=RelationshipType.withName("codeMention");
    public static final RelationshipType ADD=RelationshipType.withName("add");
    public static final RelationshipType MODIFY=RelationshipType.withName("modify");
    public static final RelationshipType DELETE=RelationshipType.withName("delete");
    private GraphDatabaseService db = null;

    public static void process(String graphDir) throws IOException, ParseException {
        CodeMentionDetector codeMentionDetector = new CodeMentionDetector(graphDir);
        codeMentionDetector.detectCodeMentionInFlossDocuments();
        codeMentionDetector.detectCodeMentionInDocx();
        codeMentionDetector.detectCodeMentionInDiff();
        codeMentionDetector.db.shutdown();
    }

    private CodeMentionDetector(String graphDir) {
        db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
    }

    private void detectCodeMentionInFlossDocuments() {
        CodeIndexes codeIndexes = new CodeIndexes(db);
        Set<Node> textNodes = new HashSet<>();
        try (Transaction tx = db.beginTx()) {
            for (Node node:db.getAllNodes()){
                if (!node.hasProperty(CodeTokenizer.IS_TEXT)||!(boolean)node.getProperty(CodeTokenizer.IS_TEXT))
                    continue;
                if (node.hasLabel(JavaCodeGraphBuilder.CLASS)||node.hasLabel(JavaCodeGraphBuilder.METHOD)||node.hasLabel(JavaCodeGraphBuilder.FIELD))
                    continue;
                textNodes.add(node);
            }
            //System.out.println(textNodes.size());
            for (Node srcNode : textNodes) {
                String text = (String) srcNode.getProperty(CodeTokenizer.TITLE);
                text += " " + srcNode.getProperty(CodeTokenizer.TEXT);
                String content = Jsoup.parse(text).text();
                Set<String> lexes = new HashSet<>();
                Collections.addAll(lexes, content.toLowerCase().split("\\W+"));
                Set<Node> resultNodes = new HashSet<>();
                //类/接口
                for (String typeShortName : codeIndexes.typeShortNameMap.keySet())
                    if (lexes.contains(typeShortName.toLowerCase()))
                        for (long id : codeIndexes.typeShortNameMap.get(typeShortName))
                            resultNodes.add(db.getNodeById(id));

                for (String methodShortName : codeIndexes.methodShortNameMap.keySet()) {
                    //后接小括号，不要构造函数
                    if (methodShortName.charAt(0) < 'a' || methodShortName.charAt(0) > 'z' || !(lexes.contains(methodShortName.toLowerCase()) && content.contains(methodShortName + "(")))
                        continue;
                    boolean flag = false;
                    //无歧义
                    if (codeIndexes.methodShortNameMap.get(methodShortName).size() == 1) {
                        for (long id : codeIndexes.methodShortNameMap.get(methodShortName))
                            resultNodes.add(db.getNodeById(id));
                        flag = true;
                    }
                    //主类在
                    for (long methodNodeId : codeIndexes.methodShortNameMap.get(methodShortName)) {
                        Node methodNode = db.getNodeById(methodNodeId);
                        if (resultNodes.contains(methodNode.getRelationships(JavaCodeGraphBuilder.HAVE_METHOD, Direction.INCOMING).iterator().next().getStartNode())) {
                            resultNodes.add(methodNode);
                            flag = true;
                        }
                    }
                    //歧义不多
                    if (!flag && codeIndexes.methodShortNameMap.get(methodShortName).size() <= 5)
                        for (long id : codeIndexes.methodShortNameMap.get(methodShortName))
                            resultNodes.add(db.getNodeById(id));
                }
                for (Node rNode : resultNodes)
                    srcNode.createRelationshipTo(rNode, CODE_MENTION);

            }
            tx.success();
        }
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
                String[] eles=name.substring(0, name.indexOf("(")).split("\\.");
                if (eles[eles.length-1].equals(eles[eles.length-2]))
                    continue;
                String q = eles[eles.length-1].toLowerCase()+" AND "+eles[eles.length-2].toLowerCase();
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
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> classNodes = db.findNodes(JavaCodeGraphBuilder.CLASS);
            while (classNodes.hasNext()) {
                Node classNode = classNodes.next();
                String name = (String) classNode.getProperty(JavaCodeGraphBuilder.NAME);
                String q = name.toLowerCase();
                Query query = parser.parse(q);
                ScoreDoc[] hits = isearcher.search(query, 10000).scoreDocs;
                if (hits.length > 0 && hits.length<20) {
                    for (ScoreDoc hit:hits) {
                        Node docxNode = db.getNodeById(Long.parseLong(ireader.document(hit.doc).get(ID_FIELD)));
                        classNode.createRelationshipTo(docxNode,CODE_MENTION);
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
    public static void main(String[] args) throws IOException, ParseException {
        CodeMentionDetector.process("F:\\graph-tsr4");
    }

}
