package cn.edu.pku.sei.intellide.graph.extraction.task;

import java.util.*;
import java.io.File;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import edu.stanford.nlp.util.CoreMap;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


public class TaskExtractor extends KnowledgeExtractor{
    public static final Label QUESTION = Label.label("StackOverflowQuestion");
    public static final Label ANSWER = Label.label("StackOverflowAnswer");
    public static final Label TASK = Label.label("Task");
    public static final String BODY = "body";
    public static final String TEXT = "text";
    public static final String PROOFSCORE = "proofScore";
    public static final RelationshipType FUNCTIONALFEATURE = RelationshipType.withName("functionalFeature");

    public GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("E:/graphs/graph-lucene"));

    @Override
    public void extraction() {
        Map<Long, String> questionMap = getNodesFromNeo4j(QUESTION);
        Map<Long, String> answerMap = getNodesFromNeo4j(ANSWER);
        for (Map.Entry<Long, String> entry : questionMap.entrySet()) {
            extractTask(entry.getKey());
        }
        for (Map.Entry<Long, String> entry : answerMap.entrySet()) {
            extractTask(entry.getKey());
        }
    }


    public Map<Long,String> getNodesFromNeo4j(Label label) {
        Map<Long,String> textMap = new LinkedHashMap<Long,String>();
//        GraphDatabaseService db = this.getDb();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodes = db.findNodes(label);
            while(nodes.hasNext()){
                Node node = nodes.next();
                String text = (String)node.getProperty(BODY);
                if(!text.equals("")){
                    textMap.put(node.getId(),text);
                }
            }
            tx.success();
        }
        return textMap;
    }


    public void extractTask(long id) {
//        GraphDatabaseService db = this.getDb();
        try(Transaction tx = db.beginTx()){
            Node textNode = db.getNodeById(id);
            String body = textNode.getProperty("body").toString();
            Document doc = Jsoup.parse(body);
            doc.select("code").remove();
            String text = doc.text();
            List<String> sentences = splitText(text);
            for (String sentence: sentences) {
                Tree tree = NLPParser.parseGrammaticalTree(sentence);
                PhraseInfo[] verbPhrases = PhraseExtractor.extractVerbPhrases(tree);
                for (PhraseInfo phraseInfo : verbPhrases) {
                    PhraseFilter.filter(phraseInfo, sentence);
                    if (phraseInfo.getProofScore() > 0) {
                        Node taskNode = db.createNode(TASK);
                        taskNode.setProperty(TEXT, phraseInfo.getText());
                        taskNode.setProperty(PROOFSCORE, phraseInfo.getText());
                        textNode.createRelationshipTo(taskNode, FUNCTIONALFEATURE);
                        System.out.println(phraseInfo.getText());
//                        System.out.println("proofScore: " + phraseInfo.getProofScore());
//                        System.out.println(phraseInfo.getProofString());
                    }
                }
            }
            tx.success();
        }

    }


    public static List<String> splitText(String text) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        List<String> sentencesText = new ArrayList<>();
        for (CoreMap sentence: sentences) {
            sentencesText.add(sentence.toString());
        }
        return sentencesText;
    }

}