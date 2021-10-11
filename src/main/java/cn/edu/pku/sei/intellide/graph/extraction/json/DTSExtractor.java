package cn.edu.pku.sei.intellide.graph.extraction.json;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;

public class DTSExtractor extends KnowledgeExtractor {

    public static final Label DTS = Label.label("DTS");

    public static final String DTS_NO = "dts_no";       // 问题单编号
    public static final String BRIEF_DESC = "brief_desc";       // 简要描述

    public static RelationshipType CREATOR = RelationshipType.withName("creator");
    public static RelationshipType HANDLER = RelationshipType.withName("handler");
    // TODO
    public static RelationshipType SUBMITTER = RelationshipType.withName("submitter");

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
                JSONArray DTSArray = new JSONArray(jsonContent);
                try(Transaction tx = this.getDb().beginTx()) {
                    for (int i = 0; i < DTSArray.length(); i++) {
                        JSONObject DTS = DTSArray.getJSONObject(i).getJSONObject("_source");
                        Node node = this.getDb().createNode();
                        // 建立DTS实体
                        createDTSNode(DTS, node);
                        // 建立DTS到Person的链接关系
                        createDTS2PersonRelationship(node, DTS.getString("creator"), DTSExtractor.CREATOR);
                        createDTS2PersonRelationship(node, DTS.getString("current_handler"), DTSExtractor.HANDLER);
                    }
                    tx.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void createDTSNode(JSONObject DTS, Node node) throws JSONException {
        node.addLabel(DTSExtractor.DTS);
        node.setProperty(DTSExtractor.DTS_NO, DTS.getString("dts_no"));
        node.setProperty(DTSExtractor.BRIEF_DESC, DTS.getString("brief_desc"));
    }


    public void createDTS2PersonRelationship(Node dtsNode, String personInfo, RelationshipType relationshipType)  {
        if (personInfo.length() == 0)    return;
        // 姓名 工号
        String[] person = personInfo.split("\\s");
        if (person.length != 2 || person[1].length() != 8)     return;
        Node personNode = this.getDb().findNode(PersonExtractor.PERSON, PersonExtractor.ID, person[1]);
        if (personNode == null){
            personNode = this.getDb().createNode();
            personNode.addLabel(PersonExtractor.PERSON);
            personNode.setProperty(PersonExtractor.NAME, person[0]);
            personNode.setProperty(PersonExtractor.ID, person[1]);
        }
        dtsNode.createRelationshipTo(personNode, relationshipType);
    }



}
