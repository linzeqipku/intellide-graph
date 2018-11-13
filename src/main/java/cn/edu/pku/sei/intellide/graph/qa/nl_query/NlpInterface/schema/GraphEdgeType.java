package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema;

public class GraphEdgeType {
    public GraphVertexType start;
    public GraphVertexType end;
    public String name;
    public boolean direct = true;
    public double score;

    public GraphEdgeType(String name, GraphVertexType start, GraphVertexType end, boolean direct) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.direct = direct;
    }

    @Override
    public boolean equals(Object v) {
        return v instanceof GraphEdgeType && name.equals(((GraphEdgeType) v).name)
                && start.name.equals(((GraphEdgeType) v).start.name) && end.name.equals(((GraphEdgeType) v).end.name);
    }
}
