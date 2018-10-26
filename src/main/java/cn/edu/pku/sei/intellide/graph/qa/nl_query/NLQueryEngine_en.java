package cn.edu.pku.sei.intellide.graph.qa.nl_query;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.NLPInterpreter;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.extractmodel.ExtractModel;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.ir.LuceneIndex;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.core.NodeProxy;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NLQueryEngine_en {

    private GraphDatabaseService db;
    private String dataDirPath;

    public NLQueryEngine_en(GraphDatabaseService db, String dataDirPath){
        this.db=db;
        this.dataDirPath = dataDirPath;
        ExtractModel.db = db;
        ExtractModel.single = null;
        LuceneIndex.dataDirPath = dataDirPath;
    }
    public Neo4jSubGraph search(String queryString){
        queryString.replace("接口","接口类");
        ExtractModel.db = this.db;
        List<Long> nodes=new ArrayList<>();
        List<Long> rels=new ArrayList<>();
        List<Long> retnodes = new ArrayList<>();
        List<String> cyphers = null;
        String cypherret = "";
        if (queryString.matches("\\d+")){
            String c = "Match (n) where id(n)="+queryString+" return n, id(n), labels(n)";
            //System.out.println(c);
            Result p = db.execute(c + " limit 30");
            while (p.hasNext()){
                Map m = p.next();
                nodes.add((Long)m.get("id(n)"));
            }
            cypherret = c;
        }else {
            cyphers = NLPInterpreter.pipeline(queryString);
            if (cyphers==null || cyphers.size() == 0) return new Neo4jSubGraph(nodes, rels, db);

            String c = cyphers.get(0);
            String returnT;String whereT; String matchT;
            if (!c.contains("WHERE")){
                returnT = c.substring(c.indexOf("RETURN") + 7, c.length());
                whereT = "WHERE (true)";
                matchT = c.substring(c.indexOf("MATCH"), c.indexOf("RETURN"));
            }else {
                returnT = c.substring(c.indexOf("RETURN") + 7, c.length());
                matchT = c.substring(c.indexOf("MATCH"), c.indexOf("WHERE"));
                whereT = c.substring(c.indexOf("WHERE"), c.indexOf("RETURN"));
            }
            String nodeid;
            if (!returnT.contains("labels")){
                nodeid = returnT.substring(0,returnT.indexOf("."));
                c = c.substring(0,c.indexOf("RETURN")+7) + String.format("%s,id(%s),labels(%s)",nodeid,nodeid,nodeid);
            }else nodeid = returnT.substring(0,returnT.indexOf(","));
            //System.out.println(c.replaceAll("RETURN","RETURN distinct"));

            Result p = db.execute(c.replaceAll("RETURN","RETURN distinct")+ " limit 10");
            cypherret = c;
            //System.out.println(cypherret);
            while (p.hasNext()) {
                Map m = p.next();
                retnodes.add((Long) m.get("id("+nodeid+")"));
            }
            System.out.println(retnodes.size());
            for (Long id : retnodes){
                String tmpc = "MATCH p= "+matchT.substring(5,matchT.length());
                tmpc += whereT + " AND (id("+nodeid+")="+id+")";
                tmpc += "return p";
                //System.out.println(tmpc);
                Result pr = db.execute(tmpc + " limit 1");
                while (pr.hasNext()) {
                    Map m = pr.next();
                    Path obj = (Path)m.get("p");
                    Iterator iter = obj.nodes().iterator();
                    while (iter.hasNext()){
                        NodeProxy nodep = (NodeProxy)iter.next();
                        nodes.add(nodep.getId());
                    }
                    iter = obj.relationships().iterator();
                    while (iter.hasNext()){
                        Relationship relp = (Relationship)iter.next();
                        rels.add(relp.getId());
                    }
                    //retnodes.add((Long) m.get("id("+nodeid+")"));
                }
            }
        }
        Neo4jSubGraph ppp =  new Neo4jSubGraph(nodes,rels,db);
        ppp.setCypher(cypherret);
        if (!cypherret.toLowerCase().contains("where")){
            ppp.getNodes().clear();
            ppp.getRelationships().clear();
            ppp.setCypher("");
        }
        return ppp;
    }
//    public Neo4jSubGraph search(String queryString){
//        queryString.replace("接口","接口类");
//        ExtractModel.db = this.db;
//        List<Long> nodes=new ArrayList<>();
//        List<Long> rels=new ArrayList<>();
//        List<String> cyphers = null;
//        if (queryString.matches("\\d+")){
//            String c = "Match (n) where id(n)="+queryString+" return n, id(n), labels(n)";
//            System.out.println(c);
//            Result p = db.execute(c + " limit 30");
//            while (p.hasNext()){
//                Map m = p.next();
//                nodes.add((Long)m.get("id(n)"));
//            }
//        }else {
//            cyphers = NLPInterpreter.pipeline(queryString);
//            if (cyphers==null || cyphers.size() == 0) return new Neo4jSubGraph(nodes, rels, db);
//            String c = cyphers.get(0);
//            String returnT = c.substring(c.indexOf("RETURN")+7,c.length());
//            String nodeid;
//            if (!returnT.contains("labels")){
//                nodeid = returnT.substring(0,returnT.indexOf("."));
//                c = c.substring(0,c.indexOf("RETURN")+7) + String.format("%s,id(%s),labels(%s)",nodeid);
//            }else nodeid = returnT.substring(0,returnT.indexOf(","));
//            System.out.println(c);
//            Result p = db.execute(c + " limit 30");
//
//            while (p.hasNext()) {
//                Map m = p.next();
//                nodes.add((Long) m.get("id("+nodeid+")"));
//            }
//        }
//        return new Neo4jSubGraph(nodes,rels,db);
//    }

    public void createIndex(){
        if (new File(dataDirPath+"/index").exists())
            return;
        LuceneIndex LI = new LuceneIndex();
        try {
            LI.index();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]){
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("F:\\graph.db-tokens2"));
        NLQueryEngine_en engine =  new NLQueryEngine_en(db,"E:\\tmp\\data_dir");
        Scanner sc = new Scanner(System.in);
        engine.search("列出\"发布通知\"有关的文档");
        while (sc.hasNextLine()){
            String s = sc.nextLine();
            engine.search(s);
        }

        //engine.createIndex();
    }
}
