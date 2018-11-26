package cn.edu.pku.sei.intellide.graph.webapp.entity;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.ArrayList;
import java.util.List;

public class Neo4jSubGraph {

    private final List<Neo4jNode> nodes = new ArrayList<>();

    private final List<Neo4jRelation> relationships = new ArrayList<>();

    private String cypher = "";

    public Neo4jSubGraph() {

    }

    public Neo4jSubGraph(List<Long> nodeIds, List<Long> relIds, GraphDatabaseService db) {
        for (long node : nodeIds)
            nodes.add(Neo4jNode.get(node, db));
        for (long edge : relIds)
            relationships.add(Neo4jRelation.get(edge, db));
    }

    public List<Neo4jNode> getNodes() {
        return nodes;
    }

    public List<Neo4jRelation> getRelationships() {
        return relationships;
    }

    public String getCypher() {
        return cypher;
    }

    public void setCypher(String cypher) {
        this.cypher = cypher;
    }

}