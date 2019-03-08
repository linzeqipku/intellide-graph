package cn.edu.pku.sei.intellide.graph.helpers;

import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class CodeSearchDataCollector {

    public static void main(String[] args) throws IOException {
        //runText("train"); runText("valid");
        runGraphDb();
    }

    public static void runText(String name) throws IOException {
        String inCodePath = "E:\\drm_codesearch\\data\\hu18\\"+name+".code";
        String inNlPath = "E:\\drm_codesearch\\data\\hu18\\"+name+".nl";
        String outCodePath = "E:\\drm_codesearch\\data\\hu18\\"+name+"-fixed.code";
        String outNlPath = "E:\\drm_codesearch\\data\\hu18\\"+name+"-fixed.nl";

        Map<String, String> nlMap = new HashMap<>();
        Map<String, String> codeMap = new HashMap<>();

        List<String> lines = FileUtils.readLines(new File(inNlPath), "utf-8");
        for (String line : lines){
            if (line.length() > 0){
                String[] eles = line.trim().split("\t");
                nlMap.put(eles[0], eles[1]);
            }
        }
        lines = FileUtils.readLines(new File(inCodePath), "utf-8");
        for (String line : lines){
            if (line.length() > 0){
                String[] eles = line.trim().split("\t");
                codeMap.put(eles[0], eles[1]);
            }
        }

        Map<String, String> map = new HashMap<>();
        Set<String> set = new HashSet<>();

        for (String key : nlMap.keySet()){
            String javadoc = nlMap.get(key);
            String content = codeMap.get(key);
            if (javadoc.contains("test") || javadoc.contains("Test")){
                continue;
            }
            run(javadoc, content, map, set);
        }

        write(map, set, outNlPath, outCodePath);

    }

    public static void runGraphDb(){

        String[] graphDirs = new String[]{"E:\\SnowGraphData\\jfreechart\\graph.db"};
        String codePath = "E:\\SnowGraphData\\jfreechart.code";
        String nlPath = "E:\\SnowGraphData\\jfreechart.nl";

        Map<String, String> map = new HashMap<>();
        Set<String> set = new HashSet<>();

        for (String graphDir : graphDirs) {
            GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));

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
                    //System.out.println(content);
                    run(javadoc, content, map, set);
                }
                tx.success();
            }
            graphDB.shutdown();
        }
        write(map, set, nlPath, codePath);
    }

    private static void write(Map<String, String> map, Set<String> set, String nlPath, String codePath){
        for (String nl : set){
            map.remove(nl);
        }
        List<String> codes = new ArrayList<>();
        List<String> nls = new ArrayList<>();
        int c = 1;
        for (String key : map.keySet()){
            codes.add("" + c + "\t" + map.get(key));
            nls.add("" + c + "\t" + key);
            c++;
        }
        try {
            FileUtils.writeLines(new File(nlPath), nls);
            FileUtils.writeLines(new File(codePath), codes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void run(String javadoc, String content, Map<String, String> map, Set<String> set){
        if (content.length() == 0){
            return;
        }
        String code = content2code(content);
        if (code.length() == 0 || !checkCode(code)){
            return;
        }
        code = code.replace("\n","\\n");
        String nl = javadoc2nl(javadoc).replace("\n","\\n");
        //System.out.println(nl);
        //System.out.println(code);
        //System.out.println("" + checkNl(nl) + ", " + checkCode(code));
        //System.out.println();
        nl = getFirstSentence(nl);
        if (nl.length() == 0 || !checkNl(nl)){
            return;
        }
        if (!map.containsKey(nl)){
            map.put(nl, code);
        }
        else {
            set.add(nl);
        }
    }

    private static String content2code(String content){
        content = content.trim();
        content = content.replaceAll("^/\\*[\\s\\S]+?\\*/", "").trim();
        content = removeComments(content);
        content = content.replaceAll("\\r", "").trim();
        return content;
    }

    private static String removeComments(String code) {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        boolean quoteFlag = false;
        for (int i = 0; i < code.length(); i++) {
            //如果没有开始双引号范围
            if (!quoteFlag) {
                //如果发现双引号开始
                if (code.charAt(i) == '\"') {
                    sb.append(code.charAt(i));
                    quoteFlag = true;
                    continue;
                }
                //处理双斜杠注释
                else if (i + 1 < code.length() && code.charAt(i) == '/' && code.charAt(i + 1) == '/') {
                    while (code.charAt(i) != '\n') {
                        i++;
                    }
                    continue;
                }
                //不在双引号范围内
                else {
                    //处理/**/注释段
                    if (cnt == 0) {
                        if (i + 1 < code.length() && code.charAt(i) == '/' && code.charAt(i + 1) == '*') {
                            cnt++;
                            i++;
                            continue;
                        }
                    } else {
                        //发现"*/"结尾
                        if (i + 1 < code.length() && code.charAt(i) == '*' && code.charAt(i + 1) == '/') {
                            cnt--;
                            i++;
                            continue;
                        }
                        //发现"/*"嵌套
                        if (i + 1 < code.length() && code.charAt(i) == '/' && code.charAt(i + 1) == '*') {
                            cnt++;
                            i++;
                            continue;
                        }
                    }
                    //如果没有发现/**/注释段或者已经处理完了嵌套的/**/注释段
                    if (cnt == 0) {
                        sb.append(code.charAt(i));
                        continue;
                    }
                }
            }
            //处理双引号注释段
            else {
                //如果发现双引号结束(非转义形式的双引号)
                if (code.charAt(i) == '\"' && code.charAt(i - 1) != '\\') {
                    sb.append(code.charAt(i));
                    quoteFlag = false;
                }
                //双引号开始了但是还没有结束
                else {
                    sb.append(code.charAt(i));
                }
            }
        }
        return sb.toString();
    }

    private static boolean checkCode(String code){
        code = code.trim();
        if (code.contains("Deprecated") || code.contains("Override") || !code.endsWith("}")
                || code.contains("0x")
                || Pattern.compile("\\{\\s+}").matcher(code).find()){
            return false;
        }
        if (code.split(";").length < 5){
            return false;
        }
        return true;
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
        nl = nl.replaceAll("[E|e]xpect:", "");
        return nl.trim();
    }

    private static boolean checkNl(String nl){
        nl = nl.toLowerCase();
        if (nl.startsWith("return") || nl.startsWith("get") || nl.startsWith("set") || nl.startsWith("throw")
                || nl.startsWith("method") || nl.contains("deprecated") || nl.contains("todo") || nl.contains("this")
                || nl.contains("<") || nl.contains(">") || nl.contains("?") || nl.contains("eg") || nl.contains("unsupport")
                || nl.contains("see") || nl.contains("assumption")){
            return false;
        }
        String[] tokens = nl.split("\\W+");
        if (tokens.length <= 3 || !nl.contains(" ")){
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
