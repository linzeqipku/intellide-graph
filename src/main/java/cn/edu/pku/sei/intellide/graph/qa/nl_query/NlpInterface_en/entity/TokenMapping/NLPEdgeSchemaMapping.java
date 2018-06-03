package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping;


import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphEdgeType;

public class NLPEdgeSchemaMapping extends NLPMapping {
    public String type;
    public GraphEdgeType edgeType;
    public NLPEdgeSchemaMapping(String type, GraphEdgeType edgeType, NLPToken token, double similar){
        super(token, similar);
        this.type = type;
        this.edgeType = edgeType;
    }
}
