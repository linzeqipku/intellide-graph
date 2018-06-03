package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping;


import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.extractmodel.Vertex;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphVertexType;

public class NLPAttributeMapping extends NLPMapping {
    public Vertex vertex;
    public String attrValue;
    public Object value;
    public NLPAttributeSchemaMapping type;
    public NLPAttributeMapping(Vertex vertex, GraphVertexType vertexType, String attrType, String attrValue, NLPToken token, double similar){
        super(token,similar);
        type = new NLPAttributeSchemaMapping(vertexType,attrType,token,similar);
        this.vertex = vertex;
        this.attrValue = attrValue;
    }
}
