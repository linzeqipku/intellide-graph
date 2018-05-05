package cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.entity;

import cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.StackOverflowGraphBuilder;
import org.neo4j.graphdb.Node;

public class QaUserInfo {

    private Node node = null;
    private int userId;
    private String displayName;

    public QaUserInfo(Node node, int id, int reputation, String creationDate, String displayName, String lastAccessDate, int views, int upVotes, int downVotes) {
        this.node = node;
        this.userId = id;
        this.displayName = displayName;

        node.addLabel(StackOverflowGraphBuilder.USER);

        node.setProperty(StackOverflowGraphBuilder.USER_ID, id);
        node.setProperty(StackOverflowGraphBuilder.USER_REPUTATION, reputation);
        node.setProperty(StackOverflowGraphBuilder.USER_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowGraphBuilder.USER_DISPLAY_NAME, displayName);
        node.setProperty(StackOverflowGraphBuilder.USER_LAST_ACCESS_dATE, lastAccessDate);
        node.setProperty(StackOverflowGraphBuilder.USER_VIEWS, views);
        node.setProperty(StackOverflowGraphBuilder.USER_UP_VOTES, upVotes);
        node.setProperty(StackOverflowGraphBuilder.USER_DOWN_VOTES, downVotes);
    }

    public int getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Node getNode() {
        return node;
    }
}
