package cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.entity;

import cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.StackOverflowGraphBuilder;
import org.neo4j.graphdb.Node;

public class QuestionInfo {

    private Node node = null;
    private int questionId = 0;
    private int acceptedAnswerId = -1;
    private int ownerUserId = -1;


    public QuestionInfo(Node node, int id, String creationDate, int score, int viewCount, String body, int ownerUserId, String title, String tags, int acceptedAnswerId) {
        this.node = node;
        this.questionId = id;
        this.acceptedAnswerId = acceptedAnswerId;
        this.ownerUserId = ownerUserId;

        node.addLabel(StackOverflowGraphBuilder.QUESTION);

        node.setProperty(StackOverflowGraphBuilder.QUESTION_ID, id);
        node.setProperty(StackOverflowGraphBuilder.QUESTION_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowGraphBuilder.QUESTION_SCORE, score);
        node.setProperty(StackOverflowGraphBuilder.QUESTION_VIEW_COUNT, viewCount);
        node.setProperty(StackOverflowGraphBuilder.QUESTION_BODY, body);
        node.setProperty(StackOverflowGraphBuilder.QUESTION_OWNER_USER_ID, ownerUserId);
        node.setProperty(StackOverflowGraphBuilder.QUESTION_TITLE, title);
        node.setProperty(StackOverflowGraphBuilder.QUESTION_TAGS, tags);

    }

    public int getQuestionId() {
        return questionId;
    }

    public int getAcceptedAnswerId() {
        return acceptedAnswerId;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public Node getNode() {
        return node;
    }

}
