package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class GraphSchemaKeywords {
    public Map<String,Pair<String,String>> types = new HashMap<>();
    public static GraphSchemaKeywords single = null;
//    private GraphSchemaKeywords(){
//        types.put(JavaCodeExtractor.CLASS, new ImmutablePair<>(JavaCodeExtractor.CLASS_NAME,JavaCodeExtractor.CLASS_FULLNAME));
//        types.put(JavaCodeExtractor.METHOD, new ImmutablePair<>(JavaCodeExtractor.METHOD_NAME,JavaCodeExtractor.METHOD_COMMENT));
//        types.put(JavaCodeExtractor.INTERFACE, new ImmutablePair<>(JavaCodeExtractor.INTERFACE_NAME,JavaCodeExtractor.INTERFACE_FULLNAME));
//        types.put(JavaCodeExtractor.FIELD, new ImmutablePair<>(JavaCodeExtractor.FIELD_NAME,JavaCodeExtractor.FIELD_COMMENT));
//
//        types.put(BugzillaExtractor.BUGZILLAISSUE, new ImmutablePair<>(BugzillaExtractor.ISSUE_BUGID,BugzillaExtractor.ISSUE_SHORTDESC));
//        types.put(BugzillaExtractor.ISSUECOMMENT, new ImmutablePair<>(BugzillaExtractor.COMMENT_ID,BugzillaExtractor.COMMENT_NAME));
//        types.put(BugzillaExtractor.BUGZILLAUSER, new ImmutablePair<>(BugzillaExtractor.USER_ID,BugzillaExtractor.USER_NAME));
//
//        types.put("GitCommit", new ImmutablePair<>("commitId","commitId"));
//        types.put(GitExtractor.MUTATEDFILE, new ImmutablePair<>(GitExtractor.MUTATEDFILE_FILE_NAME,GitExtractor.MUTATEDFILE_API_NAME));
//        types.put(GitExtractor.MUTATEDCONTENT, new ImmutablePair<>(GitExtractor.MUTATEDCONTENT_COMMIT_UUID,GitExtractor.MUTATEDCONTENT_CONTENT));
//        types.put("GitCommitAuthor", new ImmutablePair<>(GitExtractor.COMMITAUTHOR_NAME,GitExtractor.COMMITAUTHOR_NAME));
//
//        types.put(JiraGraphBuilder.ISSUE, new ImmutablePair<>(JiraGraphBuilder.ISSUE_ID,JiraGraphBuilder.ISSUE_NAME));
//        types.put(JiraGraphBuilder.ISSUECOMMENT, new ImmutablePair<>(JiraGraphBuilder.ISSUECOMMENT_ID,JiraGraphBuilder.ISSUECOMMENT_BODY));
//        types.put(JiraGraphBuilder.ISSUEUSER, new ImmutablePair<>(JiraGraphBuilder.ISSUEUSER_NAME,JiraGraphBuilder.ISSUEUSER_EMAIL_ADDRESS));
//        types.put(JiraGraphBuilder.PATCH, new ImmutablePair<>(JiraGraphBuilder.PATCH_ID,JiraGraphBuilder.PATCH_NAME));
//
//        types.put(MailGraphBuilder.MAIL, new ImmutablePair<>(MailGraphBuilder.MAIL_ID,MailGraphBuilder.MAIL_SUBJECT));
//        types.put(MailGraphBuilder.MAILUSER, new ImmutablePair<>(MailGraphBuilder.MAILUSER_NAMES,MailGraphBuilder.MAILUSER_MAIL));
//
//        types.put(StackOverflowGraphBuilder.QUESTION, new ImmutablePair<>(StackOverflowGraphBuilder.QUESTION_ID,StackOverflowGraphBuilder.QUESTION_TITLE));
//        types.put(StackOverflowGraphBuilder.ANSWER, new ImmutablePair<>(StackOverflowGraphBuilder.ANSWER_ID,StackOverflowGraphBuilder.ANSWER_BODY));
//        types.put(StackOverflowGraphBuilder.COMMENT, new ImmutablePair<>(StackOverflowGraphBuilder.COMMENT_ID,StackOverflowGraphBuilder.COMMENT_TEXT));
//        types.put(StackOverflowGraphBuilder.USER, new ImmutablePair<>(StackOverflowGraphBuilder.USER_ID,StackOverflowGraphBuilder.USER_DISPLAY_NAME));
//    }

//    private GraphSchemaKeywords(){
//        types.put(JavaCodeExtractor.CLASS, new ImmutablePair<>(JavaCodeExtractor.CLASS_NAME,JavaCodeExtractor.CLASS_FULLNAME));
//        types.put(JavaCodeExtractor.METHOD, new ImmutablePair<>(JavaCodeExtractor.METHOD_NAME,JavaCodeExtractor.METHOD_COMMENT));
//        types.put(JavaCodeExtractor.INTERFACE, new ImmutablePair<>(JavaCodeExtractor.INTERFACE_NAME,JavaCodeExtractor.INTERFACE_FULLNAME));
//        types.put(JavaCodeExtractor.FIELD, new ImmutablePair<>(JavaCodeExtractor.FIELD_NAME,JavaCodeExtractor.FIELD_COMMENT));
//
//        types.put(BugzillaExtractor.BUGZILLAISSUE, new ImmutablePair<>(BugzillaExtractor.ISSUE_BUGID,BugzillaExtractor.ISSUE_SHORTDESC));
//        types.put(BugzillaExtractor.ISSUECOMMENT, new ImmutablePair<>(BugzillaExtractor.COMMENT_ID,BugzillaExtractor.COMMENT_NAME));
//        types.put(BugzillaExtractor.BUGZILLAUSER, new ImmutablePair<>(BugzillaExtractor.USER_ID,BugzillaExtractor.USER_NAME));
//
//        types.put(GitExtractor.COMMIT, new ImmutablePair<>(GitExtractor.COMMIT_UUID,GitExtractor.COMMIT_SVN_URL));
//        types.put(GitExtractor.MUTATEDFILE, new ImmutablePair<>(GitExtractor.MUTATEDFILE_FILE_NAME,GitExtractor.MUTATEDFILE_API_NAME));
//        types.put(GitExtractor.MUTATEDCONTENT, new ImmutablePair<>(GitExtractor.MUTATEDCONTENT_COMMIT_UUID,GitExtractor.MUTATEDCONTENT_CONTENT));
//        types.put(GitExtractor.COMMITAUTHOR, new ImmutablePair<>(GitExtractor.COMMITAUTHOR_NAME,GitExtractor.COMMITAUTHOR_NAME));
//
//        types.put(JiraGraphBuilder.ISSUE, new ImmutablePair<>(JiraGraphBuilder.ISSUE_ID,JiraGraphBuilder.ISSUE_NAME));
//        types.put(JiraGraphBuilder.ISSUECOMMENT, new ImmutablePair<>(JiraGraphBuilder.ISSUECOMMENT_ID,JiraGraphBuilder.ISSUECOMMENT_BODY));
//        types.put(JiraGraphBuilder.ISSUEUSER, new ImmutablePair<>(JiraGraphBuilder.ISSUEUSER_NAME,JiraGraphBuilder.ISSUEUSER_EMAIL_ADDRESS));
//        types.put(JiraGraphBuilder.PATCH, new ImmutablePair<>(JiraGraphBuilder.PATCH_ID,JiraGraphBuilder.PATCH_NAME));
//
//        types.put(MailGraphBuilder.MAIL, new ImmutablePair<>(MailGraphBuilder.MAIL_ID,MailGraphBuilder.MAIL_SUBJECT));
//        types.put(MailGraphBuilder.MAILUSER, new ImmutablePair<>(MailGraphBuilder.MAILUSER_NAMES,MailGraphBuilder.MAILUSER_MAIL));
//
//        types.put(StackOverflowGraphBuilder.QUESTION, new ImmutablePair<>(StackOverflowGraphBuilder.QUESTION_ID,StackOverflowGraphBuilder.QUESTION_TITLE));
//        types.put(StackOverflowGraphBuilder.ANSWER, new ImmutablePair<>(StackOverflowGraphBuilder.ANSWER_ID,StackOverflowGraphBuilder.ANSWER_BODY));
//        types.put(StackOverflowGraphBuilder.COMMENT, new ImmutablePair<>(StackOverflowGraphBuilder.COMMENT_ID,StackOverflowGraphBuilder.COMMENT_TEXT));
//        types.put(StackOverflowGraphBuilder.USER, new ImmutablePair<>(StackOverflowGraphBuilder.USER_ID,StackOverflowGraphBuilder.USER_DISPLAY_NAME));
//    }
private GraphSchemaKeywords(){
    types.put("Class", new ImmutablePair<>("name","fullName"));
    types.put("Method", new ImmutablePair<>("name","fullName"));
    types.put("Field",new ImmutablePair<>("name","fullName"));
    types.put("Docx",new ImmutablePair<>("title","title"));
    types.put("GitUser",new ImmutablePair<>("name","name"));
    types.put("Commit",new ImmutablePair<>("name","name"));
}
    public static GraphSchemaKeywords getSingle() {
        if (single == null){
            single = new GraphSchemaKeywords();
        }
        return single;
    }
}
