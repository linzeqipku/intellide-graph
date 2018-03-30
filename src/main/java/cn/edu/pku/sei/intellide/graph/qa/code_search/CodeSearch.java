package cn.edu.pku.sei.intellide.graph.qa.code_search;

import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CodeSearch {

    private GraphDatabaseService db;
    private GraphReader graphReader;
    private APILocater locater;

    public CodeSearch(GraphDatabaseService db){
        this.db = db;
        graphReader = new GraphReader(db);
        locater = new APILocater(graphReader);
    }

    public Neo4jSubGraph search(String queryString){
        Set<String> tokens = CodeTokenizer.tokenization(queryString);
        MySubgraph subgraph = locater.query(tokens);

        List<Long> nodes = new ArrayList<>(subgraph.nodes);
        List<Long> rels = new ArrayList<>(subgraph.edges);
        nodes.add((long) 203);
        return new Neo4jSubGraph(nodes,rels,db);
    }

}
