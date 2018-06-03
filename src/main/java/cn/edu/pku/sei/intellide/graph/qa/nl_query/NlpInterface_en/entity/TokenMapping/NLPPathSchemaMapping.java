package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphPath;

public class NLPPathSchemaMapping extends NLPMapping {
    public String type;
    public GraphPath path;
    public NLPPathSchemaMapping(String type, GraphPath edgeType, NLPToken token, double similar){
        super(token, similar);
        this.type = type;
        this.path = edgeType;
    }
}
