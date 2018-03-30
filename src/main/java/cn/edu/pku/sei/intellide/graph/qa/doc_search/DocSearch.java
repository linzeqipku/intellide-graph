package cn.edu.pku.sei.intellide.graph.qa.doc_search;

import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jNode;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.neo4j.driver.v1.Driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DocSearch {

    private Driver driver;

    public DocSearch(Driver driver){
        this.driver=driver;
    }

    public List<Neo4jNode> search(String queryString){
        Set<String> tokens= CodeTokenizer.tokenization(queryString);
        List<Neo4jNode> nodes=new ArrayList<>();
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        nodes.add(Neo4jNode.get(67182,driver));
        return nodes;
    }

}
