package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class GraphSchemaKeywords {
    public static GraphSchemaKeywords single = null;
    public Map<String, Pair<String, String>> types = new HashMap<>();

    private GraphSchemaKeywords() {
        types.put("Class", new ImmutablePair<>("name", "fullName"));
        types.put("Method", new ImmutablePair<>("name", "fullName"));
        types.put("Field", new ImmutablePair<>("name", "fullName"));
        types.put("Docx", new ImmutablePair<>("title", "title"));
        types.put("StackOverflowQuestion", new ImmutablePair<>("title", "title"));
        types.put("StackOverflowComment", new ImmutablePair<>("commentId", "commentId"));
        types.put("StackOverflowUser", new ImmutablePair<>("displayName", "displayName"));
        types.put("StackOverflowAnswer", new ImmutablePair<>("answerId", "answerId"));
        types.put("GitUser", new ImmutablePair<>("name", "name"));
        types.put("JiraIssue", new ImmutablePair<>("name", "name"));
        types.put("JiraIssueComment", new ImmutablePair<>("id", "id"));
        types.put("JiraIssueUser", new ImmutablePair<>("displayName", "displayName"));
        types.put("Mail", new ImmutablePair<>("mailId", "mailId"));
        types.put("MailUser", new ImmutablePair<>("names", "names"));
        types.put("Commit", new ImmutablePair<>("name", "name"));
    }

    public static GraphSchemaKeywords getSingle() {
        if (single == null) {
            single = new GraphSchemaKeywords();
        }
        return single;
    }
}
