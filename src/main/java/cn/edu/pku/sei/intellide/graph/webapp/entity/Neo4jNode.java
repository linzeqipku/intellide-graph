package cn.edu.pku.sei.intellide.graph.webapp.entity;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.Map;

public class Neo4jNode {

    private final long id;
    private final String label;
    private final Map properties = new HashMap<>();

    public Neo4jNode(long id, String label, Map properties) {
        this.id = id;
        this.label = label;
        this.properties.putAll(properties);
    }

    private Neo4jNode(long id, String label) {
        this.id = id;
        this.label = label;
    }

    public static Neo4jNode get(long id, GraphDatabaseService db) {
        Neo4jNode node = null;
        try (Transaction tx = db.beginTx()) {
            Node oNode = db.getNodeById(id);
            node = new Neo4jNode(id, oNode.getLabels().iterator().next().name());
            node.properties.putAll(oNode.getAllProperties());
            node.properties.put("id", node.id);
            node.properties.put("label", node.label);
            tx.success();
        }
        return node;
    }

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Map getProperties() {
        return properties;
    }
}