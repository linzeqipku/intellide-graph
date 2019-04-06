package cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing.SemanticParser;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityMatcher {
    private Map<String, Atom> triggers = new HashMap<>(100);
    private static EntityMatcher matcher = new EntityMatcher();

    private EntityMatcher(){
        readFile();

    }

    public static EntityMatcher getInstance(){
        return matcher;
    }

    private void readFile(){
        try {
            InputStream in = EntityMatcher.class.getResourceAsStream("/nli/lexmap.json");
            String content = StringUtils.join(IOUtils.readLines(in, "utf-8"), "\n");
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); ++i){
                JSONObject object = array.getJSONObject(i);
                JSONArray keys = object.getJSONArray("lexicon");
                JSONObject val = object.getJSONObject("atom");
                Atom atom = AtomFactory.fromJson(val);
                for (int j = 0; j < keys.length(); ++j) {
                    String key = keys.getString(j);
                    triggers.put(key, atom);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Atom> getEntities(String queryString){
        String[] query = queryString.trim().split("\\s+");
        List<Atom> entities = new ArrayList<>(query.length);
        for (String token: query){
            Atom atom = triggers.get(token);
            if (atom != null){
                entities.add(atom);
            }
        }
        return entities;
    }

    public static void main(String[] args){
        EntityMatcher matcher = EntityMatcher.getInstance();
        List<Atom> seq = matcher.getEntities("list method extends IndexReader");
        for (Atom atom: seq)
            System.out.println(atom);
        SemanticParser parser = new SemanticParser();
        parser.parse(seq);
    }
}


