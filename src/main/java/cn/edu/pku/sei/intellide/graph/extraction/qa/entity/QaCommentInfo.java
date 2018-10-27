package cn.edu.pku.sei.intellide.graph.extraction.qa.entity;

import cn.edu.pku.sei.intellide.graph.extraction.qa.StackOverflowExtractor;
import org.neo4j.graphdb.Node;

public class QaCommentInfo {

    private Node node = null;
    private int commentId = 0;
    private int parentId = 0;
    private int userId = -1;

    public QaCommentInfo(Node node, int id, int parentId, int score, String text, String creationDate, int userId) {
        this.node = node;
        this.commentId = id;
        this.parentId = parentId;
        this.userId = userId;

        node.addLabel(StackOverflowExtractor.COMMENT);

        node.setProperty(StackOverflowExtractor.COMMENT_ID, id);
        node.setProperty(StackOverflowExtractor.COMMENT_PARENT_ID, parentId);
        node.setProperty(StackOverflowExtractor.COMMENT_SCORE, score);
        node.setProperty(StackOverflowExtractor.COMMENT_TEXT, text);
        node.setProperty(StackOverflowExtractor.COMMENT_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowExtractor.COMMENT_USER_ID, userId);

    }

    public int getCommentId() {
        return commentId;
    }

    public int getParentId() {
        return parentId;
    }

    public int getUserId() {
        return userId;
    }

    public Node getNode() {
        return node;
    }
}
