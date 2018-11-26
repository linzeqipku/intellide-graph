package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphSchema {
    public Map<String, GraphVertexType> vertexTypes = new HashMap<>();
    public Map<String, Set<GraphEdgeType>> edgeTypes = new HashMap<>();
    public Map<String, Set<GraphPath>> paths = new HashMap<>();

    public GraphEdgeType findGraphEdgeTypeByNameAndVertex(String name, GraphVertexType vertex1, GraphVertexType vertex2) {
        for (GraphEdgeType edgeType : edgeTypes.get(name)) {
            if ((vertex1 == null || edgeType.start.equals(vertex1)) && (vertex2 == null || edgeType.end.equals(vertex2)))
                return edgeType;
        }
        return null;
    }

    public GraphEdgeType findGraphEdgeTypeByVertex(GraphVertexType vertex1, GraphVertexType vertex2) {
        for (Set<GraphEdgeType> edgeTypes : edgeTypes.values()) {
            for (GraphEdgeType edgeType : edgeTypes) {
                if (edgeType.start.equals(vertex1) && edgeType.end.equals(vertex2))
                    return edgeType;
            }
        }
        return null;
    }
}
