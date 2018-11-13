package cn.edu.pku.sei.intellide.graph.qa.code_search;

import cn.edu.pku.sei.intellide.graph.extraction.code_mention.CodeMentionExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.tokenization.TokenExtractor;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.*;

/*
 * 负责读取数据库的接口类
 */
public class GraphReader {
    GraphDatabaseService graphDb;

    public GraphReader(String path) {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(path));
    }

    public GraphReader(GraphDatabaseService db) {
        graphDb = db;
    }

    // 建立邻接表
    public List<MyNode> getAjacentGraph() {
        List<MyNode> graph = new ArrayList<>();
        Map<Long, MyNode> id2NodeMap = new HashMap<>();

        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> iterator = graphDb.getAllNodes().iterator();
            while (iterator.hasNext()) {
                Node node = iterator.next();
                if (!node.hasProperty(TokenExtractor.CODE_TOKENS))
                    continue;

                MyNode myNode = new MyNode(node.getId());
                myNode.fullName = (String) node.getProperty(JavaExtractor.FULLNAME);
                id2NodeMap.put(node.getId(), myNode);
                String tokenString = (String) node.getProperty(TokenExtractor.CODE_TOKENS);
                for (String s : tokenString.split(" "))
                    myNode.cnWordSet.add(s);
                myNode.weight += 1.0 / myNode.cnWordSet.size();
                graph.add(myNode);
            }
            tx.success();
        }

        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> iterator = graphDb.getAllNodes().iterator();
            while (iterator.hasNext()) {
                Node node = iterator.next();
                if (!node.hasProperty(TokenExtractor.CODE_TOKENS))
                    continue;
                MyNode myNode = id2NodeMap.get(node.getId());
                boolean hasDocRel = false; // whether has an edge to docs
                Iterator<Relationship> relationIter = node.getRelationships().iterator();
                while (relationIter.hasNext()) {
                    Relationship relation = relationIter.next();
                    if (relation.isType(CodeMentionExtractor.CODE_MENTION))
                        hasDocRel = true;
                    Node otherNode = relation.getOtherNode(node);
                    if (otherNode.hasProperty(TokenExtractor.CODE_TOKENS)) {
                        MyNode otherMyNode = id2NodeMap.get(otherNode.getId());
                        myNode.neighbors.add(otherMyNode);
                    }
                }
                if (hasDocRel)
                    myNode.weight += 1;
            }
            tx.success();
        }
        //System.out.println("node set size: " + graph.size());
        return graph;
    }

    public long getEdgeIdByNodes(long id1, long id2) {
        long result = 0;
        try (Transaction tx = graphDb.beginTx()) {
            Node node1 = graphDb.getNodeById(id1);
            Iterator<Relationship> iterator = node1.getRelationships().iterator();
            while (iterator.hasNext()) {
                Relationship relation = iterator.next();
                if (relation.getOtherNode(node1).getId() == id2) {
                    result = relation.getId();
                    break;
                }
            }
            tx.success();
        }
        return result;
    }

}
