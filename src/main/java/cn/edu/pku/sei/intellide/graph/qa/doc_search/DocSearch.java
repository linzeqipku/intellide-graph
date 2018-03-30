package cn.edu.pku.sei.intellide.graph.qa.doc_search;

import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jNode;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DocSearch {

    private GraphDatabaseService db;

    public DocSearch(GraphDatabaseService db){
        this.db=db;
    }

    public List<Neo4jNode> search(String queryString){
        Set<String> tokens= CodeTokenizer.tokenization(queryString);
        List<Neo4jNode> nodes=new ArrayList<>();
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        nodes.add(Neo4jNode.get(67182,db));
        return nodes;
    }

}
