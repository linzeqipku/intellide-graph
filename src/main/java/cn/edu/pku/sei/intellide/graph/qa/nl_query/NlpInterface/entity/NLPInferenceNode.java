package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity;

public class NLPInferenceNode {
    public NLPNode node;
    public NLPInferenceNode nextInferNode;
    public NLPRelation nextRelation;
    public boolean direct = true;
    public boolean isEnd = false;

    public NLPInferenceNode(NLPNode node) {
        this.node = node;
    }
}
