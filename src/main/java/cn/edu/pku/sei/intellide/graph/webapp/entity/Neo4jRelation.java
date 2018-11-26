package cn.edu.pku.sei.intellide.graph.webapp.entity;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Neo4jRelation {

    private final long startNode, endNode, id;
    private final String type;

    private Neo4jRelation(long startNode, long endNode, long id, String type) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.id = id;
        this.type = type;
    }

    public static List<Neo4jRelation> getNeo4jRelationList(long nodeId, GraphDatabaseService db) {
        List<Neo4jRelation> list = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {
            Iterator<Relationship> rels = db.getNodeById(nodeId).getRelationships().iterator();
            while (rels.hasNext()) {
                Relationship rel = rels.next();
                list.add(new Neo4jRelation(rel.getStartNodeId(), rel.getEndNodeId(), rel.getId(), rel.getType().name()));
            }
            tx.success();
        }
        return list;
    }

    public static Neo4jRelation get(long rId, GraphDatabaseService db) {
        Neo4jRelation r = null;
        try (Transaction tx = db.beginTx()) {
            Relationship rel = db.getRelationshipById(rId);
            r = new Neo4jRelation(rel.getStartNodeId(), rel.getEndNodeId(), rel.getId(), rel.getType().name());
            tx.success();
        }
        return r;
    }

    public long getStartNode() {
        return startNode;
    }

    public long getEndNode() {
        return endNode;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}