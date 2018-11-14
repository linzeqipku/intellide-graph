package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPNode;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPRelation;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPAttributeSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphEdgeType;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphPath;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphVertexType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LinkAllNodes {

    private String languageIdentifier;
    private Query query;
    private List<Query> queries;
    private int color[] = new int[100];
    private double dis[][] = new double[100][100];
    private int disi[][] = new int[100][100];
    private int disj[][] = new int[100][100];
    private double prim[] = new double[100];
    private int primi[] = new int[100];
    private int primj[] = new int[100];
    private int colors = 0;

    public LinkAllNodes(String languageIdentifier){
        this.languageIdentifier = languageIdentifier;
    }

    public List<Query> process(Query query) {
        this.query = query;
        queries = new ArrayList<>();
        for (int i = 0; i < query.nodes.size() + 10; i++) {
            color[i] = -1;
            prim[i] = 1e10;
        }
        for (int i = 0; i < query.nodes.size() + 10; i++)
            for (int j = 0; j < query.nodes.size() + 10; j++)
                dis[i][j] = 1e10;
        colors = 0;
        coloring();
        mst();
        for (int i = 0; i < colors; i++)
            if (prim[i] > 1e9) {
                return queries;
            }
        linking();

        queries.add(query);
        return queries;
    }

    private static double getAverageOffset(NLPNode node) {
        List<NLPNode> allNodes = new ArrayList<>();
        List<NLPRelation> allRelations = new ArrayList<>();
        allNodes.addAll(node.nextNode);
        allNodes.addAll(node.lastNode);
        allRelations.addAll(node.nextRelation);
        allRelations.addAll(node.lastRelation);
        double offset = 0;
        int tmp = 0;
        for (int i = 0; i < allNodes.size(); i++) {
            if (allNodes.get(i).token.offset >= -0.1) {
                tmp++;
                offset += allNodes.get(i).token.offset;
            }
            if (allRelations.get(i).token != null) {
                tmp++;
                offset += allRelations.get(i).token.offset;
            }
        }
        offset /= tmp;
        return offset;
    }

    private static double distance(NLPNode node1, NLPNode node2) {
        if (node1.token.mapping instanceof NLPAttributeSchemaMapping) return 1e10;
        if (node2.token.mapping instanceof NLPAttributeSchemaMapping) return 1e10;


        if (node1.token.mapping instanceof NLPVertexSchemaMapping && node2.token.mapping instanceof NLPVertexSchemaMapping) {
            double delta = 10;
            if (node1.token.mapping instanceof NLPVertexMapping && node2.token.mapping instanceof NLPVertexMapping)
                delta = 20;
            if (!(node1.token.mapping instanceof NLPVertexMapping) && !(node2.token.mapping instanceof NLPVertexMapping))
                delta = 0;
            if (node1.middle || node2.middle) delta += 10;
            if (((NLPVertexSchemaMapping) node1.token.mapping).vertexType.shortestPaths.keySet().contains(((NLPVertexSchemaMapping) node2.token.mapping).vertexType.name)) {
                int step = ((NLPVertexSchemaMapping) node1.token.mapping).vertexType.shortestPaths.get(((NLPVertexSchemaMapping) node2.token.mapping).vertexType.name).edges.size();
                if (node1.token.offset < 0 && node2.token.offset < 0) {
                    return step * 100 + Math.abs(node1.token.offsetVal - node2.token.offsetVal) + delta;
                }
                double offset1 = node1.token.offset;
                double offset2 = node2.token.offset;
                if (offset1 < 0) {
                    offset1 = getAverageOffset(node1);
                }
                if (offset2 < 0) {
                    offset2 = getAverageOffset(node2);
                }
                return step * 100 + Math.abs(offset1 - offset2) + delta;

            }
        }
        return 1e10;
    }

    private void coloring() {
        for (int i = 0; i < query.nodes.size(); i++) {
            if (color[query.nodes.get(i).id] < 0) {
                visit(query.nodes.get(i), colors);
                colors++;
            }
        }
    }

    private void visit(NLPNode node, int c) {
        color[node.id] = c;
        for (NLPNode nextNode : node.nextNode) {
            if (color[nextNode.id] < 0) visit(nextNode, c);
        }
        for (NLPNode lastNode : node.lastNode) {
            if (color[lastNode.id] < 0) visit(lastNode, c);
        }
    }

    private void mst() {
        //distance between color
        for (int i = 0; i < colors; i++) {
            dis[i][i] = 0;
            for (int j = 0; j < colors; j++)
                if (i != j) {
                    for (int idi = 0; idi < query.nodes.size(); idi++)
                        if (color[query.nodes.get(idi).id] == i) {
                            for (int idj = 0; idj < query.nodes.size(); idj++)
                                if (color[query.nodes.get(idj).id] == j) {
                                    double tmp = distance(query.nodes.get(idi), query.nodes.get(idj));
                                    if (tmp < dis[i][j]) {
                                        dis[i][j] = tmp;
                                        disi[i][j] = idi;
                                        disj[i][j] = idj;
                                        dis[j][i] = tmp;
                                        disi[j][i] = idi;
                                        disj[j][i] = idj;
                                    }
                                }
                        }
                }
        }
        //mst
        for (int i = 0; i < colors; i++) prim[i] = 1e10;
        prim[0] = 0;
        int x = 0;
        for (int i = 0; i < colors - 1; i++) {
            for (int j = 0; j < colors; j++) {
                if (prim[j] > dis[x][j]) {
                    prim[j] = dis[x][j];
                    primi[j] = disi[x][j];
                    primj[j] = disj[x][j];
                }
            }
            x = colors;
            for (int j = 0; j < colors; j++) {
                if (prim[j] > 0.1 && prim[j] < prim[x]) {
                    x = j;
                }
            }
            prim[x] = 0;
        }
    }

    private void linking() {
        for (int i = 1; i < colors; i++) {
            NLPNode nodei = query.nodes.get(primi[i]);
            NLPNode nodej = query.nodes.get(primj[i]);
            GraphPath path = ((NLPVertexSchemaMapping) nodei.token.mapping).vertexType.shortestPaths.get(((NLPVertexSchemaMapping) nodej.token.mapping).vertexType.name);
            NLPNode last = nodei;
            for (GraphVertexType vertexType : path.nodes) {
                NLPRelation relation1 = new NLPRelation("hidden");
                NLPRelation relation2 = new NLPRelation("hidden");
                relation1.mirror = relation2;
                relation2.mirror = relation1;
                relation2.direct = false;
                NLPNode newNode = new NLPNode(new NLPToken("what"));
                newNode.middle = true;
                NLPMapping mapping = new NLPVertexSchemaMapping(vertexType, newNode.token, 1);
                newNode.token.mapping = mapping;
                newNode.id = query.nodes.size();
                query.nodes.add(newNode);
                last.addNext(newNode, relation1);
                newNode.addLast(last, relation2);
                last = newNode;
            }
            if (path.nodes.size() == 0) {
                double max = 0;
                GraphEdgeType edge = null;
                for (GraphEdgeType edgeType : ((NLPVertexSchemaMapping) nodei.token.mapping).vertexType.outcomingsEdges) {
                    if (edgeType.end.name.equals(((NLPVertexSchemaMapping) nodej.token.mapping).vertexType.name)) {
                        double tmp = TokenMapping.disTextf(edgeType.name, query, languageIdentifier);
                        if (max < tmp) {
                            max = tmp;
                            edge = edgeType;
                        }
                    }
                }
                for (GraphEdgeType edgeType : ((NLPVertexSchemaMapping) nodej.token.mapping).vertexType.outcomingsEdges) {
                    if (edgeType.end.name.equals(((NLPVertexSchemaMapping) nodei.token.mapping).vertexType.name)) {
                        double tmp = TokenMapping.disTextf(edgeType.name, query, languageIdentifier);
                        if (max < tmp) {
                            max = tmp;
                            edge = edgeType;
                        }
                    }
                }
                if (edge != null) {
                    NLPRelation relation1 = new NLPRelation(edge, "what");
                    NLPRelation relation2 = new NLPRelation(edge, "what");
                    if (edge.start.name.equals(((NLPVertexSchemaMapping) nodei.token.mapping).vertexType.name))
                        relation2.direct = false;
                    else relation1.direct = false;
                    relation1.mirror = relation2;
                    relation2.mirror = relation1;
                    if (relation1.direct) {
                        last.addNext(nodej, relation1);
                        nodej.addLast(last, relation2);
                    } else {
                        nodej.addNext(last, relation2);
                        last.addLast(nodej, relation1);
                    }
                    continue;
                }
            }

            NLPRelation relation1 = new NLPRelation("hidden");
            NLPRelation relation2 = new NLPRelation("hidden");
            relation1.mirror = relation2;
            relation2.mirror = relation1;
            relation2.direct = false;
            last.addNext(nodej, relation1);
            nodej.addLast(last, relation2);

        }
    }


}
