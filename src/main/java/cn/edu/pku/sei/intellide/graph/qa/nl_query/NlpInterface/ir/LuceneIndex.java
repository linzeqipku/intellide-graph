package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.ir;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config.Config;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.ExtractModel;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.Graph;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.Vertex;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphSchema;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class LuceneIndex {

    private static QueryParser qp = new QueryParser("attr_val", new EnglishAnalyzer());
    private static IndexSearcher indexSearcher = null;
    private static LuceneIndex single = null;
    public static String dataDirPath;
    public void index() throws IOException {

        Directory dir = FSDirectory.open(Paths.get(dataDirPath+"/index"));
        Analyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);


        Graph graph = ExtractModel.getSingle().graph;
        GraphSchema graphSchema = ExtractModel.getSingle().graphSchema;
        int cnt = 0;
        for (Vertex vertex : graph.getAllVertexes()){
            cnt++;
            //if (cnt % 1000 == 0) System.out.println(cnt);
            GraphDatabaseService db = ExtractModel.getSingle().db;
            try (Transaction tx = db.beginTx()) {
                Node node = db.getNodeById(vertex.id);
                for (String attrTypeName : graphSchema.vertexTypes.get(vertex.labels).attrs.keySet()) {
                    Object obj = node.getAllProperties().get(attrTypeName);
                    if (!(obj instanceof String)) continue;
                    //graphSchema.vertexTypes.get(vertex.labels), attrTypeName, attrValue
                    Document document = new Document();
                    document.add(new StoredField("id", "" + vertex.id));
                    document.add(new StoredField("vertex_type", vertex.labels));
                    document.add(new StoredField("attr_type", attrTypeName));
                    document.add(new TextField("attr_val", (String)obj, Field.Store.YES));
                    //if (cnt % 10000 == 0) System.out.println(vertex.labels+" " + attrTypeName + " " + (String)obj);
                    writer.addDocument(document);
                }
                tx.success();
            }
        }
        writer.close();

    }

    public static List<LuceneSearchResult> query(String q) {

        List<LuceneSearchResult> r = new ArrayList<>();

        if (indexSearcher == null) {
            IndexReader reader = null;
            try {
                reader = DirectoryReader.open(FSDirectory.open(Paths.get(dataDirPath+"/index")));
            } catch (IOException e) {

                e.printStackTrace();
            }
            indexSearcher = new IndexSearcher(reader);
        }


        if (q.trim().length() == 0)
            return r;

        Query query = null;
        try {
            query = qp.parse(q);
        } catch (ParseException e) {
            return r;
        }
        TopDocs topDocs = null;
        try {
            topDocs = indexSearcher.search(query,10);
        } catch (IOException e) {

            e.printStackTrace();
        }
        //System.out.println(topDocs.scoreDocs.length);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = null;
            try {
                document = indexSearcher.doc(scoreDoc.doc);
            } catch (IOException e) {

                e.printStackTrace();
            }
//            LuceneSearchResult result = new LuceneSearchResult(Long.parseLong(document.get("id")),
//                    document.get("type"), document.get("title"), document.get("org_content"),
//                    new Double(scoreDoc.score).doubleValue(), document.get("node_set"));
//            r.add(result);
            LuceneSearchResult result = new LuceneSearchResult(Long.parseLong(document.get("id")) , document.get("vertex_type") , document.get("attr_type"), document.get("attr_val"));
            //System.out.println(document.get("id") + document.get("vertex_type") + document.get("attr_type") + document.get("attr_val"));
            r.add(result);
        }
        return r;
    }

    public static void main(String args[]) throws IOException {
        LuceneIndex  LI = new LuceneIndex();
        LI.index();
        //LI.query("Bad instructions for setting up JavaCC");
    }
}

