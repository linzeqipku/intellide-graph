package cn.edu.pku.sei.intellide.graph.extraction.qa.entity;

import cn.edu.pku.sei.intellide.graph.extraction.qa.StackOverflowExtractor;
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

        node.addLabel(StackOverflowExtractor.QUESTION);

        node.setProperty(StackOverflowExtractor.QUESTION_ID, id);
        node.setProperty(StackOverflowExtractor.QUESTION_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowExtractor.QUESTION_SCORE, score);
        node.setProperty(StackOverflowExtractor.QUESTION_VIEW_COUNT, viewCount);
        node.setProperty(StackOverflowExtractor.QUESTION_BODY, body);
        node.setProperty(StackOverflowExtractor.QUESTION_OWNER_USER_ID, ownerUserId);
        node.setProperty(StackOverflowExtractor.QUESTION_TITLE, title);
        node.setProperty(StackOverflowExtractor.QUESTION_TAGS, tags);

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
