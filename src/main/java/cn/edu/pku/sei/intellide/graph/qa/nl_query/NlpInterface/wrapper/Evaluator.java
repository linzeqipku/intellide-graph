package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPNode;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPRelation;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPPathSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexSchemaMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluator {
    private Query query;
    private boolean visited[] = new boolean[100];
    private Set<NLPNode> visitedNode = new HashSet<>();
    private Set<NLPRelation> visitedRelation = new HashSet<>();

    public void evaluate(Query query) {
        this.query = query;
        if (!isLink()) {
            query.score = -1;
            return;
        }
        double val = mappingNum() * 30 + offsetValue() + similar() * 10 + graphComplex() * 30 + (linkEntity()) * 100;
        query.score = val;
    }

    private int mappingNum() {
        int tot = 0;
        for (NLPToken token : query.tokens) {
            if (!token.nomapping) tot++;
            if (token.mapping != null) tot--;
        }
        return tot;
    }

    private double dfsNode(NLPNode start, NLPNode node, int len) {
        double tot = 0;
        visitedNode.add(node);
        List<NLPNode> allNodes = new ArrayList<>();
        List<NLPRelation> allRelations = new ArrayList<>();
        allNodes.addAll(node.nextNode);
        allNodes.addAll(node.lastNode);
        allRelations.addAll(node.nextRelation);
        allRelations.addAll(node.lastRelation);
        for (int i = 0; i < allNodes.size(); i++) {
            NLPNode n = allNodes.get(i);
            if (visitedNode.contains(n)) continue;
            NLPRelation r = allRelations.get(i);
            visitedRelation.add(r);
            visitedRelation.add(r.mirror);
            double bias = 2.0;

            if (r.token != null) {
                if (r.direct ^ !r.token.POS.equals("VBN") ^ (start.token.offsetVal < r.token.offsetVal)) {
                    bias = 1.0;
                }
                tot += bias * Math.abs(start.token.offsetVal - r.token.offsetVal) * (len + 1) / 2;
            } else if (!n.token.text.equals("what")) {
                tot += Math.abs(start.token.offsetVal - n.token.offsetVal) * (len + 1) / 2;
            } else {
                tot += dfsNode(start, n, len + 1);
            }

        }
        return tot;
    }

    private double offsetValue() {
        double val = 0;
        for (NLPNode node : query.nodes) {
            if (!node.token.text.equals("what")) {
                visitedRelation.clear();
                visitedNode.clear();
                val += dfsNode(node, node, 0);
            } else {
                for (int i = 0; i < node.nextNode.size(); i++) {
                    NLPNode n = node.nextNode.get(i);
                    NLPRelation r = node.nextRelation.get(i);
                    if (r.token != null) {
                        if (n.token.text.equals("what")) {

                        }
                    }
                }
            }

        }
        return val;
    }

    private double graphComplex() {
        double val = 0;
        for (NLPNode node : query.nodes) {
            if (node.token.text.equals("what")) {
                val += 1;
            }
            for (int i = 0; i < node.nextNode.size(); i++) {
                NLPRelation r = node.nextRelation.get(i);
                if (r.otherType != null && r.otherType.equals("hidden")) {
                    val += 1;
                }
            }
        }
        for (NLPToken token : query.tokens) {
            if (token.mapping instanceof NLPPathSchemaMapping) {
                val -= ((NLPPathSchemaMapping) token.mapping).path.nodes.size();
            }
        }
        return val;
    }

    private double similar() {
        double nodeVal = 0;
        int nodeNum = 0;
        for (NLPNode node : query.nodes) {
            if (!node.token.text.equals("what")) {
                nodeVal += node.token.mapping.rank;
                nodeNum++;
            }
        }
        for (NLPNode node : query.nodes) {
            for (int i = 0; i < node.nextNode.size(); i++) {
                NLPRelation r = node.nextRelation.get(i);
                if (r.token != null) {
                    nodeVal += r.token.mapping.rank;
                    nodeNum++;
                }
            }
        }
        if (nodeNum == 0) return 0;
        return nodeVal / nodeNum;
    }

    private double linkEntity() {
        double nodeVal = 0;
        int nodeNum = 0;
        for (NLPNode node : query.nodes) {
            for (int i = 0; i < node.nextNode.size(); i++) {
                NLPNode n = node.nextNode.get(i);
                if (n.token.mapping instanceof NLPVertexMapping && node.token.mapping instanceof NLPVertexMapping) {
                    nodeVal++;
                }
                nodeNum++;
            }
        }
        if (nodeNum == 0) return 0;
        return nodeVal / nodeNum;
    }

    private boolean isLink() {
        for (int i = 0; i < query.nodes.size(); i++) visited[i] = false;
        for (NLPNode node : query.nodes) {
            if (node.token.mapping instanceof NLPVertexSchemaMapping) {
                visit(node);
                break;
            }
        }

        for (int i = 0; i < query.nodes.size(); i++) {
            if (!visited[i]) return false;
        }
        return true;
    }

    private void visit(NLPNode node) {
        visited[node.id] = true;
        for (NLPNode nextNode : node.nextNode) {
            if (!visited[nextNode.id]) visit(nextNode);
        }
        for (NLPNode lastNode : node.lastNode) {
            if (!visited[lastNode.id]) visit(lastNode);
        }
    }

}

