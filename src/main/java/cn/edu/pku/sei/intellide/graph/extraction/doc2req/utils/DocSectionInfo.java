package cn.edu.pku.sei.intellide.graph.extraction.doc2req.utils;

import cn.edu.pku.sei.intellide.graph.extraction.doc2req.Doc2ReqExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.docx.DocxExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.json.RequirementExtractor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.*;
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
        //System.out.println("cp2 node title :" + node.getProperty("title"));
        try {
            extractContentFromNode();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setIR = new HashSet<>();
        setSR = new HashSet<>();
        setAR = new HashSet<>();
    }


    public void extractContentFromNode() throws JSONException {
        // TODO : getProperty返回的是String吗;
        StringBuilder s = new StringBuilder();
        s.append(node.getProperty(DocxExtractor.TITLE)).append(" ");
        JSONObject content = new JSONObject((String) node.getProperty(DocxExtractor.CONTENT));
        for (Iterator it = content.keys(); it.hasNext(); ) {
            String key = (String) it.next();
            s.append(key).append(" ");
            s.append(content.getString(key)).append(" ");
        }
        JSONArray tables = new JSONArray((String) node.getProperty(DocxExtractor.TABLE));
        for (int i = 0; i < tables.length(); i++) {
            JSONArray table = tables.getJSONArray(i);
            for (int j = 0; j < table.length(); j++) {
                s.append(table.getString(j)).append(" ");
            }
        }
        this.content = s.toString();
    }


    // 正则匹配内容中的需求编号
    public void findReqByBusinessno(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {

            Matcher irMatcher = irPattern.matcher(content);
            while (irMatcher.find()) {
                String ir_no = content.substring(irMatcher.start(), irMatcher.end());
                Node ir = db.findNode(RequirementExtractor.IR, RequirementExtractor.BUSINESS_NO, ir_no);
                if (ir != null) {
                    //System.out.println("cp3 regex: " + ir.getProperty("business_no"));
                    setIR.add(ir);
                }
            }

            Matcher srMatcher = srPattern.matcher(content);
            while (srMatcher.find()) {
                String sr_no = content.substring(srMatcher.start(), srMatcher.end());
                Node sr = db.findNode(RequirementExtractor.SR, RequirementExtractor.BUSINESS_NO, sr_no);
                if (sr != null) {
                    //System.out.println("cp3 regex: " + sr.getProperty("business_no"));
                    setSR.add(sr);
                }
            }

            Matcher arMatcher = arPattern.matcher(content);
            while (arMatcher.find()) {
                String ar_no = content.substring(arMatcher.start(), arMatcher.end());
                Node ar = db.findNode(RequirementExtractor.AR, RequirementExtractor.BUSINESS_NO, ar_no);
                if (ar != null) {
                    //System.out.println("cp3 regex: " + ar.getProperty("business_no"));
                    setAR.add(ar);
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
                if (reqNode != null){
                    //System.out.println("cp4 name: " + query + " " + reqNode.getProperty("business_no"));
                    setIR.add(reqNode);
                }
                reqNode = db.findNode(RequirementExtractor.SR, RequirementExtractor.NAME, query);
                if (reqNode != null) {
                    //System.out.println("cp4 name: " + query + " " + reqNode.getProperty("business_no"));
                    setSR.add(reqNode);
                }
                reqNode = db.findNode(RequirementExtractor.AR, RequirementExtractor.NAME, query);
                if (reqNode != null) {
                    //System.out.println("cp4 name: " + query + " " + reqNode.getProperty("business_no"));
                    setAR.add(reqNode);
                }
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
        Set<Node> targetNodes;
        Set<Node> sourceNodes;
        if (n == 2) {
            sourceNodes = setIR;
            targetNodes = setSR;
        }
        else if (n == 3) {
            sourceNodes = setSR;
            targetNodes = setAR;
        }
        else {
            return;
        }
        try (Transaction tx = db.beginTx()) {
//            for (Node endNode : sourceNodes) {
//                this.node.createRelationshipTo(endNode, Doc2ReqExtractor.SOURCE);
//            }
            for (Node endNode : targetNodes) {
                this.node.createRelationshipTo(endNode, Doc2ReqExtractor.TARGET);
            }
            tx.success();
        }
    }

}
