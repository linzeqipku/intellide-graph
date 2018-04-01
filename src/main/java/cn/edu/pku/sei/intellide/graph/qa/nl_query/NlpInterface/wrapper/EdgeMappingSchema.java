package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPNode;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPRelation;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.*;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.ExtractModel;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphEdgeType;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphPath;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphVertexType;

import java.util.ArrayList;
import java.util.List;

public class EdgeMappingSchema {
    public static Query query;
    public static  List<Query> queries;
    public static int[] mapEnd = new int[100];
    public static int[] mapStart = new int[100];
    public static List<NLPNode> nodes;
    public static int tot = 0;
    public static List<Query> process(Query query){
        EdgeMappingSchema.query = query;
        nodes = query.nodes;
        queries = new ArrayList<>();
        tot = 0;
        DFS(0);
        return queries;
    }
    public static void DFS(int t){
        if (t == query.tokens.size()){
            List<NLPNode> tmp = query.nodes;
            query.nodes = nodes;
            Query newquery = query.copy();
            query.nodes = tmp;
            for (int i = 0; i < t; i++){
                NLPToken token = query.tokens.get(i);
                if (token.mapping instanceof  NLPEdgeSchemaMapping){
                    GraphEdgeType edgeType = ((NLPEdgeSchemaMapping) token.mapping).edgeType;
                    NLPRelation relation1 = new NLPRelation(edgeType,token);
                    NLPRelation relation2 = new NLPRelation(edgeType,token);
                    relation1.mirror = relation2;
                    relation2.mirror = relation1;
                    relation2.direct = false;
                    NLPNode nodeStartFinal = newquery.nodes.get(mapStart[i]);
                    NLPNode nodeEndFinal = newquery.nodes.get(mapEnd[i]);
                    nodeStartFinal.addNext(nodeEndFinal, relation1);
                    nodeEndFinal.addLast(nodeStartFinal, relation2);
                }
                if (token.mapping instanceof  NLPPathSchemaMapping){
                    GraphPath path = ((NLPPathSchemaMapping) token.mapping).path;
                    NLPNode nodeStartFinal = newquery.nodes.get(mapStart[i]);
                    NLPNode nodeEndFinal = newquery.nodes.get(mapEnd[i]);
                    NLPNode last = nodeStartFinal;
                    int tot = 0;
                    for (GraphVertexType vertexType : path.nodes){
                        NLPRelation relation1 = new NLPRelation(path.edges.get(tot),token);
                        NLPRelation relation2 = new NLPRelation(path.edges.get(tot),token);
                        relation1.mirror = relation2;
                        relation2.mirror = relation1;
                        relation2.direct = false;
                        NLPNode newNode = new NLPNode(new NLPToken("what"));
                        newNode.middle = true;
                        NLPMapping mapping = new NLPVertexSchemaMapping(vertexType,newNode.token,1);
                        newNode.token.mapping = mapping;
                        newNode.id = newquery.nodes.size();
                        newquery.nodes.add(newNode);
                        if (path.edgesDirect.get(tot)) {
                            last.addNext(newNode,relation1);
                            newNode.addLast(last,relation2);
                        }else{
                            newNode.addNext(last,relation1);
                            last.addLast(newNode,relation2);
                        }
                        last = newNode;
                        tot++;
                    }
                    NLPRelation relation1 = new NLPRelation(path.edges.get(tot),token);
                    NLPRelation relation2 = new NLPRelation(path.edges.get(tot),token);
                    relation1.mirror = relation2;
                    relation2.mirror = relation1;
                    relation2.direct = false;
                    if (path.edgesDirect.get(tot)) {
                        last.addNext(nodeEndFinal, relation1);
                        nodeEndFinal.addLast(last, relation2);
                    }else{
                        nodeEndFinal.addNext(last, relation1);
                        last.addLast(nodeEndFinal, relation2);
                    }

                }
            }
            if (!linkAttributeSchemaAndVectexSchemaAndVertex(newquery)) return;
            if (tot <= 4)queries.add(newquery);
            return;
        }
        if (!(query.tokens.get(t).mapping instanceof  NLPEdgeSchemaMapping || query.tokens.get(t).mapping instanceof NLPPathSchemaMapping)) {
            DFS(t+1);
            return;
        }
        double direct = 1;
        NLPToken token = query.tokens.get(t);
        if (token.POS.equals("VBN")) direct = -1;

        String startname;
        String endname;

        if (query.tokens.get(t).mapping instanceof  NLPEdgeSchemaMapping){
            GraphEdgeType edgeType = ((NLPEdgeSchemaMapping) token.mapping).edgeType;
            startname = edgeType.start.name;
            endname  =edgeType.end.name;
        }else{
            GraphPath path = ((NLPPathSchemaMapping) token.mapping).path;
            startname = path.start.name;
            endname  = path.end.name;
        }

        List<NLPNode> tmpnodes = new ArrayList<>();
        tmpnodes.addAll(nodes);
        for (NLPNode nodeStart : tmpnodes) if (nodeStart.token.mapping instanceof NLPVertexSchemaMapping){
            if (((NLPVertexSchemaMapping) nodeStart.token.mapping).vertexType.name.equals(startname))
                for (NLPNode nodeEnd : tmpnodes) if (nodeEnd.token.mapping instanceof  NLPVertexSchemaMapping){
                    if (nodeStart == nodeEnd) continue;
                    if (((NLPVertexSchemaMapping) nodeEnd.token.mapping).vertexType.name.equals(endname)){
                        mapStart[t] = nodeStart.id;
                        mapEnd[t] = nodeEnd.id;
                        DFS(t+1);
                    }
                }
        }

        for (NLPNode node : tmpnodes) if (node.token.mapping instanceof  NLPVertexSchemaMapping) {
            boolean flag = false;
            if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(startname)
                    && ((node.token.offsetVal < token.offsetVal && direct > 0) || (node.token.offsetVal > token.offsetVal && direct < 0))){
                NLPNode newNode = new NLPNode(new NLPToken("what"));
                NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                        graphSchema.vertexTypes.get(endname),newNode.token,1);
                newNode.token.mapping = mapping;
                newNode.token.offsetVal = query.tokens.get(t).offsetVal + 0.5 * direct;
                newNode.id = nodes.size();
                nodes.add(newNode);
                mapStart[t] = node.id;
                mapEnd[t] = newNode.id;
                tot++;
                DFS(t+1);
                tot--;
                nodes.remove(newNode);
                flag = true;
            }
            if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(endname)){
//                    && ((node.token.offsetVal < token.offsetVal && direct < 0) || (node.token.offsetVal > token.offsetVal && direct > 0))){
                NLPNode newNode = new NLPNode(new NLPToken("what"));
                NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                        graphSchema.vertexTypes.get(startname),newNode.token,1);
                newNode.token.mapping = mapping;
                newNode.token.offsetVal = query.tokens.get(t).offsetVal - 0.5*direct;
                newNode.id = nodes.size();
                nodes.add(newNode);
                mapStart[t] = newNode.id;
                mapEnd[t] = node.id;
                tot++;
                DFS(t+1);
                tot--;
                nodes.remove(newNode);
                flag = true;
            }
            if (!flag){
                if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(startname)){
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                            graphSchema.vertexTypes.get(endname),newNode.token,1);
                    newNode.token.mapping = mapping;
                    newNode.token.offsetVal = query.tokens.get(t).offsetVal + 0.5 ;
                    newNode.id = nodes.size();
                    nodes.add(newNode);
                    mapStart[t] = node.id;
                    mapEnd[t] = newNode.id;
                    tot++;
                    DFS(t+1);
                    tot--;
                    nodes.remove(newNode);
                    flag = true;
                }
                if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(endname)){
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                            graphSchema.vertexTypes.get(startname),newNode.token,1);
                    newNode.token.mapping = mapping;
                    newNode.token.offsetVal = query.tokens.get(t).offsetVal - 0.5;
                    newNode.id = nodes.size();
                    nodes.add(newNode);
                    mapStart[t] = newNode.id;
                    mapEnd[t] = node.id;
                    tot++;
                    DFS(t+1);
                    tot--;
                    nodes.remove(newNode);
                    flag = true;
                }
            }
        }
        NLPNode newNodeStart = new NLPNode(new NLPToken("what"));
        NLPMapping mappingStart = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                graphSchema.vertexTypes.get(startname),newNodeStart.token,1);
        newNodeStart.token.mapping = mappingStart;
        newNodeStart.id = nodes.size();
        newNodeStart.token.offsetVal = query.tokens.get(t).offsetVal - 0.5;
        mapStart[t] = newNodeStart.id;
        nodes.add(newNodeStart);

        NLPNode newNodeEnd = new NLPNode(new NLPToken("what"));
        NLPMapping mappingEnd = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                graphSchema.vertexTypes.get(endname),newNodeEnd.token,1);
        newNodeEnd.token.mapping = mappingEnd;
        newNodeEnd.id = nodes.size();
        newNodeEnd.token.offsetVal = query.tokens.get(t).offsetVal + 0.5;
        mapEnd[t] = newNodeEnd.id;
        nodes.add(newNodeEnd);
        tot+=2;
        DFS(t+1);
        tot-=2;
        nodes.remove(newNodeStart);
        nodes.remove(newNodeEnd);
    }
    public static void mappingEdgeSchema(){
        for (NLPToken token : query.tokens) if (token.mapping instanceof NLPEdgeSchemaMapping){
            double direct = 1;
            if (token.POS.equals("VBN")) direct = -1;
            boolean flagFind = false;
            String edgeTypeName = ((NLPEdgeSchemaMapping) token.mapping).type;
            NLPNode nodeStartFinal = null;
            NLPNode nodeEndFinal = null;
            double score = 10000;
            for (NLPNode nodeStart : query.nodes) if (nodeStart.token.mapping instanceof NLPVertexSchemaMapping){
                if (((NLPVertexSchemaMapping) nodeStart.token.mapping).vertexType.outcomings.keySet().contains(edgeTypeName))
                    for (NLPNode nodeEnd : query.nodes) if (nodeEnd.token.mapping instanceof  NLPVertexSchemaMapping){
                        if (nodeStart == nodeEnd) continue;
                        if (((NLPVertexSchemaMapping) nodeEnd.token.mapping).vertexType.incomings.keySet().contains(edgeTypeName)){
                            flagFind = true;
                            double newscore = Math.abs(token.offsetVal + direct*1 -nodeEnd.token.offsetVal)+Math.abs(token.offsetVal - direct*1 -nodeStart.token.offsetVal);
                            if (newscore < score){
                                score = newscore ;
                                nodeStartFinal = nodeStart;
                                nodeEndFinal = nodeEnd;
                            }
                            //break;
                        }
                    }
                //if (flagFind) break;

            }
            if (flagFind) {
                GraphEdgeType edgeType = ExtractModel.getSingle().graphSchema.findGraphEdgeTypeByNameAndVertex(edgeTypeName,
                        ((NLPVertexSchemaMapping) nodeStartFinal.token.mapping).vertexType, ((NLPVertexSchemaMapping) nodeEndFinal.token.mapping).vertexType);
                NLPRelation relation1 = new NLPRelation(edgeType,token);
                NLPRelation relation2 = new NLPRelation(edgeType,token);
                relation1.mirror = relation2;
                relation2.mirror = relation1;
                nodeStartFinal.addNext(nodeEndFinal, relation1);
                nodeEndFinal.addLast(nodeStartFinal, relation2);
                continue;
            }

            NLPNode newNodeLast = null;
            for (NLPNode node : query.nodes) if (node.token.mapping instanceof  NLPVertexSchemaMapping){
                if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.outcomings.keySet().contains(edgeTypeName)){
                    GraphEdgeType edgeType = ExtractModel.getSingle().graphSchema.findGraphEdgeTypeByNameAndVertex(edgeTypeName,
                            ((NLPVertexSchemaMapping) node.token.mapping).vertexType,null);
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                            graphSchema.vertexTypes.get(edgeType.end.name),newNode.token,1);
                    newNode.token.mapping = mapping;
                    newNode.focus = true;
//                    NLPRelation relation1 = new NLPRelation(edgeType);
//                    NLPRelation relation2 = new NLPRelation(edgeType);
//                    relation1.mirror = relation2;
//                    relation2.mirror = relation1;
//                    newNode.addLast(node,relation1);
//                    node.addNext(newNode,relation2);
//                    query.nodes.add(newNode);
                    double newscore = Math.abs(token.offsetVal - direct*1 -node.token.offsetVal);
                    if (newscore < score){
                        flagFind = true;
                        score = newscore ;
                        nodeStartFinal = node;
                        nodeEndFinal = newNode;
                        newNodeLast = newNode;
                    }
                }
                if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.incomings.keySet().contains(edgeTypeName)){
                    GraphEdgeType edgeType = ExtractModel.getSingle().graphSchema.findGraphEdgeTypeByNameAndVertex(edgeTypeName,
                            null,((NLPVertexSchemaMapping) node.token.mapping).vertexType);
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                            graphSchema.vertexTypes.get(edgeType.start.name),newNode.token,1);
                    newNode.token.mapping = mapping;
                    newNode.focus = true;
