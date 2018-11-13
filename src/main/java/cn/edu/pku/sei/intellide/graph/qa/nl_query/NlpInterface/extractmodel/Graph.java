package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

    public Map<Long, Vertex> vertexes = new HashMap<>();

    public void add(Vertex v) {
        vertexes.put(v.id, v);
    }

    public Vertex get(long id) {
        return vertexes.get(id);
    }

    public boolean contains(long id) {
        return vertexes.containsKey(id);
    }

    public Set<Vertex> getAllVertexes() {
        return new HashSet<>(vertexes.values());
    }

}
