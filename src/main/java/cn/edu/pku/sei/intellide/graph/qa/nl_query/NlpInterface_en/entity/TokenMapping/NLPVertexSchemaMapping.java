package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.ir.LuceneSearchResult;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphVertexType;

import java.util.List;
import java.util.Set;

public class NLPVertexSchemaMapping extends NLPMapping {
    public GraphVertexType vertexType;
    public List<LuceneSearchResult> l;
    public Set<Long> s;
    public boolean must = false;
    public NLPVertexSchemaMapping(GraphVertexType vertexType, NLPToken token, double similar){
        super(token, similar);
        this.vertexType = vertexType;
    }
    @Override
    public boolean equals(Object v) {
        return v instanceof NLPVertexSchemaMapping &&  vertexType.equals(((NLPVertexSchemaMapping) v).vertexType);
    }
}
