package cn.edu.pku.sei.intellide.graph.extraction.jira;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.IssueCommentInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.IssueInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.IssueUserInfo;
import cn.edu.pku.sei.intellide.graph.extraction.jira.entity.PatchInfo;
import cn.edu.pku.sei.intellide.graph.extraction.mail.utils.EmailAddressDecoder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JiraExtractor extends KnowledgeExtractor {

    public static final Label ISSUE = Label.label("JiraIssue");
    public static final String ISSUE_ID = "id";
    public static final String ISSUE_NAME = "name";
    public static final String ISSUE_SUMMARY = "summary";
    public static final String ISSUE_TYPE = "type";
    public static final String ISSUE_STATUS = "status";
    public static final String ISSUE_PRIORITY = "priority";
    public static final String ISSUE_RESOLUTION = "resolution";
    public static final String ISSUE_VERSIONS = "versions";
    public static final String ISSUE_FIX_VERSIONS = "fixVersions";
    public static final String ISSUE_COMPONENTS = "components";
    public static final String ISSUE_LABELS = "labels";
    public static final String ISSUE_DESCRIPTION = "description";
    public static final String ISSUE_CREATOR_NAME = "crearorName";
    public static final String ISSUE_ASSIGNEE_NAME = "assigneeName";
    public static final String ISSUE_REPORTER_NAME = "reporterName";
    public static final String ISSUE_CREATED_DATE = "createdDate";
    public static final String ISSUE_UPDATED_DATE = "updatedDate";
    public static final String ISSUE_RESOLUTION_DATE = "resolutionDate";

    public static final Label PATCH = Label.label("JiraPatch");
    public static final String PATCH_ISSUE_ID = "issueId";
    public static final String PATCH_ID = "id";
    public static final String PATCH_NAME = "name";
    public static final String PATCH_CONTENT = "content";
    public static final String PATCH_CREATOR_NAME = "creatorName";
    public static final String PATCH_CREATED_DATE = "createdDate";

    public static final Label ISSUECOMMENT = Label.label("JiraIssueComment");
    public static final String ISSUECOMMENT_ID = "id";
    public static final String ISSUECOMMENT_BODY = "body";
    public static final String ISSUECOMMENT_CREATOR_NAME = "creatorName";
    public static final String ISSUECOMMENT_UPDATER_NAME = "updaterName";
    public static final String ISSUECOMMENT_CREATED_DATE = "createdDate";
    public static final String ISSUECOMMENT_UPDATED_DATE = "updatedDate";

    public static final Label ISSUEUSER = Label.label("JiraIssueUser");
    public static final String ISSUEUSER_NAME = "name";
    public static final String ISSUEUSER_EMAIL_ADDRESS = "emailAddress";
    public static final String ISSUEUSER_DISPLAY_NAME = "displayName";
    public static final String ISSUEUSER_ACTIVE = "active";

    private static final RelationshipType HAVE_PATCH = RelationshipType.withName("jira_have_patch");
    private static final RelationshipType HAVE_ISSUE_COMMENT = RelationshipType.withName("jira_have_issue_comment");
    private static final RelationshipType ISSUE_DUPLICATE = RelationshipType.withName("jira_issue_duplicate");
    private static final RelationshipType IS_ASSIGNEE_OF_ISSUE = RelationshipType.withName("jira_is_assignee_of_issue");
    private static final RelationshipType IS_CREATOR_OF_ISSUE = RelationshipType.withName("jira_is_creator_of_issue");
    private static final RelationshipType IS_REPORTER_OF_ISSUE = RelationshipType.withName("jira_is_reporter_of_issue");
    private static final RelationshipType IS_CREATOR_OF_ISSUECOMMENT = RelationshipType.withName("jira_is_creator_of_issueComment");
    private static final RelationshipType IS_UPDATER_OF_ISSUECOMMENT = RelationshipType.withName("jira_is_updater_of_issueComment");
    private static final RelationshipType IS_CREATOR_OF_PATCH = RelationshipType.withName("jira_is_creator_of_patch");

    private Map<String, Node> userNodeMap = new HashMap<>();
    private List<String> duplicateList = new ArrayList<>();// "a b"代表a指向b
    private Map<String, Node> issueNodeMap = new HashMap<>();
    private Map<String, Node> patchNodeMap = new HashMap<>();

    @Override
    public void extraction() {
        GraphDatabaseService db = this.getDb();
        File issuesFolder = new File(this.getDataDir());

        for (File oneIssueFolder : issuesFolder.listFiles()) {
            //System.out.println(oneIssueFolder);
            if (oneIssueFolder.isDirectory()) {
                for (File issueFileOrPatchesFolder : oneIssueFolder.listFiles()) {
                    //System.out.println(issueFileOrPatchesFolder);
                    //String fileName = issueFileOrPatchesFolder.getName();
                    //System.out.println(fileName);
                    if (issueFileOrPatchesFolder.isDirectory()) {
                        for (File issueFileOrPatchesFolder2 : issueFileOrPatchesFolder.listFiles()) {
                            String fileName = issueFileOrPatchesFolder2.getName();
                            //System.out.println(fileName);
                            if (fileName.endsWith(".json")) {
                                try (Transaction tx = db.beginTx()) {
                                    try {
                                        jsonHandler(issueFileOrPatchesFolder2);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    tx.success();
                                }
                            }
                        }
                    }
                }
            }

        }
        //System.out.println("json文件处理完毕.");

        for (File oneIssueFolder : issuesFolder.listFiles()) {
            if (oneIssueFolder.isDirectory()) {
                for (File issueFileOrPatchesFolder : oneIssueFolder.listFiles()) {
                    //String fileName = issueFileOrPatchesFolder.getName();
                    //System.out.println(fileName);
                    if (issueFileOrPatchesFolder.isDirectory()) {
                        for (File issueFileOrPatchesFolder2 : issueFileOrPatchesFolder.listFiles()) {
                            String fileName = issueFileOrPatchesFolder2.getName();
                            //System.out.println(fileName);
                            if (fileName.equals("Patchs")) {
                                for (File onePatchFolder : issueFileOrPatchesFolder2.listFiles()) {
                                    String onePatchFolderName = onePatchFolder.getName();
                                    if (onePatchFolderName.endsWith(".patch")) {
                                        String patchId = onePatchFolder.getName();
                                        //System.out.println(patchId);
                                        //for (File patchFile : onePatchFolder.listFiles()) {
                                        //   System.out.println(fileName);
                                        if (patchNodeMap.containsKey(patchId))
                                            try {
                                                try (Transaction tx = db.beginTx()) {
                                                    patchNodeMap.get(patchId).setProperty(JiraExtractor.PATCH_CONTENT, FileUtils.readFileToString(onePatchFolder, "utf-8"));
                                                    tx.success();
                                                }
                                            } catch (IOException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                        //}
                                    }

                                }
                            }
                            //System.out.println(filename);
                        }
                    }

                }
            }

        }
        //System.out.println("patch文件处理完毕.");

        try (Transaction tx = db.beginTx()) {
            // 建立DUPLICATE关联
            for (String line : duplicateList) {
                String[] eles = line.trim().split("\\s+");
                String id1 = eles[0];
                String id2 = eles[1];
                if (issueNodeMap.containsKey(id1) && issueNodeMap.containsKey(id2))
                    issueNodeMap.get(id1).createRelationshipTo(issueNodeMap.get(id2), JiraExtractor.ISSUE_DUPLICATE);
            }
            tx.success();
        }
    }

    private void jsonHandler(File issueFile) throws JSONException {
        String jsonContent = null;
        try {
            jsonContent = FileUtils.readFileToString(issueFile, "utf-8");
            //System.out.println(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonContent == null) {
            //System.out.println("json is empty");
            return;
        }

        // 建立Issue实体
        IssueInfo issueInfo = getIssueInfo(jsonContent);
        Node node = this.getDb().createNode();
        issueNodeMap.put(issueInfo.getIssueId(), node);
        JiraUtils.createIssueNode(issueInfo, node);

        // 建立用户实体
        if (!TextUtils.isEmpty(jsonContent)) {
            JSONObject fields = new JSONObject(jsonContent).getJSONObject("fields");
            Pair<String, Node> assignee = createUserNode(fields, "assignee");
            Pair<String, Node> creator = createUserNode(fields, "creator");
            Pair<String, Node> reporter = createUserNode(fields, "reporter");
            // 建立用户实体与Issue实体之间的关联
            if (assignee != null) {
                node.setProperty(JiraExtractor.ISSUE_ASSIGNEE_NAME, assignee.getLeft());
                assignee.getRight().createRelationshipTo(node, JiraExtractor.IS_ASSIGNEE_OF_ISSUE);
            }
            if (creator != null) {
                node.setProperty(JiraExtractor.ISSUE_CREATOR_NAME, creator.getLeft());
                creator.getRight().createRelationshipTo(node, JiraExtractor.IS_CREATOR_OF_ISSUE);
            }
            if (reporter != null) {
                node.setProperty(JiraExtractor.ISSUE_REPORTER_NAME, reporter.getLeft());
                reporter.getRight().createRelationshipTo(node, JiraExtractor.IS_REPORTER_OF_ISSUE);
            }

            // 记录DUPLICATE关系
            JSONArray jsonIssueLinks = fields.getJSONArray("issuelinks");
            int issueLinkNum = jsonIssueLinks.length();
            for (int i = 0; i < issueLinkNum; i++) {
                JSONObject jsonIssueLink = jsonIssueLinks.getJSONObject(i);
                if (jsonIssueLink.has("inwardIssue")) {
                    String linkIssueId = jsonIssueLink.getJSONObject("inwardIssue").getString("id");
                    duplicateList.add(linkIssueId + " " + issueInfo.getIssueId());
                }
            }

            // 建立评论实体并关联到ISSUE
            JSONArray jsonCommentArr;
            if (!fields.isNull("comment")) {
                jsonCommentArr = fields.getJSONObject("comment").optJSONArray("comments");
                if (jsonCommentArr != null) {
                    int len = jsonCommentArr.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject jsonComment = jsonCommentArr.getJSONObject(i);
                        String id = jsonComment.optString("id");
                        String body = jsonComment.optString("body");
                        Pair<String, Node> author = createUserNode(jsonComment, "author");
                        Pair<String, Node> updateAuthor = createUserNode(jsonComment, "updateAuthor");
                        String createdDate = jsonComment.optString("created");
                        String updatedDate = jsonComment.optString("updated");
                        if (author == null)
                            continue;
                        IssueCommentInfo comment = new IssueCommentInfo(id, body, author.getLeft(), updateAuthor.getLeft(), createdDate, updatedDate);
                        Node commentNode = this.getDb().createNode();
                        JiraUtils.createIssueCommentNode(comment, commentNode);
                        node.createRelationshipTo(commentNode, JiraExtractor.HAVE_ISSUE_COMMENT);
                        commentNode.setProperty(JiraExtractor.ISSUECOMMENT_CREATOR_NAME, author.getLeft());
                        author.getRight().createRelationshipTo(commentNode, JiraExtractor.IS_CREATOR_OF_ISSUECOMMENT);
                        if (updateAuthor != null) {
                            commentNode.setProperty(JiraExtractor.ISSUECOMMENT_UPDATER_NAME, updateAuthor.getLeft());
                            updateAuthor.getRight().createRelationshipTo(commentNode, JiraExtractor.IS_UPDATER_OF_ISSUECOMMENT);
                        }
                    }
                }
            }

            // 建立补丁实体并关联到ISSUE
            JSONArray jsonHistoryArr;
            JSONObject root = new JSONObject(jsonContent);
            if (!root.isNull("changelog")) {
                jsonHistoryArr = root.getJSONObject("changelog").optJSONArray("histories");
                if (jsonHistoryArr != null) {
                    int hisNum = jsonHistoryArr.length();
                    for (int i = 0; i < hisNum; i++) {
                        JSONObject history = jsonHistoryArr.getJSONObject(i);
                        JSONArray items = history.optJSONArray("items");
                        if (items == null)
                            continue;
                        int itemNum = items.length();
                        for (int j = 0; j < itemNum; j++) {
                            JSONObject item = items.getJSONObject(j);
                            String to = item.optString("to");
                            String toString = item.optString("toString");
                            // not a patch
                            if (!to.matches("^\\d{1,19}$") || !toString.endsWith(".patch")) {
                                continue;
                            }
                            String patchName;
                            patchName = toString;
                            Pair<String, Node> author = createUserNode(history, "author");
                            String createdDate = history.optString("created");
                            if (createdDate == null)
                                createdDate = "";

                            PatchInfo patchInfo = new PatchInfo(to, patchName, "", createdDate);
                            Node patchNode = this.getDb().createNode();
                            patchNodeMap.put(to, patchNode);
                            JiraUtils.createPatchNode(patchInfo, patchNode);
                            node.createRelationshipTo(patchNode, JiraExtractor.HAVE_PATCH);
                            if (author != null) {
                                patchNode.setProperty(JiraExtractor.PATCH_CREATOR_NAME, author.getLeft());
                                author.getRight().createRelationshipTo(patchNode, IS_CREATOR_OF_PATCH);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * 解析.json文件，返回IssueInfo。 返回的IssueInfo中不包含crearorName, assigneeName,
     * reporterName
     */
    private IssueInfo getIssueInfo(String jsonContent) throws JSONException {

        IssueInfo issueInfo = new IssueInfo();
        if (!TextUtils.isEmpty(jsonContent)) {
            JSONObject root = new JSONObject(jsonContent);
            String issueId = root.getString("id");
            String issueName = root.getString("key");

            JSONObject fields = root.getJSONObject("fields");

            String type = "";
            if (!fields.isNull("issuetype")) {
                type = fields.getJSONObject("issuetype").optString("name");
            }

            String fixVersions = getVersions(fields, "fixVersions");
            String versions = getVersions(fields, "versions");
            String resolution = "";
            if (!fields.isNull("resolution")) {
                resolution = fields.getJSONObject("resolution").optString("name");
            }

            String priority = "";
            if (!fields.isNull("priority")) {
                priority = fields.getJSONObject("priority").optString("name");
            }

            String status = "";
            if (!fields.isNull("status")) {
                status = fields.getJSONObject("status").optString("name");
            }

            String description = fields.optString("description");
            String summary = fields.optString("summary");

            String resolutionDate = fields.optString("resolutiondate");
            String createDate = fields.optString("created");
            String updateDate = fields.optString("updated");

            // labels
            String labels = "";
            JSONArray jsonLabels = fields.optJSONArray("labels");
            if (jsonLabels != null) {
                int len = jsonLabels.length();
                for (int i = 0; i < len; i++) {
                    String label = jsonLabels.optString(i);
                    labels += label;
                    if (i != len - 1) {
                        labels += ",";
                    }
                }
            }

            // components
            String components = "";
            JSONArray jsonComponents = fields.optJSONArray("components");
            if (jsonComponents != null) {
                int len = jsonComponents.length();
                for (int i = 0; i < len; i++) {
                    String component = jsonComponents.getJSONObject(i).optString("name");
                    components += component;
                    if (i != len - 1) {
                        components += ",";
                    }
                }
            }

            issueInfo.setIssueId(issueId);
            issueInfo.setIssueName(issueName);
            issueInfo.setType(type);
            issueInfo.setFixVersions(fixVersions);
            issueInfo.setResolution(resolution);
            issueInfo.setResolutionDate(resolutionDate);
            issueInfo.setPriority(priority);
            issueInfo.setLabels(labels);
            issueInfo.setVersions(versions);
            issueInfo.setStatus(status);
            issueInfo.setComponents(components);
            issueInfo.setDescription(description);
            issueInfo.setSummary(summary);
            issueInfo.setCreatedDate(createDate);
            issueInfo.setUpdatedDate(updateDate);
        }


        return issueInfo;
    }

    private String getVersions(JSONObject jsonObj, String key) throws JSONException {
        String versions = "";
        JSONArray jsonVersions = jsonObj.optJSONArray(key);
        if (jsonVersions == null) {
            return versions;
        }

        int versionNum = jsonVersions.length();
        for (int i = 0; i < versionNum; i++) {
            JSONObject fixVersion = jsonVersions.getJSONObject(i);
            String version = fixVersion.optString("name");
            versions += version;

            if (i != versionNum - 1) {
                versions += ",";
            }
        }
        return versions;
    }

    private Pair<String, Node> createUserNode(JSONObject jsonObj, String key) throws JSONException {
        if (jsonObj.isNull(key)) {
            return null;
        }

        JSONObject userJsonObj = jsonObj.getJSONObject(key);
        String name = userJsonObj.optString("name");
        String emailAddress = userJsonObj.optString("emailAddress");
        String displayName = userJsonObj.optString("displayName");
        boolean active = userJsonObj.optBoolean("active");

        IssueUserInfo user = new IssueUserInfo(name, EmailAddressDecoder.decode(emailAddress), displayName, active);
        if (userNodeMap.containsKey(name))
            return new ImmutablePair<>(name, userNodeMap.get(name));
        Node node = this.getDb().createNode();
        JiraUtils.createIssueUserNode(user, node);
        userNodeMap.put(name, node);
        return new ImmutablePair<>(name, userNodeMap.get(name));
    }

}
