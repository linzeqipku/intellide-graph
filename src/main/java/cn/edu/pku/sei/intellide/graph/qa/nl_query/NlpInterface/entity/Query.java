package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPAttributeMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPAttributeSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexSchemaMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Query {
    public String text;
    public List<NLPToken> tokens = new ArrayList<>();
    public Set<NLPInferenceLink> inferenceLinks = new HashSet<>();
    public List<NLPNode> nodes = new ArrayList<>();
    public NLPNode focusNode = null;
    public double score = 0;
    public String cypher;
    public int rank;
    public String returnType = "node";

    public NLPNode getNodeById(int id) {
        return nodes.get(id);
    }

    public Query copy() {
        Query newQuery = new Query();
        newQuery.tokens = this.tokens;
        newQuery.text = this.text;
        for (NLPNode node : nodes)
            newQuery.nodes.add(node.copy());
        for (NLPNode node : newQuery.nodes) {
            NLPNode oldnode = getNodeById(node.id);
            for (NLPNode n : oldnode.lastNode) node.lastNode.add(newQuery.getNodeById(n.id));
            for (NLPNode n : oldnode.nextNode) node.nextNode.add(newQuery.getNodeById(n.id));
        }
        return newQuery;
    }

    public Query copyOut() {
        Query newQuery = new Query();
        newQuery.text = this.text;
        for (NLPToken token : tokens) {
            newQuery.tokens.add(token.copy());
        }
        return newQuery;
    }

}
