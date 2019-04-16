package cn.edu.pku.sei.intellide.graph.extraction.code_embedding;

import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import org.neo4j.graphdb.*;

public class CsnExtractor {

    public static String IS_A="isA";
    public static String HAS_ACTION="hasAction";
    public static String AGGREGATION="aggregation";
    public static String CALL="call";

    public static Graph extract(GraphDatabaseService db){
        Graph r=new Graph();
        try (Transaction tx=db.beginTx()){
            db.findNodes(JavaExtractor.CLASS).stream().forEach(n->
                    r.createVertex((String) n.getProperty(JavaExtractor.FULLNAME)));
            db.findNodes(JavaExtractor.METHOD).stream().forEach(n->
                    r.createVertex((String) n.getProperty(JavaExtractor.FULLNAME)));
            // IS_A relationship
            db.getAllRelationships().stream().filter(rel->rel.getType().equals(
                    JavaExtractor.EXTEND)||rel.getType().equals(JavaExtractor.IMPLEMENT)).forEach(rel->{
                    r.createEdge((String) rel.getStartNode().getProperty(JavaExtractor.FULLNAME),
                                 (String)rel.getEndNode().getProperty(JavaExtractor.FULLNAME),
                                 IS_A);
            });
            //HAS_ACTION relationship
            db.getAllRelationships().stream().filter(rel->rel.getType().equals(JavaExtractor.HAVE_METHOD)).forEach(rel->{
                r.createEdge((String) rel.getStartNode().getProperty(JavaExtractor.FULLNAME),
                            (String)rel.getEndNode().getProperty(JavaExtractor.FULLNAME),
                            HAS_ACTION);
            });
            //AGGREGATION relationship
            db.findNodes(JavaExtractor.CLASS).stream().forEach(owner->{
                owner.getRelationships(JavaExtractor.HAVE_FIELD, Direction.OUTGOING).forEach(fieldRel->{
                    fieldRel.getEndNode().getRelationships(JavaExtractor.FIELD_TYPE, Direction.OUTGOING).forEach(typeRel->{
                        r.createEdge((String) typeRel.getEndNode().getProperty(JavaExtractor.FULLNAME),
                                    (String)owner.getProperty(JavaExtractor.FULLNAME),
                                    AGGREGATION);
                    });
                });
            });
            //CALL relationship
            db.getAllRelationships().stream().filter(rel->rel.getType().equals(JavaExtractor.METHOD_CALL)).forEach(rel->{
                r.createEdge((String) rel.getStartNode().getProperty(JavaExtractor.FULLNAME),
                            (String)rel.getEndNode().getProperty(JavaExtractor.FULLNAME),
                            CALL);
            });
            tx.success();
        }
        r.weight();
        return r;
    }

}
