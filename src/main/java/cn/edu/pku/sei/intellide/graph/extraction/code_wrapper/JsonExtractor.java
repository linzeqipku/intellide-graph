package cn.edu.pku.sei.intellide.graph.extraction.code_wrapper;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonExtractor extends KnowledgeExtractor{

    private static Map<String, String> labelMap = new HashMap<>();

    static {
        labelMap.put("classes",JavaExtractor.CLASS.name());
        labelMap.put("methods",JavaExtractor.METHOD.name());
        labelMap.put("fields",JavaExtractor.FIELD.name());
    }

    private static String ID = "id";
    private static String LINKS = "links";
    private static String SRC = "src";
    private static String DST = "dst";
    private static String TYPE = "type";

    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {

        String jsonStr = null;
        try {
            jsonStr = FileUtils.readFileToString(new File(this.getDataDir()), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonStr == null){
            return;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException e) {
            return;
        }

        Map<Integer, Long> idMap = new HashMap<>();

        for (Iterator it = jsonObject.keys(); it.hasNext(); ) {
            String key = (String) it.next();
            if (key.equals(LINKS)) {
                continue;
            }
            Label label = Label.label(labelMap.get(key));
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject item = jsonArray.getJSONObject(i);
                    int id = item.getInt(ID);
                    item.remove(ID);
                    long nodeId = this.getInserter().createNode(jsonObjectToMap(item), label);
                    idMap.put(id, nodeId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONArray links = jsonObject.getJSONArray(LINKS);
            for (int i = 0;i < links.length(); i++){
                JSONObject link = links.getJSONObject(i);
                this.getInserter().createRelationship(idMap.get(link.getInt(SRC)),
                        idMap.get(link.getInt(DST)),
                        RelationshipType.withName(link.getString(TYPE)),
                        new HashMap<>());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Map<String, Object> jsonObjectToMap(JSONObject jsonObject){
        Map<String, Object> map =new HashMap<>();
        for (Iterator it = jsonObject.keys(); it.hasNext(); ) {
            String key = (String) it.next();
            try {
                map.put(key, jsonObject.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
