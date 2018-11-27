package cn.edu.pku.sei.intellide.graph.helpers;

import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.*;

public class CodeSearchDataCollector {

    public static void main(String[] args){

        String graphDir = "E:\\SnowGraphData\\poi\\graph.db";
        String codePath = "E:\\SnowGraphData\\poi\\poi.code";
        String nlPath = "E:\\SnowGraphData\\poi\\poi.nl";

        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));

        Map<String, String> map = new HashMap<>();
        Set<String> set = new HashSet<>();

        try (Transaction tx = graphDB.beginTx()){
            ResourceIterator<Node> methods = graphDB.findNodes(JavaExtractor.METHOD);
            while (methods.hasNext()){
                Node method = methods.next();
                String fullName = (String) method.getProperty(JavaExtractor.FULLNAME);
                if (fullName.toLowerCase().contains("test")){
                    continue;
                }
                if ((boolean)method.getProperty(JavaExtractor.IS_CONSTRUCTOR)){
                    continue;
                }
                String javadoc = (String) method.getProperty(JavaExtractor.COMMENT);
                String content = ((String) method.getProperty(JavaExtractor.CONTENT)).trim();
                if (content.length() == 0){
                    continue;
                }
                String code = content.replace(javadoc, "").trim()
                        .replace("\r", "").replace("\n","\\n");
                if (code.contains("@Deprecated") || code.contains("@Override") || !code.endsWith("}")){
                    continue;
                }
                String nl = javadoc2nl(javadoc).replace("\n","\\n");
                if (nl.length() == 0 || !checkNl(nl)){
                    continue;
                }
                nl = getFirstSentence(nl);
                if (!map.containsKey(nl)){
                    map.put(nl, code);
                }
                else {
                    set.add(nl);
                }
            }
            tx.success();
        }
        graphDB.shutdown();

        for (String nl : set){
            map.remove(nl);
        }
        for (String key : map.keySet()){
            System.out.println(key);
        }

    }

    private static String javadoc2nl(String javadoc){
        String nl = "";
        String[] lines = javadoc.trim().split("[\\r\\n]+");
        for (String line : lines){
            line = line.trim();
            if (line.contains("@") || line.contains("<a href=")){
                continue;
            }
            line = line.replaceAll("[/*]+", "").trim();
            if (line.length() == 0){
                continue;
            }
            nl += line + "\n";
        }
        nl = nl.replaceAll("(?<=[A-Za-z0-9])\\n(?=[A-Z]+)", ". ");
        nl = nl.replaceAll("\\n", " ");
        return nl.trim();
    }

    private static boolean checkNl(String nl){
        nl = nl.toLowerCase();
        if (nl.startsWith("return") || nl.startsWith("get") || nl.startsWith("set") || nl.startsWith("throw")
                || nl.startsWith("method")
                || nl.contains("<") || nl.contains(">") || nl.contains("?")){
            return false;
        }
        String[] tokens = nl.split("\\W+");
        if (tokens.length <= 5){
            return false;
        }
        return true;
    }

    private static String getFirstSentence(String nl){
        if (nl.contains(". ")){
            return nl.substring(0, nl.indexOf(". "));
        }
        return nl;
    }

}
