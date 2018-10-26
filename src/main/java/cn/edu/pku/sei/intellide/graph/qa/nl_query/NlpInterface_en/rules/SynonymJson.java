package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.rules;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.config.Config;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphSchema;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SynonymJson {
    public static JSONObject JsonObj = null;
    public static GraphSchema graphSchema;
    public static Map<String,Set<String>> nodedict = new HashMap<>();
    public static Map<String,Set<String>> edgedict = new HashMap<>();
    public static Map<String,Set<String>> attributedict = new HashMap<>();
    static {
        try {
            readJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void readJson() throws JSONException {
        String lines = "";
        try {
            lines = FileUtils.readFileToString(new File("C://config//Synonym.json"));
            //lines = FileUtils.readFileToString(new File("/data/Synonym.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObj = new JSONObject(lines);
        for (int i = 0; i < JsonObj.getJSONArray("node").length(); i++){
            JSONObject obj = JsonObj.getJSONArray("node").getJSONObject(i);
            JSONArray nodeArr = obj.getJSONArray("nodeName");
            JSONArray simArr = obj.getJSONArray("similar");
            for (int j = 0; j <  nodeArr.length(); j++){
                for (int k = 0; k <  simArr.length(); k++){
                    String nodeStr = nodeArr.getString(j);
                    String simStr = simArr.getString(k);
                    if (!nodedict.keySet().contains(simArr.getString(k))){
                        nodedict.put(simStr,new HashSet<>());
                    }
                    nodedict.get(simStr).add(nodeStr);
                }
            }
        }
        for (int i = 0; i < JsonObj.getJSONArray("relation").length(); i++){
            JSONObject obj = JsonObj.getJSONArray("relation").getJSONObject(i);
            JSONArray edgeArr = obj.getJSONArray("relationName");
            JSONArray simArr = obj.getJSONArray("similar");
            for (int j = 0; j <  edgeArr.length(); j++){
                for (int k = 0; k <  simArr.length(); k++){
                    String edgeStr = edgeArr.getString(j);
                    String simStr = simArr.getString(k);
                    if (!edgedict.keySet().contains(simArr.getString(k))){
                        edgedict.put(simStr,new HashSet<>());
                    }
                    edgedict.get(simStr).add(edgeStr);
                }
            }
        }
        for (int i = 0; i < JsonObj.getJSONArray("attribute").length(); i++){
            JSONObject obj = JsonObj.getJSONArray("attribute").getJSONObject(i);
            JSONArray edgeArr = obj.getJSONArray("attributeName");
            JSONArray simArr = obj.getJSONArray("similar");
            for (int j = 0; j <  edgeArr.length(); j++){
                for (int k = 0; k <  simArr.length(); k++){
                    String edgeStr = edgeArr.getString(j);
                    String simStr = simArr.getString(k);
                    if (!attributedict.keySet().contains(simArr.getString(k))){
                        attributedict.put(simStr,new HashSet<>());
                    }
                    attributedict.get(simStr).add(edgeStr);
                }
            }
        }
    }


}
