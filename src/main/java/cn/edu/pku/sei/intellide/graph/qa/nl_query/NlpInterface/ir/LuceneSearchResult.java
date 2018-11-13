package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.ir;

public class LuceneSearchResult {

    public long id;
    public String vertex_type;
    public String attr_type;
    public String attr_val;
    public LuceneSearchResult(long id, String vertex_type, String attr_type, String attr_val) {
        this.id = id;
        this.vertex_type = vertex_type;
        this.attr_type = attr_type;
        this.attr_val = attr_val;
    }
}
