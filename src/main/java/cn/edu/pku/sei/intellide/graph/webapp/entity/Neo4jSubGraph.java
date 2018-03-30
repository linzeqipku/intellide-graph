package cn.edu.pku.sei.intellide.graph.webapp.entity;

import org.neo4j.driver.v1.Driver;

import java.util.ArrayList;
import java.util.List;

public class Neo4jSubGraph {

    private final List<Neo4jNode> nodes = new ArrayList<>();

    private final List<Neo4jRelation> relationships = new ArrayList<>();

    public Neo4jSubGraph(List<Long> nodeIds, List<Long> relIds, Driver driver){
        for (long node:nodeIds)
            nodes.add(Neo4jNode.get(node, driver));
        for (long edge:relIds)
            relationships.add(Neo4jRelation.get(edge, driver));
    }

    public List<Neo4jNode> getNodes() {
        return nodes;
    }

    public List<Neo4jRelation> getRelationships() {
        return relationships;
    }

}