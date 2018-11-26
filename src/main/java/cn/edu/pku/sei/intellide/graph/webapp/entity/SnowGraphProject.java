package cn.edu.pku.sei.intellide.graph.webapp.entity;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnowGraphProject {

    private final String name;
    private final String description;

    public SnowGraphProject(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static List<SnowGraphProject> getProjectList(String jsonPath) throws IOException, JSONException {
        List<SnowGraphProject> projectList = new ArrayList<>();
        File jsonFile = ResourceUtils.getFile(jsonPath);
        String json = FileUtils.readFileToString(jsonFile, "utf-8");
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            String name = jobj.getString("name");
            String description = jobj.getString("description");
            projectList.add(new SnowGraphProject(name, description));
        }
        return projectList;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
