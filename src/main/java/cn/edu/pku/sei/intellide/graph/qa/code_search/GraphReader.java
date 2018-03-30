package cn.edu.pku.sei.intellide.graph.qa.code_search;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.*;

public class GraphReader {
    GraphDatabaseService graphDb;
    public GraphReader(String path){
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(path));
    }

    public GraphReader(GraphDatabaseService db){
        graphDb = db;
    }
    public List<MyNode> getAjacentGraph(){
        List<MyNode> graph = new ArrayList<>();
        Map<Long, MyNode> id2NodeMap = new HashMap<>();

        try(Transaction tx = graphDb.beginTx()){
            ResourceIterator<Node> iterator = graphDb.getAllNodes().iterator();
            while(iterator.hasNext()){
                Node node = iterator.next();
                if (!node.hasProperty("tokens"))
                    continue;

                MyNode myNode = new MyNode(node.getId());
                myNode.fullName = (String)node.getProperty("fullName");
                id2NodeMap.put(node.getId(), myNode);
                String tokenString = (String)node.getProperty("tokens");
                for (String s : tokenString.split(" "))
                    myNode.cnWordSet.add(s);
                graph.add(myNode);
            }
            tx.success();
        }

        try(Transaction tx = graphDb.beginTx()){
            ResourceIterator<Node> iterator = graphDb.getAllNodes().iterator();
            while(iterator.hasNext()){
                Node node = iterator.next();
                if (!node.hasProperty("tokens"))
                    continue;
                MyNode myNode = id2NodeMap.get(node.getId());
                Iterator<Relationship> relationIter = node.getRelationships().iterator();
                while(relationIter.hasNext()){
                    Relationship relation = relationIter.next();
                    Node otherNode = relation.getOtherNode(node);
                    if (otherNode.hasProperty("tokens")) {
                        MyNode otherMyNode = id2NodeMap.get(otherNode.getId());
                        myNode.neighbors.add(otherMyNode);
                    }
                }
            }
            tx.success();
        }
        System.out.println("node set size: " + graph.size());
        return graph;
    }

    public long getEdgeIdByNodes(long id1, long id2){
        long result = 0;
        try(Transaction tx = graphDb.beginTx()) {
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

    public static void main(String[] args){
        GraphReader graphReader = new GraphReader("F:\\testdata\\graph.db-tokens");
        APILocater locater = new APILocater(graphReader);
        String[] query = {"区域", "游客"};
        MySubgraph result = locater.query(new HashSet<>(Arrays.asList(query)));
        result.print();
    }
}
