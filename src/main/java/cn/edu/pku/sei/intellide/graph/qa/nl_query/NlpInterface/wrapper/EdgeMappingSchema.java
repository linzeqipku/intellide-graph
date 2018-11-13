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
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.ArrayList;
import java.util.List;

public class EdgeMappingSchema {
    private Query query;
    private List<Query> queries;
    private int[] mapEnd = new int[100];
    private int[] mapStart = new int[100];
    private List<NLPNode> nodes;
    private int tot = 0;

    public List<Query> process(Query query, GraphDatabaseService db) {
        this.query = query;
        nodes = query.nodes;
        queries = new ArrayList<>();
        tot = 0;
        dfs(0, db);
        return queries;
    }

    private void dfs(int t, GraphDatabaseService db) {
        if (t == query.tokens.size()) {
            List<NLPNode> tmp = query.nodes;
            query.nodes = nodes;
            Query newquery = query.copy();
            query.nodes = tmp;
            for (int i = 0; i < t; i++) {
                NLPToken token = query.tokens.get(i);
                if (token.mapping instanceof NLPEdgeSchemaMapping) {
                    GraphEdgeType edgeType = ((NLPEdgeSchemaMapping) token.mapping).edgeType;
                    NLPRelation relation1 = new NLPRelation(edgeType, token);
                    NLPRelation relation2 = new NLPRelation(edgeType, token);
                    relation1.mirror = relation2;
                    relation2.mirror = relation1;
                    relation2.direct = false;
                    NLPNode nodeStartFinal = newquery.nodes.get(mapStart[i]);
                    NLPNode nodeEndFinal = newquery.nodes.get(mapEnd[i]);
                    nodeStartFinal.addNext(nodeEndFinal, relation1);
                    nodeEndFinal.addLast(nodeStartFinal, relation2);
                }
                if (token.mapping instanceof NLPPathSchemaMapping) {
                    GraphPath path = ((NLPPathSchemaMapping) token.mapping).path;
                    NLPNode nodeStartFinal = newquery.nodes.get(mapStart[i]);
                    NLPNode nodeEndFinal = newquery.nodes.get(mapEnd[i]);
                    NLPNode last = nodeStartFinal;
                    int tot = 0;
                    for (GraphVertexType vertexType : path.nodes) {
                        NLPRelation relation1 = new NLPRelation(path.edges.get(tot), token);
                        NLPRelation relation2 = new NLPRelation(path.edges.get(tot), token);
                        relation1.mirror = relation2;
                        relation2.mirror = relation1;
                        relation2.direct = false;
                        NLPNode newNode = new NLPNode(new NLPToken("what"));
                        newNode.middle = true;
                        NLPMapping mapping = new NLPVertexSchemaMapping(vertexType, newNode.token, 1);
                        newNode.token.mapping = mapping;
                        newNode.id = newquery.nodes.size();
                        newquery.nodes.add(newNode);
                        if (path.edgesDirect.get(tot)) {
                            last.addNext(newNode, relation1);
                            newNode.addLast(last, relation2);
                        } else {
                            newNode.addNext(last, relation1);
                            last.addLast(newNode, relation2);
                        }
                        last = newNode;
                        tot++;
                    }
                    NLPRelation relation1 = new NLPRelation(path.edges.get(tot), token);
                    NLPRelation relation2 = new NLPRelation(path.edges.get(tot), token);
                    relation1.mirror = relation2;
                    relation2.mirror = relation1;
                    relation2.direct = false;
                    if (path.edgesDirect.get(tot)) {
                        last.addNext(nodeEndFinal, relation1);
                        nodeEndFinal.addLast(last, relation2);
                    } else {
                        nodeEndFinal.addNext(last, relation1);
                        last.addLast(nodeEndFinal, relation2);
                    }

                }
            }
            if (!linkAttributeSchemaAndVectexSchemaAndVertex(newquery)) return;
            if (tot <= 4) queries.add(newquery);
            return;
        }
        if (!(query.tokens.get(t).mapping instanceof NLPEdgeSchemaMapping || query.tokens.get(t).mapping instanceof NLPPathSchemaMapping)) {
            dfs(t + 1, db);
            return;
        }
        double direct = 1;
        NLPToken token = query.tokens.get(t);
        if (token.POS.equals("VBN")) direct = -1;

        String startname;
        String endname;

        if (query.tokens.get(t).mapping instanceof NLPEdgeSchemaMapping) {
            GraphEdgeType edgeType = ((NLPEdgeSchemaMapping) token.mapping).edgeType;
            startname = edgeType.start.name;
            endname = edgeType.end.name;
        } else {
            GraphPath path = ((NLPPathSchemaMapping) token.mapping).path;
            startname = path.start.name;
            endname = path.end.name;
        }

        List<NLPNode> tmpnodes = new ArrayList<>();
        tmpnodes.addAll(nodes);
        for (NLPNode nodeStart : tmpnodes)
            if (nodeStart.token.mapping instanceof NLPVertexSchemaMapping) {
                if (((NLPVertexSchemaMapping) nodeStart.token.mapping).vertexType.name.equals(startname))
                    for (NLPNode nodeEnd : tmpnodes)
                        if (nodeEnd.token.mapping instanceof NLPVertexSchemaMapping) {
                            if (nodeStart == nodeEnd) continue;
                            if (((NLPVertexSchemaMapping) nodeEnd.token.mapping).vertexType.name.equals(endname)) {
                                mapStart[t] = nodeStart.id;
                                mapEnd[t] = nodeEnd.id;
                                dfs(t + 1, db);
                            }
                        }
            }

        for (NLPNode node : tmpnodes)
            if (node.token.mapping instanceof NLPVertexSchemaMapping) {
                boolean flag = false;
                if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(startname)
                        && ((node.token.offsetVal < token.offsetVal && direct > 0) || (node.token.offsetVal > token.offsetVal && direct < 0))) {
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getInstance(db).getGraphSchema()
                            .vertexTypes.get(endname), newNode.token, 1);
                    newNode.token.mapping = mapping;
                    newNode.token.offsetVal = query.tokens.get(t).offsetVal + 0.5 * direct;
                    newNode.id = nodes.size();
                    nodes.add(newNode);
                    mapStart[t] = node.id;
                    mapEnd[t] = newNode.id;
                    tot++;
                    dfs(t + 1, db);
                    tot--;
                    nodes.remove(newNode);
                    flag = true;
                }
                if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(endname)) {
//                    && ((node.token.offsetVal < token.offsetVal && direct < 0) || (node.token.offsetVal > token.offsetVal && direct > 0))){
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getInstance(db).getGraphSchema()
                            .vertexTypes.get(startname), newNode.token, 1);
                    newNode.token.mapping = mapping;
                    newNode.token.offsetVal = query.tokens.get(t).offsetVal - 0.5 * direct;
                    newNode.id = nodes.size();
                    nodes.add(newNode);
                    mapStart[t] = newNode.id;
                    mapEnd[t] = node.id;
                    tot++;
                    dfs(t + 1, db);
                    tot--;
                    nodes.remove(newNode);
                    flag = true;
                }
                if (!flag) {
                    if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(startname)) {
                        NLPNode newNode = new NLPNode(new NLPToken("what"));
                        NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getInstance(db).getGraphSchema()
                                .vertexTypes.get(endname), newNode.token, 1);
                        newNode.token.mapping = mapping;
                        newNode.token.offsetVal = query.tokens.get(t).offsetVal + 0.5;
                        newNode.id = nodes.size();
                        nodes.add(newNode);
                        mapStart[t] = node.id;
                        mapEnd[t] = newNode.id;
                        tot++;
                        dfs(t + 1, db);
                        tot--;
                        nodes.remove(newNode);
                        flag = true;
                    }
                    if (((NLPVertexSchemaMapping) node.token.mapping).vertexType.name.equals(endname)) {
                        NLPNode newNode = new NLPNode(new NLPToken("what"));
                        NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getInstance(db).getGraphSchema()
                                .vertexTypes.get(startname), newNode.token, 1);
                        newNode.token.mapping = mapping;
                        newNode.token.offsetVal = query.tokens.get(t).offsetVal - 0.5;
                        newNode.id = nodes.size();
                        nodes.add(newNode);
                        mapStart[t] = newNode.id;
                        mapEnd[t] = node.id;
                        tot++;
                        dfs(t + 1, db);
                        tot--;
                        nodes.remove(newNode);
                        flag = true;
                    }
                }
            }
        NLPNode newNodeStart = new NLPNode(new NLPToken("what"));
        NLPMapping mappingStart = new NLPVertexSchemaMapping(ExtractModel.getInstance(db).getGraphSchema()
                .vertexTypes.get(startname), newNodeStart.token, 1);
        newNodeStart.token.mapping = mappingStart;
        newNodeStart.id = nodes.size();
        newNodeStart.token.offsetVal = query.tokens.get(t).offsetVal - 0.5;
        mapStart[t] = newNodeStart.id;
        nodes.add(newNodeStart);

        NLPNode newNodeEnd = new NLPNode(new NLPToken("what"));
        NLPMapping mappingEnd = new NLPVertexSchemaMapping(ExtractModel.getInstance(db).getGraphSchema()
                .vertexTypes.get(endname), newNodeEnd.token, 1);
        newNodeEnd.token.mapping = mappingEnd;
        newNodeEnd.id = nodes.size();
        newNodeEnd.token.offsetVal = query.tokens.get(t).offsetVal + 0.5;
        mapEnd[t] = newNodeEnd.id;
        nodes.add(newNodeEnd);
        tot += 2;
        dfs(t + 1, db);
        tot -= 2;
        nodes.remove(newNodeStart);
        nodes.remove(newNodeEnd);
    }

    public static boolean linkAttributeSchemaAndVectexSchemaAndVertex(Query query) {
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

