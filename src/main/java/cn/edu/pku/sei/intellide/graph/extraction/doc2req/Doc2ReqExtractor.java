package cn.edu.pku.sei.intellide.graph.extraction.doc2req;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.doc2req.utils.DocSectionInfo;
import cn.edu.pku.sei.intellide.graph.extraction.json.RequirementExtractor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
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

import java.io.IOException;
import java.util.*;

public class Doc2ReqExtractor extends KnowledgeExtractor {
    public static final Label FeatureSection = Label.label("FeatureSection");

    public static RelationshipType PARENT = RelationshipType.withName("parent");

    public static RelationshipType TARGET = RelationshipType.withName("target");

    // TODO
    private static final double threshold = 0.25;

    @Override
    public void extraction() {

    }

    /**
     *  这里需要算法描述
     */
    public void feature2AR() {
        try (Transaction tx = this.getDb().beginTx()) {
            // TODO : label, property
            for (ResourceIterator<Node> it = this.getDb().findNodes(FeatureSection, "level", 0); it.hasNext(); ) {
                Node docRoot = it.next();       // 每篇文档的根节点
                // 文件子树的先序遍历
                List<DocSectionInfo> docSections = getDocSections(docRoot);

                Set<Node> allRelatedSR = new HashSet<>();

                // 寻找文档中准确提及的需求
                for (DocSectionInfo docSection : docSections) {
                    docSection.findReqByBusinessno(this.getDb());
                    docSection.findReqByName(this.getDb());
                    docSection.addSR(this.getDb());
                    allRelatedSR.addAll(docSection.setSR);
                }

                Set<Node> potentialAR = getChildReqNode(allRelatedSR);
                searchARinDocSection(potentialAR, docSections);

                for (DocSectionInfo docSection : docSections) {
                    docSection.addRelationship(this.getDb(), 3);
                }

                tx.success();
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }


    public void requirement2SR() {

    }


    // 返回以node为根节点的子文件树的先序遍历
    public List<DocSectionInfo> getDocSections(Node node) {
        List<DocSectionInfo> result = new ArrayList<>();
        result.add(new DocSectionInfo(node));
        // TODO : relationshiptype
        for (Relationship edge: node.getRelationships(Direction.INCOMING, PARENT)) {
            Node child = edge.getStartNode();
            result.addAll(getDocSections(child));
        }
        return result;
    }


    // 获取需求的子需求
    public Set<Node> getChildReqNode(Set<Node> parentNodes) {
        Set<Node> result = new HashSet<>();
        for (Node pNode : parentNodes) {
            for (Relationship relationship : pNode.getRelationships(Direction.INCOMING, RequirementExtractor.PARENT)) {
                result.add(relationship.getStartNode());
            }
        }
        return result;
    }


    // 使用Lucene搜索
    public void searchARinDocSection(Set<Node> potentialAR, List<DocSectionInfo> docSections) throws IOException, ParseException {
        final String ID_FIELD = "id";
        final String CONTENT_FIELD = "content";
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();

        // 建立倒排索引表
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        for (int i = 0; i < docSections.size(); i++) {
            DocSectionInfo docSection = docSections.get(i);
            String content = docSection.content.replaceAll("\\W+", " ").toLowerCase();
            Document document = new Document();
            document.add(new IntField(ID_FIELD, i, Field.Store.YES));
            document.add(new TextField(CONTENT_FIELD, content, Field.Store.YES));
            indexWriter.addDocument(document);
        }
        indexWriter.close();

        // 检索AR
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        QueryParser parser = new QueryParser(CONTENT_FIELD, analyzer);
        for (Node arNode : potentialAR) {
            String q = (String) arNode.getProperty(RequirementExtractor.NAME);
            q = q.toLowerCase();
            Query query = parser.parse(q);
            ScoreDoc[] hits = indexSearcher.search(query, 5).scoreDocs;
            for (ScoreDoc hit : hits) {
                if (hit.score < threshold)
                    break;
                docSections.get(Integer.parseInt(directoryReader.document(hit.doc).get(ID_FIELD))).setAR.add(arNode);
            }
        }

    }


}
