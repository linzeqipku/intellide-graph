package cn.edu.pku.sei.intellide.graph.extraction.graph_xml_parser;

public class ProgramXml {

    private int id;
    private String name;
    private String codePath;
    private String emailPath;
    private String issuePath;
    private String commitPath;
    private String stackoverflowPath;
    private String docxPath;
    private String graphDataBasePath;
    private String description;
    private String projectJson;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public String getEmailPath() {
        return emailPath;
    }

    public void setEmailPath(String emailPath) {
        this.emailPath = emailPath;
    }

    public String getIssuePath() {
        return issuePath;
    }

    public void setIssuePath(String issuePath) {
        this.issuePath = issuePath;
    }

    public String getCommitPath() {
        return commitPath;
    }

    public void setCommitPath(String commitPath) {
        this.commitPath = commitPath;
    }

    public String getStackoverflowPath() {
        return stackoverflowPath;
    }

    public void setStackoverflowPath(String stackoverflowPath) {
        this.stackoverflowPath = stackoverflowPath;
    }

    public String getDocxPath() {
        return docxPath;
    }

    public void setDocxPath(String docxPath) {
        this.docxPath = docxPath;
    }

    public String getGraphDataBasePath() {
        return graphDataBasePath;
    }

    public void setGraphDataBasePath(String graphDataBasePath) {
        this.graphDataBasePath = graphDataBasePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectJson() {
        return projectJson;
    }

    public void setProjectJson(String projectJson) {
        this.projectJson = projectJson;
    }

    @Override
    public String toString(){
        return "Program [id="+id+", name="+ name+"]";
    }
}
