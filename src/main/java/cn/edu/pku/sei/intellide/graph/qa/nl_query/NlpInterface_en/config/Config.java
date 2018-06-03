package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.config;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private static String neo4jBoltUrl = null;
    //private static Driver neo4jBoltConnection = null;
    private static String lucenePath = null;
    private static Config single = null;
    static {
        List<String> lines = new ArrayList<>();
        try {
            lines = FileUtils.readLines(new File( Config.class.getResource("/").getPath() + "conf"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (String line : lines) {
            int p = line.indexOf(' ');
            if (p > 0) {
                String pre = line.substring(0, p);
                String suf = line.substring(p + 1);
                if (pre.equals("neo4jBoltUrl"))
                    neo4jBoltUrl = suf;
                if (pre.equals("dataPath")) {
                    lucenePath = suf+"/index";
                }
            }
        }
        //neo4jBoltConnection = GraphDatabase.driver(neo4jBoltUrl, AuthTokens.basic("neo4j", "123"));
    }

    public static String getLucenePath() {
        if (Config.single == null) single = new Config();
        return lucenePath;
    }
//    public static Driver getNeo4jBoltDriver() {
//        if (Config.single == null) single = new Config();
//        return neo4jBoltConnection;
//    }

}
