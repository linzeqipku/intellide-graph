package cn.edu.pku.sei.intellide.graph.qa.nl_query;

import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.neo4j.driver.v1.Driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NLQueryEngine {

    private Driver driver;

    public NLQueryEngine(Driver driver, String dataDirPath){
        this.driver=driver;
    }

    public Neo4jSubGraph search(String queryString){
        List<Long> nodes=new ArrayList<>();
        List<Long> rels=new ArrayList<>();
        nodes.add((long) 203);
        return new Neo4jSubGraph(nodes,rels,driver);
    }

}
