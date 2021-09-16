package cn.edu.pku.sei.intellide.graph.extraction.json;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.io.IOException;

public class RequirementExtractor extends KnowledgeExtractor {
    public static final Label IR = Label.label("IR");
    public static final Label SR = Label.label("SR");
    public static final Label AR = Label.label("AR");

    public static final String BUSINESS_NO = "business_no";
    public static final String NAME = "name";
    public static final String DETAIL_DESC = "detail_desc";
    public static final String DETAILS_URL = "details_url";

    public static RelationshipType PARENT = RelationshipType.withName("parent");
    public static RelationshipType DESIGNER = RelationshipType.withName("designer");



    @Override
    public void extraction() {
        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[]{"json"}, true)) {
            System.out.println(file.getName());
            String jsonContent = null;
            try {
                jsonContent = FileUtils.readFileToString(file, "utf-8");
                //System.out.println(jsonContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (jsonContent == null){
                continue;
            }
            try {
                JSONArray ReqArray = new JSONArray(jsonContent);
                try(Transaction tx = this.getDb().beginTx()) {
                    for (int i = 0; i < ReqArray.length(); i++) {
                        JSONObject req = ReqArray.getJSONObject(i).getJSONObject("_source");
                        Node node = this.getDb().createNode();
                        // 建立需求实体
                        createReqNode(req, node);
                        // 建立需求到Person的链接关系
                        // createReq2PersonRelationship(node, req.getString("designer"), RequirementExtractor.DESIGNER);

                    }
                    tx.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void createReqNode(JSONObject reqJson, Node node) throws JSONException {
        String reqType = reqJson.getString("requirement_type");
        String id = reqJson.getString("business_no");
        Node parentNode;
        // TODO: 要不要再加一个Requirement标签
        // TODO:
        switch (reqType) {
            case "IR":
                node.addLabel(RequirementExtractor.IR);
                break;
            case "SR Node" :
                node.addLabel(RequirementExtractor.SR);
                parentNode = findParentNode(id, false);
                if (parentNode != null) node.createRelationshipTo(parentNode, RequirementExtractor.PARENT);
                break;
            case "AR Node" :
                node.addLabel(RequirementExtractor.AR);
                parentNode = findParentNode(id, true);
                if (parentNode != null) node.createRelationshipTo(parentNode, RequirementExtractor.PARENT);
                break;
        }
        node.setProperty(RequirementExtractor.BUSINESS_NO, reqJson.getString("business_no"));
        node.setProperty(RequirementExtractor.NAME, reqJson.getString("name"));
        node.setProperty(RequirementExtractor.DETAIL_DESC, reqJson.getString("detail_desc"));
        node.setProperty(RequirementExtractor.DETAILS_URL, reqJson.getString("details_url"));

    }

    public Node findParentNode(String id, boolean isAR) {
        Node parentNode = null;
        Label label;
        if (isAR) label = RequirementExtractor.SR;
        else label = RequirementExtractor.IR;
        id = id.substring(id.indexOf(".")+1, id.lastIndexOf("."));
        parentNode = this.getDb().findNode(label, RequirementExtractor.BUSINESS_NO, id);
        if (parentNode == null && id.lastIndexOf(".") > 0){
            id = id.substring(0, id.lastIndexOf("."));
            parentNode = this.getDb().findNode(label, "business_no", id);
        }
        return parentNode;
    }

    public void createReq2PersonRelationship(Node reqNode, String persons, RelationshipType relationshipType) {
        if (persons.contains(",")) {
            String[] persons_id = persons.split(",");
            for(String personId : persons_id){
                createReq2OnePersonRelationship(reqNode, personId, relationshipType);
            }
        }
        else {
            createReq2OnePersonRelationship(reqNode, persons, relationshipType);
        }
    }

    public void createReq2OnePersonRelationship(Node reqNode, String personId, RelationshipType relationshipType)  {
        if (personId.length() == 0)    return;
        if (personId.length() == 9){
            personId = personId.substring(1);
        }
        Node personNode = this.getDb().findNode(PersonExtractor.PERSON, PersonExtractor.ID, personId);
        if (personNode == null){
            personNode = this.getDb().createNode();
            personNode.addLabel(PersonExtractor.PERSON);
            personNode.setProperty("name", "");
            personNode.setProperty("id", personId);
        }
        reqNode.createRelationshipTo(personNode, relationshipType);
    }
}
