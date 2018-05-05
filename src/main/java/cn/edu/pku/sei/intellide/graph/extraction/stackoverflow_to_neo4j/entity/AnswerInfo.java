package cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.entity;

import cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.StackOverflowGraphBuilder;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

public class AnswerInfo {

    private Node node = null;
    private int answerId = 0;
    private int parentQuestionId = 0;
    private int ownerUserId = -1;

    public AnswerInfo(Node node, int id, int parentId, String creationDate, int score, String body, int ownerUserId) {
        this.node = node;
        this.answerId = id;
        this.parentQuestionId = parentId;
        this.ownerUserId = ownerUserId;

        node.addLabel(StackOverflowGraphBuilder.ANSWER);

        node.setProperty(StackOverflowGraphBuilder.ANSWER_ID, id);
        node.setProperty(StackOverflowGraphBuilder.ANSWER_PARENT_QUESTION_ID, parentId);
        node.setProperty(StackOverflowGraphBuilder.ANSWER_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowGraphBuilder.ANSWER_SCORE, score);
        node.setProperty(StackOverflowGraphBuilder.ANSWER_BODY, body);
        node.setProperty(StackOverflowGraphBuilder.ANSWER_OWNER_USER_ID, ownerUserId);
        node.setProperty(StackOverflowGraphBuilder.ANSWER_ACCEPTED, false);

    }

    public int getAnswerId() {
        return answerId;
    }

    public int getParentQuestionId() {
        return parentQuestionId;
    }

    public void setAccepted(boolean accepted) {
        node.setProperty(StackOverflowGraphBuilder.ANSWER_ACCEPTED, accepted);
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public Node getNode() {
        return node;
    }

}
