package cn.edu.pku.sei.intellide.graph.qa.nl_query;

import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.ArrayList;
import java.util.List;

public class NLQueryEngine {

    private GraphDatabaseService db;

    public NLQueryEngine(GraphDatabaseService db, String dataDirPath){
        this.db=db;
    }

    public Neo4jSubGraph search(String queryString){
        List<Long> nodes=new ArrayList<>();
        List<Long> rels=new ArrayList<>();
        nodes.add((long) 203);
        return new Neo4jSubGraph(nodes,rels,db);
    }

}
