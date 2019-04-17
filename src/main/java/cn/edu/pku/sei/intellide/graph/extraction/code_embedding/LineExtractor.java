package cn.edu.pku.sei.intellide.graph.extraction.code_embedding;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class LineExtractor extends KnowledgeExtractor {

    public static final String LINE_VEC = "lineVec";
    private LINE line = null;

    @Override
    public void extraction() {
        try (Transaction tx = this.getDb().beginTx()) {
            for (Node node : this.getDb().getAllNodes()) {
                if (node.hasProperty(LINE_VEC))
                    node.removeProperty(LINE_VEC);
            }
            tx.success();
        }
        line = new LINE(this.getDb());
        line.readData();
        line.run();
        writeData();
    }

    private void writeData(){
        try(Transaction tx = this.getDb().beginTx()){
            for (String key : line.vertex.keySet()){
                double[] embedding = line.vertex.get(key).emb_vertex;
                String line = "";
                for (double x : embedding)
                    line += x + " ";
                line = line.trim();
                Node node = this.getDb().findNode(JavaExtractor.CLASS,JavaExtractor.FULLNAME, key);
                if (node==null)
                    node = this.getDb().findNode(JavaExtractor.CLASS,JavaExtractor.FULLNAME, key);
                if (node==null)
                    node = this.getDb().findNode(JavaExtractor.METHOD,JavaExtractor.FULLNAME, key);
                if (node!=null)
                    node.setProperty(LineExtractor.LINE_VEC, line);
            }
            tx.success();
        }
    }
}
