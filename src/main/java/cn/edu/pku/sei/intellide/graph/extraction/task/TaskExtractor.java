package cn.edu.pku.sei.intellide.graph.extraction.task;

import java.util.*;
import java.io.File;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.qa.StackOverflowExtractor;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.GraphInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NodeInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.VerbalPhraseStructureInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.graph.FrequentGraphMiner;
import cn.edu.pku.sei.intellide.graph.extraction.task.graph.GraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.*;
import de.parsemis.graph.Graph;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class TaskExtractor extends KnowledgeExtractor{
    public static final Label TASK = Label.label("Task");
    public static final String TASK_TEXT = "text";
    public static final String TASK_LEVEL = "level";
    public static final RelationshipType FUNCTIONALFEATURE = RelationshipType.withName("functionalFeature");
    public static final RelationshipType GENERALIZATION = RelationshipType.withName("generalization");

    public FrequentGraphMiner miner = new FrequentGraphMiner();
//    public GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("E:/graphs/graph-lucene"));

    @Override
    public void extraction(){
        Map<Long, String> questionMap = getNodesFromNeo4j(StackOverflowExtractor.QUESTION);
        Map<Long, String> answerMap = getNodesFromNeo4j(StackOverflowExtractor.ANSWER);
        System.out.println("extract task of level 2");
        //extract task of level 2
        for (Map.Entry<Long, String> entry : questionMap.entrySet()) {
            extractPhrase(entry.getKey());
        }
        for (Map.Entry<Long, String> entry : answerMap.entrySet()) {
            extractPhrase(entry.getKey());
        }
        System.out.println("extract task of level 1");
        //extract task of level 1
        extractTask(questionMap, answerMap);
    }

    public Map<Long,String> getNodesFromNeo4j(Label label) {
        Map<Long,String> textMap = new LinkedHashMap<Long,String>();
        GraphDatabaseService db = this.getDb();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodes = db.findNodes(label);
            while(nodes.hasNext()){
                Node node = nodes.next();
                String text = "";
                if (label == StackOverflowExtractor.QUESTION || label == StackOverflowExtractor.ANSWER) {
                    text = (String)node.getProperty("body");
                }
                if (label == TaskExtractor.TASK) {
                    String level = (String)node.getProperty(TASK_LEVEL);
                    if (level.equals("2")) {
                        text = (String)node.getProperty(TaskExtractor.TASK_TEXT);
                    }
                }
                if(!text.equals("")){
                    textMap.put(node.getId(),text);
                }
            }
            tx.success();
        }
        return textMap;
    }

    public void extractPhrase(long id) {
        GraphDatabaseService db = this.getDb();
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
                        taskNode.setProperty(TASK_TEXT, phraseInfo.getText());
                        taskNode.setProperty(TASK_LEVEL, "2");
                        textNode.createRelationshipTo(taskNode, FUNCTIONALFEATURE);
                        System.out.println(sentence);
                        System.out.println(phraseInfo.getText());
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

    public void extractTask(Map<Long, String> questionMap, Map<Long, String> answerMap) {
        GraphDatabaseService db = this.getDb();
        miner.setQuestionMap(questionMap);
        miner.setAnswerMap(answerMap);
        try(Transaction tx = db.beginTx()){
            for (Map.Entry<Long, String> entry : questionMap.entrySet()) {
                if (miner.getObjSize() > 300)
                    break;
                parseNode(entry.getKey());
            }
            for (Map.Entry<Long, String> entry : answerMap.entrySet()) {
                if (miner.getObjSize() > 600)
                    break;
                parseNode(entry.getKey());
            }
            HashSet<String> taskes = miner.mineGraph();
            Map<Long, String> phraseMap = getNodesFromNeo4j(TaskExtractor.TASK);
            for (String task: taskes) {
                Node taskNode = db.createNode(TASK);
                taskNode.setProperty(TASK_TEXT, task);
                taskNode.setProperty(TASK_LEVEL, "1");
                for (Map.Entry<Long, String> entry : phraseMap.entrySet()) {
                    Node phraseNode = db.getNodeById(entry.getKey());
                    String phrase = (String)phraseNode.getProperty(TASK_TEXT);
                    if (phrase.contains(task)) {
                        phraseNode.createRelationshipTo(taskNode, GENERALIZATION);
                    }
                }
            }
            tx.success();
        }
    }

    public void parseNode(long id) {
        GraphDatabaseService db = this.getDb();
        org.neo4j.graphdb.Node textNode = db.getNodeById(id);
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
                    Tree stemmedPhraseTree = Stemmer.stemTree(Tree.valueOf(phraseInfo.getSyntaxTree()));
                    VerbalPhraseStructureInfo vp = new VerbalPhraseStructureInfo(stemmedPhraseTree);
                    GraphInfo graphInfo = GraphBuilder.buildGraphInfoFromStructure(vp);
                    miner.addToObj(graphInfo);
                    Graph<NodeInfo, Integer> g = GraphBuilder.convertToParsemisGraph(graphInfo);
                    Iterator<de.parsemis.graph.Node<NodeInfo, Integer>> ite = g.nodeIterator();
                    while (ite.hasNext()) {
                        de.parsemis.graph.Node<NodeInfo, Integer> n = ite.next();
                    }
                }
            }
        }
    }

}