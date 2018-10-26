package cn.edu.pku.sei.intellide.graph.qa.doc_search;

import cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector.CodeMentionDetector;
import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.extraction.docx_to_neo4j.DocxGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.StackOverflowGraphBuilder;
import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jNode;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.lucene.store.SimpleFSDirectory;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DocSearch {

    private GraphDatabaseService db;
    private String indexDirPath;
    private CodeSearch codeSearch;
    private final String ID_FIELD = "id";
    private final String CONTENT_FIELD = "content";
    private DirectoryReader ireader;
    private IndexSearcher isearcher;
    private QueryParser parser;

    public DocSearch(GraphDatabaseService db, String indexDirPath, CodeSearch codeSearch){
        this.db=db;
        this.indexDirPath=indexDirPath;
        this.codeSearch=codeSearch;
    }

    private void createIndex() throws IOException {
        if (new File(indexDirPath).exists())
            return;
        //System.out.println("Creating Doc Search Index ...");
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new SimpleFSDirectory(new File(indexDirPath).toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        try (Transaction tx=db.beginTx()){
            ResourceIterator<Node> nodes=db.getAllNodes().iterator();
            while (nodes.hasNext()){
                Node node=nodes.next();
                String indexStr=getIndexStr(node);
                if (indexStr==null)
                    continue;
                Document document=new Document();
                document.add(new StringField(ID_FIELD, "" + node.getId(), Field.Store.YES));
                document.add(new TextField(CONTENT_FIELD, indexStr, Field.Store.NO));
                iwriter.addDocument(document);
            }
            tx.success();
        }
        iwriter.close();
        //System.out.println("Doc Search Index Created.");
    }

    public List<Neo4jNode> search(String queryString,String project) throws IOException, ParseException {
        createIndex();
        List<Neo4jNode> codeNodes=codeSearch.searchBaseNode(queryString).getNodes();
        Set<Long> nodeSet=new HashSet<>();
        try (Transaction tx=db.beginTx()) {
            for (Neo4jNode codeNode:codeNodes){
                Node node=db.getNodeById(codeNode.getId());
                Iterator<Relationship> rels=node.getRelationships(CodeMentionDetector.CODE_MENTION,Direction.OUTGOING).iterator();
                Set<Long> tmpSet=new HashSet<>();
                while (rels.hasNext()){
                    Relationship rel=rels.next();
                    Node docNode=rel.getEndNode();
                    tmpSet.add(docNode.getId());
                }
                if (tmpSet.size()<=5)
                    nodeSet.addAll(tmpSet);
            }
            tx.success();
        }
        List<Neo4jNode> r=new ArrayList<>();
        Directory directory = new SimpleFSDirectory(new File(indexDirPath).toPath());
        Analyzer analyzer = new StandardAnalyzer();
        ireader = DirectoryReader.open(directory);
        isearcher = new IndexSearcher(ireader);
        parser = new QueryParser(CONTENT_FIELD, analyzer);
        Query query = parser.parse(StringUtils.join(CodeTokenizer.tokenization(queryString)," "));
        ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
        try (Transaction tx=db.beginTx()) {
            for (int i = 0; i < hits.length; i++) {
                Document doc = ireader.document(hits[i].doc);
                long id=Long.parseLong(doc.getField(ID_FIELD).stringValue());
                if (!nodeSet.contains(id))
                    continue;
                Node node=db.getNodeById(id);
                Map map = new HashMap<>();
                if(project.contains("chinese")){

                    map.put(DocxGraphBuilder.TITLE, getTitle(node));
                    map.put(DocxGraphBuilder.HTML, getHtml(node));
                    //System.out.println(getHtml(node));
                }
                else{
                    map.put(StackOverflowGraphBuilder.QUESTION_TITLE,getTitle(node));
                    map.put("html",getHtml(node));
                    //System.out.println(getHtml(node));
                }

                r.add(new Neo4jNode(Long.parseLong(doc.getField(ID_FIELD).stringValue()), node.getLabels().iterator().next().name(), map));
            }
            tx.success();
        }
        //System.out.println(""+r.size()+" documents raised.");
        try (Transaction tx=db.beginTx()) {
            for (int i = 0; i < hits.length; i++) {
                if (r.size()>=10)
                    return r;
                Document doc = ireader.document(hits[i].doc);
                long id=Long.parseLong(doc.getField(ID_FIELD).stringValue());
                if (nodeSet.contains(id))
                    continue;
                Node node=db.getNodeById(id);
                Map map = new HashMap<>();
                if(project.contains("chinese")){
                    //System.out.println(node.getLabels().toString());
                    map.put(DocxGraphBuilder.TITLE, getTitle(node));
                    map.put(DocxGraphBuilder.HTML, getHtml(node));
                    //System.out.println(getHtml(node));
                }
                else{
                    map.put(StackOverflowGraphBuilder.QUESTION_TITLE,getTitle(node));
                    map.put("html",getHtml(node));
                    //System.out.println(getHtml(node));
                }

                r.add(new Neo4jNode(Long.parseLong(doc.getField(ID_FIELD).stringValue()), node.getLabels().iterator().next().name(), map));
            }
            tx.success();
        }
        return r;
    }

    private String getIndexStr(Node node){
        if (node.hasLabel(DocxGraphBuilder.DOCX))
            return StringUtils.join(CodeTokenizer.tokenization((String) node.getProperty(DocxGraphBuilder.CONTENT))," ");
        //TODO
        if(node.hasLabel(StackOverflowGraphBuilder.QUESTION))
            return StringUtils.join(CodeTokenizer.tokenization((String) node.getProperty(StackOverflowGraphBuilder.QUESTION_BODY))," ");
        if(node.hasLabel(StackOverflowGraphBuilder.ANSWER))
            return StringUtils.join(CodeTokenizer.tokenization((String) node.getProperty(StackOverflowGraphBuilder.ANSWER_BODY))," ");

        return null;
    }

    private String getTitle(Node node){
        if (node.hasLabel(DocxGraphBuilder.DOCX)) {
            //System.out.println("ooooooo");
            String r="";
            r+=(String) node.getProperty(DocxGraphBuilder.TITLE);
            Iterator<Relationship> rels=node.getRelationships(DocxGraphBuilder.SUB_DOCX_ELEMENT, Direction.INCOMING).iterator();
            if (rels.hasNext()){
                Relationship rel=rels.next();
                r=getTitle(rel.getStartNode())+"/"+r;
            }
            //System.out.println(r);
            return r;
        }
        //TODO
        if(node.hasLabel(StackOverflowGraphBuilder.QUESTION)){
            String r ="";
            r=(String) node.getProperty(StackOverflowGraphBuilder.QUESTION_TITLE);
            /*Iterator<Relationship> rels = node.getRelationships(StackOverflowGraphBuilder.HAVE_ANSWER,Direction.INCOMING).iterator();
            if(rels.hasNext()){
                Relationship rel = rels.next();
                r = getTitle(rel.getStartNode())+"/"+r;
            }*/
            return r;
        }
        return null;
    }

    private String getHtml(Node node){
        if (node.hasLabel(DocxGraphBuilder.DOCX)) {
            //System.out.println("Dffdfffgg");
            String r="";
            r+=node.getProperty(DocxGraphBuilder.HTML);
            Iterator<Relationship> rels=node.getRelationships(DocxGraphBuilder.SUB_DOCX_ELEMENT, Direction.OUTGOING).iterator();
            Map<Integer,String> subNodes=new HashMap<>();
            while (rels.hasNext()){
                Relationship rel=rels.next();
                int num= (int) rel.getProperty(DocxGraphBuilder.SERIAL_NUMBER);
                subNodes.put(num,getHtml(rel.getEndNode()));
            }
            int i=0;
            while (subNodes.containsKey(i)){
                r+=subNodes.get(i);
                i++;
            }
            return r;
        }
        //TODO
        if(node.hasLabel(StackOverflowGraphBuilder.QUESTION) || node.hasLabel(StackOverflowGraphBuilder.ANSWER)){
            String r = "";
            //r+=node.getProperty(StackOverflowGraphBuilder.QUESTION_BODY);
            r+=node.getProperty(StackOverflowGraphBuilder.ANSWER_BODY);
            Iterator<Relationship> rels=node.getRelationships(StackOverflowGraphBuilder.HAVE_ANSWER,Direction.OUTGOING).iterator();
            while(rels.hasNext()){
                Relationship rel = rels.next();
                r += getHtml(rel.getEndNode());
            }
            return r;
        }
        return null;
    }

}
