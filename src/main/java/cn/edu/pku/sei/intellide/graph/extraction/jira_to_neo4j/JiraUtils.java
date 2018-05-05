package cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j;

import cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j.entity.IssueCommentInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j.entity.IssueInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j.entity.IssueUserInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j.entity.PatchInfo;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

class JiraUtils {

    public static void createIssueNode(IssueInfo issueInfo, Node node) {
        node.addLabel(JiraGraphBuilder.ISSUE);
        node.setProperty(JiraGraphBuilder.ISSUE_ID, issueInfo.getIssueId());
        node.setProperty(JiraGraphBuilder.ISSUE_NAME, issueInfo.getIssueName());
        node.setProperty(JiraGraphBuilder.ISSUE_SUMMARY, issueInfo.getSummary());
        node.setProperty(JiraGraphBuilder.ISSUE_TYPE, issueInfo.getType());
        node.setProperty(JiraGraphBuilder.ISSUE_STATUS, issueInfo.getStatus());
        node.setProperty(JiraGraphBuilder.ISSUE_PRIORITY, issueInfo.getPriority());
        node.setProperty(JiraGraphBuilder.ISSUE_RESOLUTION, issueInfo.getResolution());
        node.setProperty(JiraGraphBuilder.ISSUE_VERSIONS, issueInfo.getVersions());
        node.setProperty(JiraGraphBuilder.ISSUE_FIX_VERSIONS, issueInfo.getFixVersions());
        node.setProperty(JiraGraphBuilder.ISSUE_COMPONENTS, issueInfo.getComponents());
        node.setProperty(JiraGraphBuilder.ISSUE_LABELS, issueInfo.getLabels());
        node.setProperty(JiraGraphBuilder.ISSUE_DESCRIPTION, issueInfo.getDescription());
        node.setProperty(JiraGraphBuilder.ISSUE_CREATOR_NAME, issueInfo.getCrearorName());
        node.setProperty(JiraGraphBuilder.ISSUE_ASSIGNEE_NAME, issueInfo.getAssigneeName());
        node.setProperty(JiraGraphBuilder.ISSUE_REPORTER_NAME, issueInfo.getReporterName());
        node.setProperty(JiraGraphBuilder.ISSUE_CREATED_DATE, issueInfo.getCreatedDate());
        node.setProperty(JiraGraphBuilder.ISSUE_UPDATED_DATE, issueInfo.getUpdatedDate());
        node.setProperty(JiraGraphBuilder.ISSUE_RESOLUTION_DATE, issueInfo.getResolutionDate());
    }

    public static void createPatchNode(PatchInfo patchInfo, Node node) {
        node.addLabel(JiraGraphBuilder.PATCH);
        node.setProperty(JiraGraphBuilder.PATCH_ISSUE_ID, patchInfo.getIssueId());
        node.setProperty(JiraGraphBuilder.PATCH_ID, patchInfo.getPatchId());
        node.setProperty(JiraGraphBuilder.PATCH_NAME, patchInfo.getPatchName());
        node.setProperty(JiraGraphBuilder.PATCH_CONTENT, patchInfo.getContent());
        node.setProperty(JiraGraphBuilder.PATCH_CREATOR_NAME, patchInfo.getCreatorName());
        node.setProperty(JiraGraphBuilder.PATCH_CREATED_DATE, patchInfo.getCreatedDate());
    }

    public static void createIssueCommentNode(IssueCommentInfo issueCommentInfo, Node node) {
        node.addLabel(JiraGraphBuilder.ISSUECOMMENT);
        node.setProperty(JiraGraphBuilder.ISSUECOMMENT_ID, issueCommentInfo.getCommentId());
        node.setProperty(JiraGraphBuilder.ISSUECOMMENT_BODY, issueCommentInfo.getBody());
        node.setProperty(JiraGraphBuilder.ISSUECOMMENT_CREATOR_NAME, issueCommentInfo.getCreatorName());
        node.setProperty(JiraGraphBuilder.ISSUECOMMENT_UPDATER_NAME, issueCommentInfo.getUpdaterName());
        node.setProperty(JiraGraphBuilder.ISSUECOMMENT_CREATED_DATE, issueCommentInfo.getCreatedDate());
        node.setProperty(JiraGraphBuilder.ISSUECOMMENT_UPDATED_DATE, issueCommentInfo.getUpdatedDate());
    }

    public static void createIssueUserNode(IssueUserInfo issueUserInfo, Node node) {
        node.addLabel(JiraGraphBuilder.ISSUEUSER);
        node.setProperty(JiraGraphBuilder.ISSUEUSER_NAME, issueUserInfo.getName());
        node.setProperty(JiraGraphBuilder.ISSUEUSER_EMAIL_ADDRESS, issueUserInfo.getName());
        node.setProperty(JiraGraphBuilder.ISSUEUSER_DISPLAY_NAME, issueUserInfo.getName());
        node.setProperty(JiraGraphBuilder.ISSUEUSER_ACTIVE, issueUserInfo.getName());
    }

}
