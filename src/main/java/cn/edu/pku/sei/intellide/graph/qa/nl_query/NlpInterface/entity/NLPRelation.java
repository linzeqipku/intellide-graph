package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphAttribute;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphEdgeType;

public class NLPRelation {
    public NLPToken token;
    public GraphEdgeType edgeType;
    public String otherType;
    public NLPRelation mirror;
    public boolean direct = true;

    public NLPRelation() {
    }

    public NLPRelation(String type) {
        this.otherType = type;
    }

    public NLPRelation(GraphEdgeType type, String name) {
        this.edgeType = type;
        this.otherType = name;
    }

    public NLPRelation(GraphEdgeType type, NLPToken token) {
        this.edgeType = type;
        this.token = token;
    }
}
