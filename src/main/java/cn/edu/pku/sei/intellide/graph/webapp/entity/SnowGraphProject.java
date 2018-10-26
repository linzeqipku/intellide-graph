package cn.edu.pku.sei.intellide.graph.webapp.entity;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config.Config;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.cypher.internal.frontend.v2_3.ast.Pattern;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

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

    public static List<SnowGraphProject> getProjectList(String jsonPath) throws IOException, JSONException {
        List<SnowGraphProject> projectList = new ArrayList<>();
        //File jsonFile = new File("project.json");
        //File jsonFile = ResourceUtils.getFile(cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config.Config.class.getResource("/").getPath()+"/project-final.json");
        File jsonFile = ResourceUtils.getFile(jsonPath);
        //File jsonFile = ResourceUtils.getFile("/data/project-final.json");
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

    public  static Map<String,GraphDatabaseService> getDbMap(String graphRootDir,String jsonPath) throws IOException, JSONException {
        Map<String, GraphDatabaseService> dbMap1 = new LinkedHashMap<>();

        File jsonFile = ResourceUtils.getFile(jsonPath);
        String json = FileUtils.readFileToString(jsonFile);
        JSONArray jsonArray = new JSONArray(json);

        for(int i=0;i<jsonArray.length();i++){
            JSONObject jobj = jsonArray.getJSONObject(i);
            String name = jobj.getString("name");
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphRootDir + name));
            dbMap1.put(name,db);
        }
        /*File jsonFile = ResourceUtils.getFile(Config.class.getResource("/").getPath() + "\\project-final.json");

        String json = FileUtils.readFileToString(jsonFile);
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            String name = jobj.getString("name");*/
            //System.out.println(name);
            //System.out.println(graphRootDir+name);
            //GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphRootDir + "Lucene"));

            //dbMap1.put("Lucene", db);
            //System.out.println("okm");
        //}

        return dbMap1;
    }

    public static boolean isNlpSolver(String query){
        if(query.contains("Who")||query.contains("What")||query.contains("Which")||query.contains("When")||query.contains("List")){
            return true;
        }
        if(query.contains("who")||query.contains("what")||query.contains("which")||query.contains("when")||query.contains("list")){
            return true;
        }
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(query);
        if(m.matches()){
            return true;
        }
        return false;
    }

    public static void wirte2json(List<List<String>> project,String jsonFilePath) throws JSONException, IOException {


        JSONArray projectInfo = new JSONArray();

        for(int i = 0;i< project.size();i++){
            JSONObject projectobj = new JSONObject();
            projectobj.put("name",project.get(i).get(0));
            projectobj.put("description",project.get(i).get(1));
            projectInfo.put(i,projectobj);
        }
        FileWriter fw = new FileWriter(jsonFilePath,true);
        PrintWriter out = new PrintWriter(fw);
        out.write(projectInfo.toString());
        out.println();
        out.close();
        fw.close();
    }

    public static void main(String args[]) throws IOException, JSONException {
        //getProjectList();
        List<String> project1 = new ArrayList<>();
        project1.add("name1");
        project1.add("desc1");
        List<String> project2 = new ArrayList<>();
        project2.add("name2");
        project2.add("desc2");
        List<List<String>> project = new ArrayList<>();
        project.add(project1);
        project.add(project2);
        //wirte2json(project,"F:\\pro.json");


        System.out.println("OK");
    }
}
