package cn.edu.pku.sei.intellide.graph.extraction.jira;

import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.IssueCommentInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.IssueInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.IssueUserInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.PatchInfo;
import org.neo4j.graphdb.Node;

class JiraUtils {

    public static void createIssueNode(IssueInfo issueInfo, Node node) {
        node.addLabel(JiraExtractor.ISSUE);
        node.setProperty(JiraExtractor.ISSUE_ID, issueInfo.getIssueId());
        node.setProperty(JiraExtractor.ISSUE_NAME, issueInfo.getIssueName());
        node.setProperty(JiraExtractor.ISSUE_SUMMARY, issueInfo.getSummary());
        node.setProperty(JiraExtractor.ISSUE_TYPE, issueInfo.getType());
        node.setProperty(JiraExtractor.ISSUE_STATUS, issueInfo.getStatus());
        node.setProperty(JiraExtractor.ISSUE_PRIORITY, issueInfo.getPriority());
        node.setProperty(JiraExtractor.ISSUE_RESOLUTION, issueInfo.getResolution());
        node.setProperty(JiraExtractor.ISSUE_VERSIONS, issueInfo.getVersions());
        node.setProperty(JiraExtractor.ISSUE_FIX_VERSIONS, issueInfo.getFixVersions());
        node.setProperty(JiraExtractor.ISSUE_COMPONENTS, issueInfo.getComponents());
        node.setProperty(JiraExtractor.ISSUE_LABELS, issueInfo.getLabels());
        node.setProperty(JiraExtractor.ISSUE_DESCRIPTION, issueInfo.getDescription());
        node.setProperty(JiraExtractor.ISSUE_CREATOR_NAME, issueInfo.getCrearorName());
        node.setProperty(JiraExtractor.ISSUE_ASSIGNEE_NAME, issueInfo.getAssigneeName());
        node.setProperty(JiraExtractor.ISSUE_REPORTER_NAME, issueInfo.getReporterName());
        node.setProperty(JiraExtractor.ISSUE_CREATED_DATE, issueInfo.getCreatedDate());
        node.setProperty(JiraExtractor.ISSUE_UPDATED_DATE, issueInfo.getUpdatedDate());
        node.setProperty(JiraExtractor.ISSUE_RESOLUTION_DATE, issueInfo.getResolutionDate());
    }

    public static void createPatchNode(PatchInfo patchInfo, Node node) {
        node.addLabel(JiraExtractor.PATCH);
        node.setProperty(JiraExtractor.PATCH_ISSUE_ID, patchInfo.getIssueId());
        node.setProperty(JiraExtractor.PATCH_ID, patchInfo.getPatchId());
        node.setProperty(JiraExtractor.PATCH_NAME, patchInfo.getPatchName());
        node.setProperty(JiraExtractor.PATCH_CONTENT, patchInfo.getContent());
        node.setProperty(JiraExtractor.PATCH_CREATOR_NAME, patchInfo.getCreatorName());
        node.setProperty(JiraExtractor.PATCH_CREATED_DATE, patchInfo.getCreatedDate());
    }

    public static void createIssueCommentNode(IssueCommentInfo issueCommentInfo, Node node) {
        node.addLabel(JiraExtractor.ISSUECOMMENT);
        node.setProperty(JiraExtractor.ISSUECOMMENT_ID, issueCommentInfo.getCommentId());
        node.setProperty(JiraExtractor.ISSUECOMMENT_BODY, issueCommentInfo.getBody());
        node.setProperty(JiraExtractor.ISSUECOMMENT_CREATOR_NAME, issueCommentInfo.getCreatorName());
        node.setProperty(JiraExtractor.ISSUECOMMENT_UPDATER_NAME, issueCommentInfo.getUpdaterName());
        node.setProperty(JiraExtractor.ISSUECOMMENT_CREATED_DATE, issueCommentInfo.getCreatedDate());
        node.setProperty(JiraExtractor.ISSUECOMMENT_UPDATED_DATE, issueCommentInfo.getUpdatedDate());
    }

    public static void createIssueUserNode(IssueUserInfo issueUserInfo, Node node) {
        node.addLabel(JiraExtractor.ISSUEUSER);
        node.setProperty(JiraExtractor.ISSUEUSER_NAME, issueUserInfo.getName());
        node.setProperty(JiraExtractor.ISSUEUSER_EMAIL_ADDRESS, issueUserInfo.getName());
        node.setProperty(JiraExtractor.ISSUEUSER_DISPLAY_NAME, issueUserInfo.getName());
        node.setProperty(JiraExtractor.ISSUEUSER_ACTIVE, issueUserInfo.getName());
    }

}
