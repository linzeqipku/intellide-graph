package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.rules;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CnToEnDirectory;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphEdgeType;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphPath;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphSchema;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathsJson {
    public static JSONArray JsonArr = null;
    public static GraphSchema graphSchema;
    public static String nodeName[] = new String[100];

    public static void readJson() {
        String content = "";
        try {
            InputStream in = CnToEnDirectory.class.getResourceAsStream("/nli/Path.json");
            content = StringUtils.join(IOUtils.readLines(in, "utf-8"), "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JsonArr = new JSONArray(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void getPaths(GraphSchema _graphSchema) {
        graphSchema = _graphSchema;
        if (JsonArr == null) {
            readJson();
        }
        for (int id = 0; id < JsonArr.length(); id++) {
            JSONObject pathObj;
            try {
                pathObj = JsonArr.getJSONObject(id);
                DFS(pathObj, 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public static void DFS(JSONObject json, int t) throws JSONException {
        if (t == json.getInt("length")) {
            for (int i = 0; i < json.getJSONArray("relations").getJSONObject(t - 1).getJSONArray("to").length(); i++) {
                nodeName[t] = json.getJSONArray("relations").getJSONObject(t - 1).getJSONArray("to").getString(i);
                GraphPath path = new GraphPath();
                path.start = graphSchema.vertexTypes.get(nodeName[0]);
                path.end = graphSchema.vertexTypes.get(nodeName[t]);
                boolean flag = true;
                for (int j = 1; j < t; j++) path.nodes.add(graphSchema.vertexTypes.get(nodeName[j]));
                String edgen = json.getJSONArray("relations").getJSONObject(0).getString("via");
                boolean direct = json.getJSONArray("relations").getJSONObject(0).getBoolean("direct");
                GraphEdgeType edge;
                if (direct) edge = graphSchema.findGraphEdgeTypeByNameAndVertex(edgen, path.start, path.nodes.get(0));
                else
                    edge = graphSchema.findGraphEdgeTypeByNameAndVertex(edgen, path.nodes.get(0), path.start);
                if (edge == null) continue;
                path.edges.add(edge);
                path.edgesDirect.add(direct);
                for (int j = 1; j < t - 1; j++) {
                    edgen = json.getJSONArray("relations").getJSONObject(j).getString("via");
                    direct = json.getJSONArray("relations").getJSONObject(j).getBoolean("direct");
                    if (direct)
                        edge = graphSchema.findGraphEdgeTypeByNameAndVertex(edgen, path.nodes.get(j - 1), path.nodes.get(j));
                    else
                        edge = graphSchema.findGraphEdgeTypeByNameAndVertex(edgen, path.nodes.get(j), path.nodes.get(j - 1));
                    if (edge == null) {
                        flag = false;
                        break;
                    }
                    path.edges.add(edge);
                    path.edgesDirect.add(direct);
                }
                if (!flag) continue;
                edgen = json.getJSONArray("relations").getJSONObject(t - 1).getString("via");
                direct = json.getJSONArray("relations").getJSONObject(t - 1).getBoolean("direct");
                if (direct) edge = graphSchema.findGraphEdgeTypeByNameAndVertex(edgen, path.nodes.get(t - 2), path.end);
                else
                    edge = graphSchema.findGraphEdgeTypeByNameAndVertex(edgen, path.end, path.nodes.get(t - 2));
                if (edge == null) continue;
                path.edges.add(edge);
                path.edgesDirect.add(direct);
                for (int k = 0; k < json.getJSONArray("keywords").length(); k++) {
                    String key = json.getJSONArray("keywords").getString(k);
                    if (!graphSchema.paths.keySet().contains(key)) {
                        Set<GraphPath> entry = new HashSet<>();
                        graphSchema.paths.put(key, entry);
                    }
                    graphSchema.paths.get(key).add(path);
                }
            }
            return;
        }
        for (int i = 0; i < json.getJSONArray("relations").getJSONObject(t).getJSONArray("from").length(); i++) {
            nodeName[t] = json.getJSONArray("relations").getJSONObject(t).getJSONArray("from").getString(i);
            DFS(json, t + 1);
        }

    }
}
