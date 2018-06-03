package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema;

public class GraphAttribute {
    public GraphVertexType belong;
    public String name;
    public GraphAttribute(String name, GraphVertexType belong){
        this.name = name;
        this.belong = belong;
    }
}
