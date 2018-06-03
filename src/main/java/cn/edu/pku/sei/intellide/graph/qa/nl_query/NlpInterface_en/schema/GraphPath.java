package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema;

import java.util.ArrayList;
import java.util.List;

public class GraphPath {
    public List<GraphVertexType> nodes = new ArrayList<>();
    public List<GraphEdgeType> edges = new ArrayList<>();
    public List<Boolean> edgesDirect = new ArrayList<>();
    public GraphVertexType start;
    public GraphVertexType end;
    public String name;
    public double score;
    public int length;
    public boolean ispreDefine = false;
}
