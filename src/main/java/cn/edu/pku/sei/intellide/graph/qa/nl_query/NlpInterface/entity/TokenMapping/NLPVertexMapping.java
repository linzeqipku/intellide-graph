package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.Vertex;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphVertexType;

public class NLPVertexMapping extends NLPVertexSchemaMapping {
    public Vertex vertex;

    public NLPVertexMapping(Vertex vertex, GraphVertexType vertexType, NLPToken token, double similar) {
        super(vertexType, token, similar);
        this.vertex = vertex;
    }

    public boolean equals(Object v) {
        return v instanceof NLPVertexMapping && vertexType.equals(((NLPVertexSchemaMapping) v).vertexType);
    }
}
