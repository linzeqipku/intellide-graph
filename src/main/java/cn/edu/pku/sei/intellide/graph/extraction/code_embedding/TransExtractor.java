package cn.edu.pku.sei.intellide.graph.extraction.code_embedding;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransExtractor extends KnowledgeExtractor {

    public static final String EMBEDDING = "embedding";

    private TransE transE = null;

    @Override
    public void extraction() {
        transE = new TransE();
        prepare();
        transE.run();
        writeVecLines();
    }

    private void prepare() {
        List<String> entities = new ArrayList<>();
        List<String> relations = new ArrayList<>();
        List<Triple<String, String, String>> triples = new ArrayList<>();
        try (Transaction tx = this.getDb().beginTx()) {
            for (Node node : this.getDb().getAllNodes()) {
                if (!node.hasLabel(JavaExtractor.CLASS) &&
                        !node.hasLabel(JavaExtractor.METHOD) &&
                        !node.hasLabel(JavaExtractor.FIELD))
                    continue;
                entities.add("" + node.getId());
            }

            for (Relationship rel : this.getDb().getAllRelationships()) {
                Node node1 = rel.getStartNode();
                if (!node1.hasLabel(JavaExtractor.CLASS) &&
                        !node1.hasLabel(JavaExtractor.METHOD) &&
                        !node1.hasLabel(JavaExtractor.FIELD))
                    continue;
                Node node2 = rel.getEndNode();
                if (!node2.hasLabel(JavaExtractor.CLASS) &&
                        !node2.hasLabel(JavaExtractor.METHOD) &&
                        !node2.hasLabel(JavaExtractor.FIELD))
                    continue;
                triples.add(new ImmutableTriple<>("" + node1.getId(), "" + node2.getId(), rel.getType().name()));
                if (!relations.contains(rel.getType().name()))
                    relations.add(rel.getType().name());
            }
            tx.success();
        }
        transE.prepare(entities, relations, triples);
    }

    private void writeVecLines() {
        Map<String, double[]> embeddings = transE.getEntityVecMap();
        List<String> keys = new ArrayList<>(embeddings.keySet());
        for (int i = 0; i < keys.size(); i += 1000) {
            try (Transaction tx = this.getDb().beginTx()) {
                for (int j = 0; j < 1000; j++) {
                    if (i + j >= keys.size())
                        break;
                    String nodeIdString = keys.get(i + j);
                    Node node = this.getDb().getNodeById(Long.parseLong(nodeIdString));
                    String line = "";
                    for (double d : embeddings.get(nodeIdString))
                        line += d + " ";
                    line = line.trim();
                    setVec(node, line);
                }
                tx.success();
            }
        }
    }

    private void setVec(Node node, String line) {
        node.setProperty(EMBEDDING, line);
    }

}
