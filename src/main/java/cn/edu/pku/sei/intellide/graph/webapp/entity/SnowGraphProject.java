package cn.edu.pku.sei.intellide.graph.webapp.entity;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config.Config;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnowGraphProject {
    private final String name;
    private final String description;

    //public Map<String,String> projectMap = new HashMap<String,String>();
    public SnowGraphProject(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static List<SnowGraphProject> getProjectList() throws IOException, JSONException {
        List<SnowGraphProject> projectList = new ArrayList<>();
        //File jsonFile = new File("project.json");
        File jsonFile = ResourceUtils.getFile(Config.class.getResource("/").getPath()+"\\project-final.json");
        String json = FileUtils.readFileToString(jsonFile);
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i<jsonArray.length();i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            String name = jobj.getString("name");
            String description = jobj.getString("description");
            //System.out.println(name);
            projectList.add(new SnowGraphProject(name,description));
        }
        return projectList;

    }

    public  static Map<String,GraphDatabaseService> getDbMap(String graphRootDir) throws IOException, JSONException {
        Map<String, GraphDatabaseService> dbMap1 = new HashMap<>();
        File jsonFile = ResourceUtils.getFile(Config.class.getResource("/").getPath() + "\\project-final.json");
        String json = FileUtils.readFileToString(jsonFile);
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            String name = jobj.getString("name");
            //System.out.println(name);
            //System.out.println(graphRootDir+name);
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphRootDir + name));

            dbMap1.put(name, db);
            //System.out.println("okm");
        }

        return dbMap1;
    }

    public static void main(String args[]) throws IOException, JSONException {
        //getProjectList();
        System.out.println("OK");
    }
}
