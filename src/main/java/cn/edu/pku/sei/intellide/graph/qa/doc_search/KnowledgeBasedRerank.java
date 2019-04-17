package cn.edu.pku.sei.intellide.graph.qa.doc_search;

import cn.edu.pku.sei.intellide.graph.extraction.code_embedding.TransExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.code_mention.CodeMentionExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.tokenization.TokenExtractor;
import cn.edu.pku.sei.intellide.graph.qa.code_search.APILocater;
import cn.edu.pku.sei.intellide.graph.qa.code_search.GraphReader;
import cn.edu.pku.sei.intellide.graph.qa.code_search.MyNode;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config.StopWords;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;

import java.util.*;

public class KnowledgeBasedRerank {

    private GraphDatabaseService db;
    private APILocater apiLocater;
    private String languageIdentifier;

    public KnowledgeBasedRerank(GraphDatabaseService db, String languageIdentifier){
        this.db = db;
        this.apiLocater = new APILocater(new GraphReader(db));
        this.languageIdentifier = languageIdentifier;
    }

    public List<Neo4jNode> rerank(String queryString, List<Neo4jNode> oList){
        List<Neo4jNode> r=new ArrayList<>();
        Set<String> baseTokens = TokenExtractor.tokenization(queryString);
        Set<String> tokens = new HashSet<>();
        for (String token : baseTokens)
            if (!StopWords.getInstance(languageIdentifier).isStopWord(token))
                tokens.add(token);
        Set<MyNode> qNodes = apiLocater.getStartSet(apiLocater.getRootNodeSet(tokens));
        List<Neo4jNode> qCodeElements = new ArrayList<>();
        try (Transaction tx=db.beginTx()){
            for (MyNode myNode : qNodes){
                Node node = db.getNodeById(myNode.id);
                HashMap<String, List<Double>> map = new HashMap<>();
                map.put("vec", new ArrayList<>());
                for (String e : ((String)node.getProperty(TransExtractor.EMBEDDING)).trim().split("\\s+")){
                    map.get("vec").add(Double.parseDouble(e));
                }
                qCodeElements.add(new Neo4jNode(node.getId(), "", map));
            }
            List<Pair<Neo4jNode,Double>> list = new ArrayList<>();
            for (Neo4jNode docNode : oList){
                List<Neo4jNode> codeElements = getCodeElements(docNode);
                double v = 0;
                for (Neo4jNode qCodeElement : qCodeElements) {
                    double minDistance = Double.MAX_VALUE;
                    for (Neo4jNode dCodeElement : codeElements) {
                        double distance = distance((List<Double>) qCodeElement.getProperties().get("vec"),
                                (List<Double>) dCodeElement.getProperties().get("vec"));
                        if (distance < minDistance) {
                            minDistance = distance;
                        }
                    }
                    v += minDistance;
                }
                list.add(new ImmutablePair<>(docNode, v));
            }
            list.sort(Comparator.comparing(Pair::getValue));
            list.forEach(n->r.add(n.getLeft()));
            tx.success();
        }
        return r;
    }

    private List<Neo4jNode> getCodeElements(Neo4jNode docNode){
        List<Neo4jNode> r = new ArrayList<>();
        db.getNodeById(docNode.getId()).getRelationships(CodeMentionExtractor.CODE_MENTION, Direction.OUTGOING)
                .forEach(x->{
                    HashMap<String, List<Double>> map = new HashMap<>();
                    map.put("vec", new ArrayList<>());
                    for (String e : ((String)x.getEndNode().getProperty(TransExtractor.EMBEDDING)).trim().split("\\s+")){
                        map.get("vec").add(Double.parseDouble(e));
                    }
                    r.add(new Neo4jNode(x.getEndNode().getId(), "", map));
                });
        return r;
    }

    private double distance(List<Double> a, List<Double> b){
        double r = 0;
        if (a.size()!=b.size()){
            return Double.MAX_VALUE;
        }
        for (int i=0;i<a.size();i++){
            r+=(a.get(i)-b.get(i))*(a.get(i)-b.get(i));
        }
        return Math.sqrt(r);
    }

}
