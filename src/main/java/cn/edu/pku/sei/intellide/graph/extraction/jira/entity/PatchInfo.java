package cn.edu.pku.sei.intellide.graph.extraction.jira.entity;

import java.util.UUID;

/*
 * A patch entity which contains the information about a patch.
 */
public class PatchInfo {
    private String uuid = UUID.randomUUID().toString();

    private String projectName = "";// the project which current patch belongs to
    private String issueId = "";// the issue which current patch belongs to
    private String patchId = "";
    private String patchName = "";
    private String content = "";
    private String creatorName = "";
    private String createdDate = "";

    public PatchInfo() {
    }

    public PatchInfo(String patchId, String patchName, String creatorName, String createdDate) {
        this.patchId = patchId;
        this.patchName = patchName;
        this.creatorName = creatorName;
        this.createdDate = createdDate;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getPatchId() {
        return patchId;
    }

    public void setPatchId(String patchId) {
        this.patchId = patchId;
    }

    public String getPatchName() {
        return patchName;
    }

    public void setPatchName(String patchName) {
        this.patchName = patchName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "*****************************\n" +
                "A patch:\n" +
                "projectname:" + projectName + "\n" +
                "issueId:" + issueId + "\n" +
                "patchId:" + patchId + "\n" +
                "patchName:" + patchName + "\n" +
                "content:" + content + "\n" +
                "creatorName:" + creatorName + "\n" +
                "createdDate:" + createdDate + "\n" +
                "*****************************\n";
    }
}
