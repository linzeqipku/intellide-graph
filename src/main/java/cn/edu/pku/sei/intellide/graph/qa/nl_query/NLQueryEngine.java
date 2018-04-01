package cn.edu.pku.sei.intellide.graph.qa.nl_query;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.NLPInterpreter;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.ExtractModel;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.ir.LuceneIndex;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.json.JSONException;
import org.neo4j.cypher.internal.ExecutionEngine;
import org.neo4j.cypher.internal.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class NLQueryEngine {

    private GraphDatabaseService db;
    private String dataDirPath;

    public NLQueryEngine(GraphDatabaseService db, String dataDirPath){
        this.db=db;
        this.dataDirPath = dataDirPath;
        ExtractModel.db = db;
        LuceneIndex.dataDirPath = dataDirPath;
    }

    public Neo4jSubGraph search(String queryString){
        ExtractModel.db = this.db;
        List<Long> nodes=new ArrayList<>();
        List<Long> rels=new ArrayList<>();
        List<String> cyphers = null;
        try {
            cyphers = NLPInterpreter.pipeline(queryString);
            Result p = db.execute(cyphers.get(0) + " limit 30");
            for (String s : cyphers) {
                System.out.println(s);
            }
            while (p.hasNext()){
                Map m = p.next();
                nodes.add((Long)m.get("id(n0)"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new Neo4jSubGraph(nodes,rels,db);
    }
    public void createIndex(){

        LuceneIndex LI = new LuceneIndex();
        try {
            LI.index();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]){
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("F:\\graph.db-tokens2"));
        NLQueryEngine engine =  new NLQueryEngine(db,"E:\\tmp\\data_dir");
        Scanner sc = new Scanner(System.in);
        engine.search("列出\"发布通知\"有关的文档");
        while (sc.hasNextLine()){
            String s = sc.nextLine();
            engine.search(s);
        }

        //engine.createIndex();
    }
}
