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

    public static final String business_no = "business_no";
    public static final String name = "name";
    public static final String detail_desc = "detail_desc";
    public static final String details_url = "details_url";

    public static RelationshipType PARENT = RelationshipType.withName("parent");
    public static RelationshipType DESIGNER = RelationshipType.withName("designer");



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
                        createReq2PersonRelationship(node, req.getString("designer"), RequirementExtractor.DESIGNER);
                        // createDTS2PersonRelationship(node, DTS.getString("current_handler"), DTSExtractor.HANDLER);
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
        // TODO: 要不要再加一个Requirement的标签
        switch (reqType) {
            case "IR Node":
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
        node.setProperty(RequirementExtractor.business_no, reqJson.getString("business_no"));
        node.setProperty(RequirementExtractor.name, reqJson.getString("name"));
        node.setProperty(RequirementExtractor.detail_desc, reqJson.getString("details_desc"));
        node.setProperty(RequirementExtractor.details_url, reqJson.getString("details_url"));

    }

    public Node findParentNode(String id, boolean isAR) {
        Node parentNode = null;
        Label label;
        if (isAR) label = RequirementExtractor.SR;
        else label = RequirementExtractor.IR;
        id = id.substring(id.indexOf(".")+1, id.lastIndexOf("."));
        parentNode = this.getDb().findNode(label, "business_no", id);
        if (parentNode == null){
            id = id.substring(0, id.lastIndexOf("."));
            parentNode = this.getDb().findNode(label, "business_no", id);
        }
        return parentNode;
    }

    public void createReq2PersonRelationship(Node reqNode, String personId, RelationshipType relationshipType)  {
        if (personId.length() == 0)    return;
        if (personId.length() == 9){
            personId = personId.substring(1);
        }
        Node personNode = this.getDb().findNode(PersonExtractor.PERSON, "id", personId);
        if (personNode == null){
            personNode = this.getDb().createNode();
            personNode.addLabel(PersonExtractor.PERSON);
            personNode.setProperty("name", "");
            personNode.setProperty("id", personId);
        }
        reqNode.createRelationshipTo(personNode, relationshipType);
    }
}
