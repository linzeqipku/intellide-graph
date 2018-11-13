package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity;

import java.util.ArrayList;
import java.util.List;

public class NLPNode {
    public NLPToken token;
    public List<NLPNode> nextNode = new ArrayList<>();
    public List<NLPRelation> nextRelation = new ArrayList<>();
    public List<NLPNode> lastNode = new ArrayList<>();
    public List<NLPRelation> lastRelation = new ArrayList<>();

    public int id;
    public boolean focus = false;
    public boolean hasattr = false;
    public boolean middle = false;

    public NLPNode(NLPToken token) {
        this.token = token;
    }

    public void addNext(NLPNode node, NLPRelation relation) {
        nextNode.add(node);
        nextRelation.add(relation);
    }

    public void addLast(NLPNode node, NLPRelation relation) {
        lastNode.add(node);
        lastRelation.add(relation);
    }

    public NLPNode copy() {
        NLPNode node = new NLPNode(token);
        node.id = id;
        node.focus = focus;
        node.middle = middle;
        node.hasattr = hasattr;
        node.nextRelation.addAll(nextRelation);
        node.lastRelation.addAll(lastRelation);

        return node;
    }
}
