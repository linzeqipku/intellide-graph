package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping.NLPAttributeMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping.NLPAttributeSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping.NLPVertexMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping.NLPVertexSchemaMapping;
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
    public NLPNode getNodeById(int id){
        return nodes.get(id);
    }
    public Query copy(){
        Query newQuery = new Query();
        newQuery.tokens = this.tokens;
        newQuery.text = this.text;
        for (NLPNode node : nodes)
            newQuery.nodes.add(node.copy());
        for (NLPNode node : newQuery.nodes){
            NLPNode oldnode = getNodeById(node.id);
            for (NLPNode n : oldnode.lastNode) node.lastNode.add(newQuery.getNodeById(n.id));
            for (NLPNode n : oldnode.nextNode) node.nextNode.add(newQuery.getNodeById(n.id));
        }
        return newQuery;
    }
    public Query copyOut(){
        Query newQuery = new Query();
        newQuery.text = this.text;
        for (NLPToken token : tokens){
            newQuery.tokens.add(token.copy());
        }
        return newQuery;
    }
    public JSONObject toJsonQuery() throws JSONException {
        int totedges = 0;
        JSONObject queryJson = new JSONObject();
        queryJson.put("score",score);
        queryJson.put("cypher",cypher);
        JSONObject graphJson = new JSONObject();
        JSONArray nodesJson = new JSONArray();
        JSONArray edgesJson =  new JSONArray();
        for (NLPNode node : nodes){
            JSONObject nodeObj = new JSONObject();
            nodeObj.put("id",node.id);
            nodeObj.put("focus",node.focus);

            if (node.token.mapping instanceof NLPVertexMapping){
                nodeObj.put("type", "vertex");
                nodeObj.put("typeName", ((NLPVertexSchemaMapping)node.token.mapping).vertexType.name);
                nodeObj.put("display", ((NLPVertexSchemaMapping)node.token.mapping).vertexType.name + " : " + ((NLPVertexMapping)node.token.mapping).vertex.name);
            }
            else if (node.token.mapping instanceof NLPVertexSchemaMapping){
                nodeObj.put("type", "vertexSchema");
                nodeObj.put("typeName", ((NLPVertexSchemaMapping)node.token.mapping).vertexType.name);
                nodeObj.put("display", ((NLPVertexSchemaMapping)node.token.mapping).vertexType.name);
            }
            else if (node.token.mapping instanceof NLPAttributeSchemaMapping) {
                nodeObj.put("type", "attributeSchema");
                nodeObj.put("typeName", ((NLPAttributeSchemaMapping)node.token.mapping).attrType);
                nodeObj.put("display", ((NLPAttributeSchemaMapping)node.token.mapping).attrType);
            }
            else if (node.token.mapping instanceof NLPAttributeMapping){
                nodeObj.put("type", "attribute");
                nodeObj.put("typeName", ((NLPAttributeMapping)node.token.mapping).type.attrType);
                nodeObj.put("display",((NLPAttributeMapping)node.token.mapping).type.attrType + " : " + ((NLPAttributeMapping)node.token.mapping).attrValue);
            }
            for (int i = 0; i < node.nextNode.size(); i++){
                NLPRelation r = node.nextRelation.get(i);
                NLPNode n = node.nextNode.get(i);
                JSONObject relObj = new JSONObject();
                relObj.put("start",node.id);
                relObj.put("end",n.id);
                if (r.edgeType != null) relObj.put("type",r.edgeType.name); else relObj.put("type",r.otherType);
                relObj.put("id", totedges);
                totedges++;
                edgesJson.put(relObj);
            }
            nodesJson.put(nodeObj);
        }
         queryJson.put("returnType",returnType);
        graphJson.put("nodes",nodesJson);
        graphJson.put("edges",edgesJson);
        queryJson.put("graph",graphJson);
        queryJson.put("rank",rank);
        return queryJson;
    }
}
