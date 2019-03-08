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
        labelMap.put("Classes",JavaExtractor.CLASS.name());
        labelMap.put("Methods",JavaExtractor.METHOD.name());
        labelMap.put("Fields",JavaExtractor.FIELD.name());
    }

    private static String ID = "Id";
    private static String LINKS = "Links";
    private static String SRC = "Src";
    private static String DST = "Dst";
    private static String TYPE = "Type";

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

        Map<String, Long> idMap = new HashMap<>();

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
                    String id = item.getString(ID);
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
                if (!(idMap.containsKey(link.getString(SRC))&&idMap.containsKey(link.getString(DST)))){
                    continue;
                }
                this.getInserter().createRelationship(idMap.get(link.getString(SRC)),
                        idMap.get(link.getString(DST)),
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
                map.put(headLowercase(key), jsonObject.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private static String headLowercase(String s){
        if (s==null){
            return s;
        }
        return s.substring(0,1).toLowerCase()+s.substring(1);
    }

}
