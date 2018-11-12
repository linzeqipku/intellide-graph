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
//        types.put(JiraExtractor.ISSUE, new ImmutablePair<>(JiraExtractor.ISSUE_ID,JiraExtractor.ISSUE_NAME));
//        types.put(JiraExtractor.ISSUECOMMENT, new ImmutablePair<>(JiraExtractor.ISSUECOMMENT_ID,JiraExtractor.ISSUECOMMENT_BODY));
//        types.put(JiraExtractor.ISSUEUSER, new ImmutablePair<>(JiraExtractor.ISSUEUSER_NAME,JiraExtractor.ISSUEUSER_EMAIL_ADDRESS));
//        types.put(JiraExtractor.PATCH, new ImmutablePair<>(JiraExtractor.PATCH_ID,JiraExtractor.PATCH_NAME));
//
//        types.put(MailExtractor.MAIL, new ImmutablePair<>(MailExtractor.MAIL_ID,MailExtractor.MAIL_SUBJECT));
//        types.put(MailExtractor.MAILUSER, new ImmutablePair<>(MailExtractor.MAILUSER_NAMES,MailExtractor.MAILUSER_MAIL));
//
//        types.put(StackOverflowExtractor.QUESTION, new ImmutablePair<>(StackOverflowExtractor.QUESTION_ID,StackOverflowExtractor.QUESTION_TITLE));
//        types.put(StackOverflowExtractor.ANSWER, new ImmutablePair<>(StackOverflowExtractor.ANSWER_ID,StackOverflowExtractor.ANSWER_BODY));
//        types.put(StackOverflowExtractor.COMMENT, new ImmutablePair<>(StackOverflowExtractor.COMMENT_ID,StackOverflowExtractor.COMMENT_TEXT));
//        types.put(StackOverflowExtractor.USER, new ImmutablePair<>(StackOverflowExtractor.USER_ID,StackOverflowExtractor.USER_DISPLAY_NAME));
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
//        types.put(JiraExtractor.ISSUE, new ImmutablePair<>(JiraExtractor.ISSUE_ID,JiraExtractor.ISSUE_NAME));
//        types.put(JiraExtractor.ISSUECOMMENT, new ImmutablePair<>(JiraExtractor.ISSUECOMMENT_ID,JiraExtractor.ISSUECOMMENT_BODY));
//        types.put(JiraExtractor.ISSUEUSER, new ImmutablePair<>(JiraExtractor.ISSUEUSER_NAME,JiraExtractor.ISSUEUSER_EMAIL_ADDRESS));
//        types.put(JiraExtractor.PATCH, new ImmutablePair<>(JiraExtractor.PATCH_ID,JiraExtractor.PATCH_NAME));
//
//        types.put(MailExtractor.MAIL, new ImmutablePair<>(MailExtractor.MAIL_ID,MailExtractor.MAIL_SUBJECT));
//        types.put(MailExtractor.MAILUSER, new ImmutablePair<>(MailExtractor.MAILUSER_NAMES,MailExtractor.MAILUSER_MAIL));
//
//        types.put(StackOverflowExtractor.QUESTION, new ImmutablePair<>(StackOverflowExtractor.QUESTION_ID,StackOverflowExtractor.QUESTION_TITLE));
//        types.put(StackOverflowExtractor.ANSWER, new ImmutablePair<>(StackOverflowExtractor.ANSWER_ID,StackOverflowExtractor.ANSWER_BODY));
//        types.put(StackOverflowExtractor.COMMENT, new ImmutablePair<>(StackOverflowExtractor.COMMENT_ID,StackOverflowExtractor.COMMENT_TEXT));
//        types.put(StackOverflowExtractor.USER, new ImmutablePair<>(StackOverflowExtractor.USER_ID,StackOverflowExtractor.USER_DISPLAY_NAME));
//    }
private GraphSchemaKeywords(){
    types.put("Class", new ImmutablePair<>("name","fullName"));
    types.put("Method", new ImmutablePair<>("name","fullName"));
    types.put("Field",new ImmutablePair<>("name","fullName"));
    types.put("Docx",new ImmutablePair<>("title","title"));
    types.put("StackOverflowQuestion",new ImmutablePair<>("title","title"));
    types.put("StackOverflowComment",new ImmutablePair<>("commentId","commentId"));
    types.put("StackOverflowUser",new ImmutablePair<>("displayName","displayName"));
    types.put("StackOverflowAnswer",new ImmutablePair<>("answerId","answerId"));
    types.put("GitUser",new ImmutablePair<>("name","name"));
    types.put("JiraIssue",new ImmutablePair<>("name","name"));
    types.put("JiraIssueComment",new ImmutablePair<>("id","id"));
    types.put("JiraIssueUser",new ImmutablePair<>("displayName","displayName"));
    types.put("Mail",new ImmutablePair<>("mailId","mailId"));
    types.put("MailUser",new ImmutablePair<>("names","names"));
    types.put("Commit",new ImmutablePair<>("name","name"));
}
    public static GraphSchemaKeywords getSingle() {
        if (single == null){
            single = new GraphSchemaKeywords();
        }
        return single;
    }
}
