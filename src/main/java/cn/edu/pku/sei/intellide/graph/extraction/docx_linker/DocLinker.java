package cn.edu.pku.sei.intellide.graph.extraction.docx_linker;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import java.io.File;
import java.util.*;


public class DocLinker {
    public static String DB_PATH = "D://graph.db-tokens";

    public static GraphDatabaseService instance;

    public static GraphDatabaseService getInstance(){
        if(instance == null){
            instance = new GraphDatabaseFactory().newEmbeddedDatabase(new File(DB_PATH));
        }
        return instance;
    }

    public static void shutdownDB(){
        GraphDatabaseService db;
        db = DocLinker.getInstance();
        db.shutdown();
    }


    public static Map<Long,String> getNodeTitle(Label label){
        Map<Long,String> titleMap = new LinkedHashMap<Long,String>();
        GraphDatabaseService db = null;
        db = DocLinker.getInstance();
        ResourceIterator<Node> nodes = null;

        try(Transaction tx = db.beginTx()){
            nodes = db.findNodes(label);
            while(nodes.hasNext()){
                Node node = nodes.next();
                long id = node.getId();
                String title = (String)node.getProperty("title");
                if(!title.equals("")){
                    titleMap.put(id,title);
                }
            }
            tx.success();
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return titleMap;
    }

    public static void createRealtionship(List<Map<Long,Set<Long>>> nodePair){

        GraphDatabaseService db;
        db = DocLinker.getInstance();

        try (Transaction tx = db.beginTx()) {

            int cnt = 0;
            for(Map<Long,Set<Long>> nodeMap : nodePair){
                for (Map.Entry<Long, Set<Long>> entry : nodeMap.entrySet()){
                    long id1 = entry.getKey();
                    Node node1 = db.getNodeById(id1);
                    Set<Long> nodeset = entry.getValue();
                    for(long id2 : nodeset){
                        Node node2 = db.getNodeById(id2);
                        node1.createRelationshipTo(node2, RelationshipType.withName("relatedDocxSec"));
                        cnt++;
                        if(cnt % 1000 == 0 ) System.out.println(cnt);
                    }
                }
            }
            tx.success();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void deleteRelationship(){
        GraphDatabaseService db ;
        db = DocLinker.getInstance();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> docxNodes ;
            docxNodes = db.findNodes(Label.label("Docx"));
            while(docxNodes.hasNext()){
                Node node = docxNodes.next();
                if(node.getDegree(RelationshipType.withName("relatedDocxSec")) > 10){
                    Iterable<Relationship> relationships = node.getRelationships(RelationshipType.withName("relatedDocxSec"));
                    while(relationships.iterator().hasNext())
                    {
                        Relationship relationship = relationships.iterator().next();
                        relationship.delete();
                    }
                }

            }
            tx.success();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static int isMatch(String text1,String text2){
        if(text1.equals(text2)){
            return 1;
        }
        else{
            if((text1.contains(text2)) ||(text2.contains(text1))){
                return 2;
            }
        }
        return 0;
    }

    public static List<Map<Long,Set<Long>>> addRelationships(){
        Map<Long,String> titleMap;
        Map<Long,String> titleMap2;
        List<Map<Long,Set<Long>>> nodePair = new ArrayList<>();
        titleMap = DocLinker.getNodeTitle(Label.label("Docx"));
        titleMap2 = DocLinker.getNodeTitle(Label.label("Docx"));
        Map<Long,Integer> cc = new HashMap<>();
        for (Map.Entry<Long, String> entry : titleMap.entrySet()){
            Long id1 = entry.getKey();
            if (!cc.containsKey(id1)) cc.put(id1,0);
            String title1 = entry.getValue();
            Map<Long,Set<Long>> nodeMap = new HashMap<>();
            Set<Long> nodeSet = new HashSet<>();
            titleMap2.remove(id1);
            for(Map.Entry<Long,String> entry2 : titleMap2.entrySet()){
                Long id2 = entry2.getKey();
                String title2 = entry2.getValue();
                if(!title2.equals("")){
                    int grade = isMatch(title1,title2);
                    if(grade != 0){
                        if (!cc.containsKey(id1))cc.put(id1,1);else cc.put(id1,cc.get(id1)+1);
                        if (!cc.containsKey(id2))cc.put(id2,1);else cc.put(id2,cc.get(id2)+1);
                        nodeSet.add(id2);
                    }
                }
            }
            if((cc.get(id1) < 10)&&(cc.get(id1) > 0)){
                if(nodeSet.size()!= 0){
                    nodeMap.put(id1,nodeSet);
                    nodePair.add(nodeMap);
                }
            }
        }
        return nodePair;

    }

    public static void main(String[] args){

        List<Map<Long,Set<Long>>> nodepairs;
        nodepairs = DocLinker.addRelationships();
        DocLinker.createRealtionship(nodepairs);
        DocLinker.deleteRelationship();
        DocLinker.shutdownDB();

        System.out.println("OK!");



    }
}

