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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DocSearchDataCollector {

    public static void main(String[] args) throws IOException, ParseException {
        String graphDir = "E:/temp/lucene";
        String indexDirPath = "E:/temp/index";
        String languageIdentifier = "english";
        String project = "lucene";

        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
        CodeSearch codeSearch = new CodeSearch(db, languageIdentifier);
        DocSearch docSearch = new DocSearch(db, indexDirPath, codeSearch);

        Map<Node, Node> qaMap = new HashMap<>();
        try (Transaction tx = db.beginTx()) {
            db.getAllNodes().forEach(n -> {
                if (n.hasLabel(StackOverflowExtractor.QUESTION)) {
                    Iterator<Relationship> rels = n.getRelationships(StackOverflowExtractor.HAVE_ANSWER, Direction.OUTGOING).iterator();
                    while (rels.hasNext()) {
                        Node aNode = rels.next().getEndNode();
                        if ((boolean) aNode.getProperty(StackOverflowExtractor.ANSWER_ACCEPTED)) {
                            qaMap.put(n, aNode);
                        }
                    }
                }
            });
            tx.success();
        }

        int c = 0;
        for (Node qNode : qaMap.keySet()) {
            c++;
            String query;
            long qId,aId;
            try (Transaction tx=db.beginTx()){
                query = (String) qNode.getProperty(StackOverflowExtractor.QUESTION_TITLE);
                qId = qNode.getId();
                aId = qaMap.get(qNode).getId();
                tx.success();
            }
            List<Neo4jNode> irResult = docSearch.search(query, project, false);
            List<Neo4jNode> snowResult = docSearch.search(query, project, true);
            int irRank = irResult.size() + 1, snowRank = irRank;
            for (int i = 0; i < irResult.size(); i++) {
                if (irResult.get(i).getId() == aId) {
                    irRank = i + 1;
                }
                if (snowResult.get(i).getId() == aId) {
                    snowRank = i + 1;
                }
            }
            if (snowRank <= 20 && (irRank > snowRank || aId % 7 < 2)) {
                System.out.println(c + "/" + qaMap.size() + ": (" + query + "), qId=" + qId + ", " + irRank + "-->" + snowRank);
            }
        }
        db.shutdown();
    }

}
