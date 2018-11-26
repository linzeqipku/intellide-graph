package cn.edu.pku.sei.intellide.graph.extraction.doc_link;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.neo4j.graphdb.*;

import java.util.*;

/**
 * 根据标题匹配，把不同的中文文档实体关联起来
 *
 * @author: 王敏
 */
public class DocLinkExtractor extends KnowledgeExtractor {

    public static final Label DOCX = Label.label("Docx");
    public static final String TITLE = "title";

    public static final RelationshipType RELATEDOCXSEC = RelationshipType.withName("relatedDocxSec");

    @Override
    public void extraction() {
        List<Map<Long, Set<Long>>> nodepairs;
        this.createRealtionship(this.addRelationships());
        this.deleteRelationship();
    }

    public Map<Long, String> getNodeTitle(Label label) {
        Map<Long, String> titleMap = new LinkedHashMap<Long, String>();
        GraphDatabaseService db = this.getDb();

        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.findNodes(label);
            while (nodes.hasNext()) {
                Node node = nodes.next();
                long id = node.getId();
                String title = (String) node.getProperty(TITLE);
                if (!title.equals("")) {
                    titleMap.put(id, title);
                }
            }
            tx.success();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return titleMap;
    }

    public void createRealtionship(List<Map<Long, Set<Long>>> nodePair) {

        GraphDatabaseService db = this.getDb();

        try (Transaction tx = db.beginTx()) {

            int cnt = 0;
            for (Map<Long, Set<Long>> nodeMap : nodePair) {
                for (Map.Entry<Long, Set<Long>> entry : nodeMap.entrySet()) {
                    long id1 = entry.getKey();
                    Node node1 = db.getNodeById(id1);
                    Set<Long> nodeset = entry.getValue();
                    for (long id2 : nodeset) {
                        Node node2 = db.getNodeById(id2);
                        node1.createRelationshipTo(node2, RELATEDOCXSEC);
                        cnt++;
                        //if(cnt % 1000 == 0 ) System.out.println(cnt);
                    }
                }
            }
            tx.success();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void deleteRelationship() {
        GraphDatabaseService db = this.getDb();
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> docxNodes;
            docxNodes = db.findNodes(DOCX);
            while (docxNodes.hasNext()) {
                Node node = docxNodes.next();
                if (node.getDegree(RELATEDOCXSEC) > 10) {
                    Iterable<Relationship> relationships = node.getRelationships(RELATEDOCXSEC);
                    while (relationships.iterator().hasNext()) {
                        Relationship relationship = relationships.iterator().next();
                        relationship.delete();
                    }
                }

            }
            tx.success();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int isMatch(String text1, String text2) {
        if (text1.equals(text2)) {
            return 1;
        } else {
            if ((text1.contains(text2)) || (text2.contains(text1))) {
                return 2;
            }
        }
        return 0;
    }

    public List<Map<Long, Set<Long>>> addRelationships() {
        Map<Long, String> titleMap;
        Map<Long, String> titleMap2;
        List<Map<Long, Set<Long>>> nodePair = new ArrayList<>();
        titleMap = getNodeTitle(DOCX);
        titleMap2 = getNodeTitle(DOCX);
        Map<Long, Integer> cc = new HashMap<>();
        for (Map.Entry<Long, String> entry : titleMap.entrySet()) {
            Long id1 = entry.getKey();
            if (!cc.containsKey(id1)) cc.put(id1, 0);
            String title1 = entry.getValue();
            Map<Long, Set<Long>> nodeMap = new HashMap<>();
            Set<Long> nodeSet = new HashSet<>();
            titleMap2.remove(id1);
            for (Map.Entry<Long, String> entry2 : titleMap2.entrySet()) {
                Long id2 = entry2.getKey();
                String title2 = entry2.getValue();
                if (!title2.equals("")) {
                    int grade = isMatch(title1, title2);
                    if (grade != 0) {
                        if (!cc.containsKey(id1)) cc.put(id1, 1);
                        else cc.put(id1, cc.get(id1) + 1);
                        if (!cc.containsKey(id2)) cc.put(id2, 1);
                        else cc.put(id2, cc.get(id2) + 1);
                        nodeSet.add(id2);
                    }
                }
            }
            if ((cc.get(id1) < 10) && (cc.get(id1) > 0)) {
                if (nodeSet.size() != 0) {
                    nodeMap.put(id1, nodeSet);
                    nodePair.add(nodeMap);
                }
            }
        }
        return nodePair;
    }

}

