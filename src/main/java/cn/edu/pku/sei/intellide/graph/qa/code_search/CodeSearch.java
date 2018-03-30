package cn.edu.pku.sei.intellide.graph.qa.code_search;

import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.neo4j.driver.v1.Driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CodeSearch {

    private Driver driver;

    public CodeSearch(Driver driver){
        this.driver=driver;
    }

    public Neo4jSubGraph search(String queryString){
        Set<String> tokens= CodeTokenizer.tokenization(queryString);
        List<Long> nodes=new ArrayList<>();
        List<Long> rels=new ArrayList<>();
        nodes.add((long) 203);
        return new Neo4jSubGraph(nodes,rels,driver);
    }

}
