package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.rules;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CnToEnDirectory;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphSchema;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SynonymJson {

    private static Map<String, SynonymJson> instances = new HashMap<>();

    public JSONObject JsonObj = null;
    public Map<String, Set<String>> nodedict = new HashMap<>();
    public Map<String, Set<String>> edgedict = new HashMap<>();
    public Map<String, Set<String>> attributedict = new HashMap<>();

    public SynonymJson(String languageIdentifier) {
        String content = "";
        try {
            InputStream in = CnToEnDirectory.class.getResourceAsStream("/nli/synonym/" + languageIdentifier + ".json");
            content = StringUtils.join(IOUtils.readLines(in, "utf-8"), "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JsonObj = new JSONObject(content);
            for (int i = 0; i < JsonObj.getJSONArray("node").length(); i++) {
                JSONObject obj = JsonObj.getJSONArray("node").getJSONObject(i);
                JSONArray nodeArr = obj.getJSONArray("nodeName");
                JSONArray simArr = obj.getJSONArray("similar");
                for (int j = 0; j < nodeArr.length(); j++) {
                    for (int k = 0; k < simArr.length(); k++) {
                        String nodeStr = nodeArr.getString(j);
                        String simStr = simArr.getString(k);
                        if (!nodedict.keySet().contains(simArr.getString(k))) {
                            nodedict.put(simStr, new HashSet<>());
                        }
                        nodedict.get(simStr).add(nodeStr);
                    }
                }
            }
            for (int i = 0; i < JsonObj.getJSONArray("relation").length(); i++) {
                JSONObject obj = JsonObj.getJSONArray("relation").getJSONObject(i);
                JSONArray edgeArr = obj.getJSONArray("relationName");
                JSONArray simArr = obj.getJSONArray("similar");
                for (int j = 0; j < edgeArr.length(); j++) {
                    for (int k = 0; k < simArr.length(); k++) {
                        String edgeStr = edgeArr.getString(j);
                        String simStr = simArr.getString(k);
                        if (!edgedict.keySet().contains(simArr.getString(k))) {
                            edgedict.put(simStr, new HashSet<>());
                        }
                        edgedict.get(simStr).add(edgeStr);
                    }
                }
            }
            for (int i = 0; i < JsonObj.getJSONArray("attribute").length(); i++) {
                JSONObject obj = JsonObj.getJSONArray("attribute").getJSONObject(i);
                JSONArray edgeArr = obj.getJSONArray("attributeName");
                JSONArray simArr = obj.getJSONArray("similar");
                for (int j = 0; j < edgeArr.length(); j++) {
                    for (int k = 0; k < simArr.length(); k++) {
                        String edgeStr = edgeArr.getString(j);
                        String simStr = simArr.getString(k);
                        if (!attributedict.keySet().contains(simArr.getString(k))) {
                            attributedict.put(simStr, new HashSet<>());
                        }
                        attributedict.get(simStr).add(edgeStr);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static SynonymJson getInstance(String languageIdentifier) {
        SynonymJson instance = instances.get(languageIdentifier);
        if (instance != null) {
            return instance;
        }
        instance = new SynonymJson(languageIdentifier);
        instances.put(languageIdentifier, instance);
        return instance;
    }


}
