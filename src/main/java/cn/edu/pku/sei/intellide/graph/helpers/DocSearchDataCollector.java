package cn.edu.pku.sei.intellide.graph.helpers;

import cn.edu.pku.sei.intellide.graph.extraction.qa.StackOverflowExtractor;
import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.qa.doc_search.DocSearch;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jNode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DocSearchDataCollector {

    public static void main(String[] args) throws IOException, ParseException {
        String graphDir = "";
        String indexDirPath = "";
        String languageIdentifier = "english";
        String project = "lucene";

        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
        CodeSearch codeSearch = new CodeSearch(db, languageIdentifier);
        DocSearch docSearch = new DocSearch(db, indexDirPath, codeSearch);

        Map<Node, Node> qaMap = new HashMap<>();
        try (Transaction tx=db.beginTx()){
            db.getAllNodes().forEach(n->{
                if (n.hasLabel(StackOverflowExtractor.QUESTION)){
                    Iterator<Relationship> rels = n.getRelationships(StackOverflowExtractor.HAVE_ANSWER, Direction.OUTGOING).iterator();
                    if (rels.hasNext()){
                        Node aNode = rels.next().getEndNode();
                        if ((boolean)aNode.getProperty(StackOverflowExtractor.ANSWER_ACCEPTED)){
                            qaMap.put(n,aNode);
                        }
                    }
                }
            });
            tx.success();
        }

        try (Transaction tx=db.beginTx()){
            for (Node qNode:qaMap.keySet()){
                String query = (String)qNode.getProperty(StackOverflowExtractor.QUESTION_TITLE);
                List<Neo4jNode> irResult = docSearch.search(query, project, false);
                List<Neo4jNode> snowResult = docSearch.search(query, project, true);
                int irRank = irResult.size() + 1, snowRank = irRank;
                long aId = qaMap.get(qNode).getId();
                for (int i = 0; i < irResult.size(); i++){
                    if (irResult.get(i).getId() == aId){
                        irRank = i+1;
                    }
                    if (snowResult.get(i).getId() == aId){
                        snowRank = i+1;
                    }
                }
                if (snowRank <= 20 && (irRank>snowRank || aId%7<2)){
                    System.out.println(qNode.getId()+": "+irRank+", "+snowRank);
                }
            }
            tx.success();
        }

    }

}
