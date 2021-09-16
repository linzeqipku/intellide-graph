package cn.edu.pku.sei.intellide.graph.extraction.doc2req.utils;

import cn.edu.pku.sei.intellide.graph.extraction.doc2req.Doc2ReqExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.json.RequirementExtractor;
import org.json.JSONArray;
import org.json.JSONException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.api.exceptions.Status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocSectionInfo {
    public Node node;
    public String content;
    public Set<Node> setIR;
    public Set<Node> setSR;
    public Set<Node> setAR;

    private static String IRRegex = "IR\\.[A-Za-z0-9\\.]+";
    private static String SRRegex = "SR\\.[A-Za-z0-9\\.]+";
    private static String ARRegex = "AR\\.[A-Za-z0-9\\.]+";

    private static Pattern irPattern;
    private static Pattern srPattern;
    private static Pattern arPattern;

    static {
        irPattern = Pattern.compile(IRRegex);
        srPattern = Pattern.compile(SRRegex);
        arPattern = Pattern.compile(ARRegex);
    }


    public DocSectionInfo(Node node) {
        this.node = node;
        extractContentFromNode();
        setIR = new HashSet<>();
        setSR = new HashSet<>();
        setAR = new HashSet<>();
    }


    public void extractContentFromNode() {
        // TODO : getProperty返回的是String吗; 改成WordExtractor.xxx
        this.content = node.getProperty("title") + " " + node.getProperty("content") + " " + node.getProperty("table") + " ";
    }


    // 正则匹配内容中的需求编号
    public void findReqByBusinessno(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {

            Matcher irMatcher = irPattern.matcher(content);
            while (irMatcher.find()) {
                String ir_no = content.substring(irMatcher.start(), irMatcher.end());
                Node ir = db.findNode(RequirementExtractor.IR, RequirementExtractor.BUSINESS_NO, ir_no);
                if (ir != null) {
                    setIR.add(ir);
                }
            }

            Matcher srMatcher = srPattern.matcher(content);
            while (srMatcher.find()) {
                String sr_no = content.substring(srMatcher.start(), srMatcher.end());
                Node sr = db.findNode(RequirementExtractor.SR, RequirementExtractor.BUSINESS_NO, sr_no);
                if (sr != null) {
                    setIR.add(sr);
                }
            }

            Matcher arMatcher = arPattern.matcher(content);
            while (arMatcher.find()) {
                String ar_no = content.substring(arMatcher.start(), arMatcher.end());
                Node ar = db.findNode(RequirementExtractor.AR, RequirementExtractor.BUSINESS_NO, ar_no);
                if (ar != null) {
                    setIR.add(ar);
                }
            }

            tx.success();
        }
    }


    // 用特定query匹配需求名称
    public void findReqByName(GraphDatabaseService db) {
        List<String> queryString = getQueryString();
        try (Transaction tx = db.beginTx()) {
            for (String query : queryString) {
                Node reqNode;
                reqNode = db.findNode(RequirementExtractor.IR, RequirementExtractor.NAME, query);
                if (reqNode != null) setIR.add(reqNode);
                reqNode = db.findNode(RequirementExtractor.SR, RequirementExtractor.NAME, query);
                if (reqNode != null) setSR.add(reqNode);
                reqNode = db.findNode(RequirementExtractor.AR, RequirementExtractor.NAME, query);
                if (reqNode != null) setAR.add(reqNode);
            }
            tx.success();
        }
    }


    // 目前query来源于章节标题以及表格中内容
    // TODO : How to generate good queries?
    public List<String> getQueryString() {
        List<String> result = new ArrayList<>();
        result.add((String) node.getProperty("title"));
        try{
            JSONArray tables = new JSONArray((String) node.getProperty("table"));
            for (int i = 0; i < tables.length(); i++) {
                JSONArray oneTable = tables.getJSONArray(i);
                for (int j = 0; j < oneTable.length(); j++) {
                    String oneLine = oneTable.getString(j);
                    String[] tableContent = oneLine.split("\t");
                    for (String s : tableContent) {
                        if (s.length() > 0)  result.add(s);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void addSR(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            for (Node ar: setAR) {
                Node sr = ar.getSingleRelationship(RequirementExtractor.PARENT, Direction.OUTGOING).getEndNode();
                if (sr != null)     setSR.add(sr);
            }
            tx.success();
        }
    }


    public void addIR(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            for (Node sr: setSR) {
                Node ir = sr.getSingleRelationship(RequirementExtractor.PARENT, Direction.OUTGOING).getEndNode();
                if (ir != null)     setIR.add(ir);
            }
            tx.success();
        }
    }


    public void addRelationship(GraphDatabaseService db, int n) {
        Set<Node> endNodes;
        if (n == 2) {
            endNodes = setSR;
        }
        else if (n == 3) {
            endNodes = setAR;
        }
        else {
            return;
        }
        try (Transaction tx = db.beginTx()) {
            for (Node endNode : endNodes) {
                this.node.createRelationshipTo(endNode, Doc2ReqExtractor.TARGET);
            }
            tx.success();
        }
    }

}
