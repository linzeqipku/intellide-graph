package cn.edu.pku.sei.intellide.graph.extraction.json;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MRExtractor extends KnowledgeExtractor {

    public static final Label MR = Label.label("MR");
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String URL = "url";
    public static final RelationshipType REFERENCE = RelationshipType.withName("reference");
    public static final RelationshipType AUTHOR = RelationshipType.withName("author");
    public static final RelationshipType ASSIGNEE = RelationshipType.withName("assignee");

    private static String DTSRegex = "DTS[A-Z0-9]+";
    private static String ARRegex = "AR\\.[A-Za-z0-9\\.]+";

    private static Pattern dtsPattern;
    private static Pattern arPattern;

    static {
        dtsPattern = Pattern.compile(DTSRegex);
        arPattern = Pattern.compile(ARRegex);
    }


    @Override
    public void extraction() {
        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[]{"json"}, true)) {
            String jsonContent = null;
            try {
                jsonContent = FileUtils.readFileToString(file, "utf-8");
                //System.out.println(jsonContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (jsonContent == null) {
                continue;
            }
            try {
                JSONArray MRArray = new JSONArray(jsonContent);
                try(Transaction tx = this.getDb().beginTx()) {
                    for (int i = 0; i < MRArray.length(); i++) {
                        JSONObject MR = MRArray.getJSONObject(i).getJSONObject("_source");
                        Node node = this.getDb().createNode();
                        // 建立MR实体
                        createMRNode(MR, node);
                        // 建立MR到DTS/AR的reference关系
                        createMRRelationship(MR.getString("content"), node);
                        // 建立MR到Person的链接关系
                        createMR2PersonRelationship(node, MR.getString("author_id"), MRExtractor.AUTHOR);
                        createMR2PersonRelationship(node, MR.getString("assignee_id"), MRExtractor.ASSIGNEE);
                    }
                    tx.success();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void createMRNode(JSONObject MR, Node node) throws JSONException {
        node.addLabel(MRExtractor.MR);
        node.setProperty(MRExtractor.ID, MR.getString("id"));
        node.setProperty(MRExtractor.TITLE, MR.getString("title"));
        node.setProperty(MRExtractor.CONTENT, MR.getString("content"));
        node.setProperty(MRExtractor.URL, MR.getString("merge_request_url"));
        //System.out.println("create MR Node: " + MR.getString("id"));
    }

    // 建立MR到DTS或AR的关联关系
    public void createMRRelationship(String content, Node mrNode) {
        Matcher dtsMatcher = dtsPattern.matcher(content);
        while(dtsMatcher.find()){
            String dts_no = content.substring(dtsMatcher.start(), dtsMatcher.end());
            //System.out.println(dts_no);
            Node dtsNode = this.getDb().findNode(DTSExtractor.DTS, DTSExtractor.DTS_NO, dts_no);
            if(dtsNode != null) {
                //System.out.println(dtsNode.getProperty("brief_desc"));
                mrNode.createRelationshipTo(dtsNode, MRExtractor.REFERENCE);
            }
        }

        Matcher arMatcher = arPattern.matcher(content);
        while(arMatcher.find()){
            String ar_no = content.substring(arMatcher.start(), arMatcher.end());
            Node arNode = this.getDb().findNode(RequirementExtractor.AR, RequirementExtractor.BUSINESS_NO, ar_no);
            if(arNode != null) {
                mrNode.createRelationshipTo(arNode, MRExtractor.REFERENCE);
                //System.out.println(ar_no);
            }
        }
    }

    // TODO: person name
    public void createMR2PersonRelationship(Node mrNode, String personId, RelationshipType relationshipType)  {
        if (personId.length() == 0)    return;
        if (personId.length() == 9){
            personId = personId.substring(1);
        }
        Node personNode = this.getDb().findNode(PersonExtractor.PERSON, PersonExtractor.ID, personId);
        if (personNode == null){
            personNode = this.getDb().createNode();
            personNode.addLabel(PersonExtractor.PERSON);
            personNode.setProperty(PersonExtractor.NAME, "");
            personNode.setProperty(PersonExtractor.ID, personId);
        }
        mrNode.createRelationshipTo(personNode, relationshipType);
    }

}
