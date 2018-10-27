package cn.edu.pku.sei.intellide.graph.extraction.jira.entity;

import java.util.UUID;

/*
 * An issue entity which contains the information about an issue.
 */
public class IssueInfo {

    private String uuid = UUID.randomUUID().toString();

    private String issueId = "";
    private String issueName = "";

    private String summary = "";
    private String type = "";
    private String status = "";
    private String priority = "";
    private String resolution = "";
    private String versions = "";
    private String fixVersions = "";
    private String components = "";
    private String labels = "";
    private String description = "";

    private String crearorName = "";
    private String assigneeName = "";
    private String reporterName = "";

    //possible to be null
    private String createdDate = "";
    private String updatedDate = "";
    private String resolutionDate = "";

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public String getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(String fixVersions) {
        this.fixVersions = fixVersions;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCrearorName() {
        return crearorName;
    }

    public void setCrearorName(String crearorName) {
        this.crearorName = crearorName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(String resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public String getUuid() {
        return uuid;
    }
}