//                    NLPRelation relation1 = new NLPRelation(edgeType);
//                    NLPRelation relation2 = new NLPRelation(edgeType);
//                    relation1.mirror = relation2;
//                    relation2.mirror = relation1;
//                    newNode.addNext(node,relation1);
//                    node.addLast(newNode,relation2);
//                    query.nodes.add(newNode);
                    double newscore = Math.abs(token.offsetVal + direct*1 -node.token.offsetVal);
                    if (newscore < score){
                        flagFind = true;
                        score = newscore ;
                        nodeStartFinal = newNode;
                        nodeEndFinal = node;
                        newNodeLast = newNode;
                    }
                }
            }
            if (flagFind) {
                GraphEdgeType edgeType = ExtractModel.getSingle().graphSchema.findGraphEdgeTypeByNameAndVertex(edgeTypeName,
                        ((NLPVertexSchemaMapping) nodeStartFinal.token.mapping).vertexType, ((NLPVertexSchemaMapping) nodeEndFinal.token.mapping).vertexType);
                NLPRelation relation1 = new NLPRelation(edgeType,token);
                NLPRelation relation2 = new NLPRelation(edgeType,token);
                relation1.mirror = relation2;
                relation2.mirror = relation1;
                nodeStartFinal.addNext(nodeEndFinal, relation1);
                nodeEndFinal.addLast(nodeStartFinal, relation2);
                query.nodes.add(newNodeLast);
            }
        }
    }
    public static boolean linkAttributeSchemaAndVectexSchemaAndVertex(Query query){
        for (NLPNode node : query.nodes) {
            if (node.token.mapping instanceof NLPAttributeSchemaMapping && node.nextNode.isEmpty()) { //has no value
                boolean flag = false;
                for (long offset = 1; offset < 20; offset++) {
                    for (NLPNode faNode : query.nodes) {
                        if (Math.abs(faNode.token.offset - node.token.offset) != offset) continue;
                        if (faNode.token.mapping instanceof NLPVertexSchemaMapping && !(faNode.token.mapping instanceof NLPVertexMapping) &&
                                ((NLPVertexSchemaMapping) faNode.token.mapping).vertexType.equals(((NLPAttributeSchemaMapping) node.token.mapping).vertexType)) {
                            NLPRelation relation1 = new NLPRelation("has");
                            NLPRelation relation2 = new NLPRelation("has");
                            relation1.mirror = relation2;
                            relation2.mirror = relation1;
                            relation2.direct = false;
                            faNode.addNext(node, relation1);
                            node.addLast(faNode, relation2);
                            faNode.focus = true;
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                }
                if (flag) continue;
                for (long offset = 1; offset < 20; offset++) {
                    for (NLPNode faNode : query.nodes) {
                        if (Math.abs(faNode.token.offset - node.token.offset) != offset) continue;
                        if (faNode.token.mapping instanceof NLPVertexMapping &&
                                ((NLPVertexSchemaMapping) faNode.token.mapping).vertexType.equals(((NLPAttributeSchemaMapping) node.token.mapping).vertexType)) {
                            NLPRelation relation1 = new NLPRelation("has");
                            NLPRelation relation2 = new NLPRelation("has");
                            relation1.mirror = relation2;
                            relation2.mirror = relation1;
                            relation2.direct = false;
                            faNode.addNext(node, relation1);
                            node.addLast(faNode, relation2);
                            faNode.focus = true;
                            if (flag) break;
                            break;
                        }
                    }
                    if (flag) break;
                }
                if (!flag) return false;
            }
        }
        return true;
    }
}

