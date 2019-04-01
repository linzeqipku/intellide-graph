package cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Schema {

    public static Map<String, Pair<String, String>> relations = new HashMap<>();

    private void readFile(){
         try {
            InputStream in = Schema.class.getResourceAsStream("/nli/schema.json");
            String content = StringUtils.join(IOUtils.readLines(in, "utf-8"), "\n");
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); ++i){
                JSONObject object = array.getJSONObject(i);
                String name = object.getString("relation");
                String left = object.getString("left");
                String right = object.getString("right");
                relations.put(name, Pair.of(left, right));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
