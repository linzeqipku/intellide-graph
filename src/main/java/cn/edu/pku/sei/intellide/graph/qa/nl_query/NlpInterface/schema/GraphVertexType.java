package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphVertexType {
    public Map<String, Set<GraphVertexType>> outcomings = new HashMap<>();
    public Map<String, Set<GraphVertexType>> incomings = new HashMap<>();
    public Set<GraphEdgeType> outcomingsEdges = new HashSet<>();
    public Set<GraphEdgeType> incomingsEdges = new HashSet<>();
    public Map<String, GraphAttribute> attrs = new HashMap<>();
    public Map<String, GraphPath> shortestPaths = new HashMap<>();
    public String name;
    public double score;

    public GraphVertexType(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object v) {
        return (v instanceof GraphVertexType && ((GraphVertexType) v).name.equals(name));
    }
}
