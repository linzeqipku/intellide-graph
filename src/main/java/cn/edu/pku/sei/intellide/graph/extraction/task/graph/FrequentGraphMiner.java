package cn.edu.pku.sei.intellide.graph.extraction.task.graph;

import cn.edu.pku.sei.intellide.graph.extraction.task.TaskExtractor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.VerbalPhraseStructureInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.NLPParser;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.PhraseExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.PhraseFilter;
//import cn.edu.pku.sei.tsr.dragon.test.GraphTest;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Stemmer;
import de.parsemis.graph.Node;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

//import cn.edu.pku.sei.tsr.dragon.experiment.APILibrary;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.GraphUtils;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.GraphInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NodeInfo;
//import cn.edu.pku.sei.tsr.dragon.graph.graphofnodeinfo.*;
//import cn.edu.pku.sei.tsr.dragon.utils.ObjectIO;
import de.parsemis.Miner;
import de.parsemis.graph.Graph;
import de.parsemis.graph.ListGraph;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.environment.Statistics;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.IntFrequency;
import de.parsemis.parsers.LabelParser;
import de.parsemis.strategy.ThreadedDFSStrategy;

/**
 * 读入Graph的列表，然后进行子图挖掘，并调用{@code GraphFilterOfStringNode.filter()}进行初步的过滤。使用NodeInfo作为Graph结点类型
 *
 * @author ZHUZixiao
 *
 */
public class FrequentGraphMiner {
//	public static final Logger logger = Logger.getLogger(FrequentGraphMiner.class);

    private static String								libraryName;
    private static List<Graph<NodeInfo, Integer>> graphs				= new ArrayList<>();
    private static List<Fragment<NodeInfo, Integer>>	frequentSubgraphs	= new ArrayList<>();
    // 为了保留frequency信息，只能用Fragment来存

    private static List<GraphInfo> obj = new ArrayList<>();
    private static Map<Long, String> questionMap;
    private static Map<Long, String> answerMap;

//    public static GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("E:/graphs/graph-lucene"));

    public static void reset(String _libraryName) {
        for (Fragment<NodeInfo, Integer> fragment : frequentSubgraphs) {
            fragment.clear();
            fragment = null;
        }
        graphs = new ArrayList<>();
        frequentSubgraphs = new ArrayList<>();
        libraryName = _libraryName;
    }

    public static void main(String[] args) {
//        setQuestionMap(getNodesFromNeo4j(Label.label("StackOverflowQuestion")));
//        setAnswerMap(getNodesFromNeo4j(Label.label("StackOverflowAnswer")));
        mineGraph();
    }

//    public static Map<Long,String> getNodesFromNeo4j(Label label) {
//        Map<Long,String> textMap = new LinkedHashMap<Long,String>();
//        try(Transaction tx = db.beginTx()){
//            ResourceIterator<org.neo4j.graphdb.Node> nodes = db.findNodes(label);
//            while(nodes.hasNext()){
//                org.neo4j.graphdb.Node node = nodes.next();
//                String text = (String)node.getProperty("body");
//                if(!text.equals("")){
//                    textMap.put(node.getId(),text);
//                }
//            }
//            tx.success();
//        }
//        return textMap;
//    }

    public static List<String> splitText(String text) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> sentencesText = new ArrayList<>();
        for (CoreMap sentence: sentences) {
            sentencesText.add(sentence.toString());
        }
        return sentencesText;
    }

    public static HashSet<String> mineGraph() {
        HashSet<String> phrases = new HashSet<>();
        if (obj != null && obj instanceof List<?>) {
            System.out.println("******************************************************************");
            System.out.println(obj.size());
            System.out.println(obj);
            List<GraphInfo> graphList = (List<GraphInfo>) obj;
            for (GraphInfo graphInfo : graphList) {
                // 原来存的Graph里的node，都不包含number属性，所以要加进去。
                // GraphBuilderOfStringNode.addNodeIndexNumber(graphInfo);
                Graph<NodeInfo, Integer> graph = GraphBuilder.convertToParsemisGraph(graphInfo);
                if (graph != null) {
                    graphs.add(graph); // 此时图中的结点编号顺序都是对的，已确认。
                }
            }
            System.out.println("Start to mine frequent subgraphs from [" + graphs.size() + "] graphs...");

            mineFrequentGraph();
            graphs = null; // 试着释放空间

            System.out.println("frequentSubgraphs");
            System.out.println(frequentSubgraphs.size());

            List<GraphInfo> frequentSubgraphsFromFilter = FrequentGraphPostRunner.filter(libraryName);
            System.out.println("frequentSubgraphsFromFilter");
            System.out.println(frequentSubgraphsFromFilter.size());
            for (GraphInfo graphInfo: frequentSubgraphsFromFilter) {
                VerbalPhraseStructureInfo vp = GraphParser.parseGraphToPhraseStructure(graphInfo);
                if (vp != null) {
                    phrases.add(vp.toNLText());
                    System.out.println(vp.toNLText());
                }
            }
            System.out.println("*************************");
            System.out.println(phrases.size());
            System.out.println(phrases);
        }
        return phrases;
    }

    public static List<Fragment<NodeInfo, Integer>> mineFrequentGraph() {
        for (int i = 0; i < graphs.size(); i++) {
            Graph<NodeInfo, Integer> g = graphs.get(i);
            if (g == null) {
//				logger.error("ERROR_NULL_GRAPH!!! INDEX: " + i + "\t" + graphs.get(i));
                System.out.println("ERROR_NULL_GRAPH!!! INDEX: " + i + "\t" + graphs.get(i));
                FrequentGraphMiner.getGraphs().remove(i);
                i--;
            }
        }
        Settings<NodeInfo, Integer> settings = new Settings<>();
        settings.threadCount = 16;
        settings.naturalOrderedNodeLabels = true;
        settings.algorithm = new de.parsemis.algorithms.gSpan.Algorithm<>();
        settings.strategy = new ThreadedDFSStrategy<>(16, new Statistics());
        settings.minFreq = new IntFrequency(4);// iTextPdf项目只能用3，否则内存就不够了
        settings.factory = new ListGraph.Factory<>(new NodeInfoLabelParser(), new IntegerLabelParser());

        Collection<Fragment<NodeInfo, Integer>> result = Miner.mine(graphs, settings);

        frequentSubgraphs.addAll(result);
        result = null;
        return frequentSubgraphs;
    }

    public static String getLibraryName() {
        return libraryName;
    }

    public static void setLibraryName(String libraryName) {
        FrequentGraphMiner.libraryName = libraryName;
    }

    public synchronized static List<Graph<NodeInfo, Integer>> getGraphs() {
        return graphs;
    }

    public static void setGraphs(List<Graph<NodeInfo, Integer>> graphs) {
        FrequentGraphMiner.graphs = graphs;
    }

    public static List<Fragment<NodeInfo, Integer>> getFrequentSubgraphs() {
        return frequentSubgraphs;
    }

    public static void setFrequentSubgraphs(List<Fragment<NodeInfo, Integer>> frequentSubgraphs) {
        FrequentGraphMiner.frequentSubgraphs = frequentSubgraphs;
    }

    public static void setQuestionMap(Map<Long, String> questionMap) { FrequentGraphMiner.questionMap = questionMap; }

    public static void setAnswerMap(Map<Long, String> answerMap) { FrequentGraphMiner.answerMap = answerMap; }

    public static void addToObj(GraphInfo graphInfo) { obj.add(graphInfo); }

    public static int getObjSize() { return obj.size(); }
}

class NodeInfoLabelParser implements LabelParser<NodeInfo> {
    private static final long serialVersionUID = 8844206741183890442L;

    @Override
    public NodeInfo parse(String s) throws ParseException {
        return GraphUtils.parseStringToNodeInfo(s);
    }

    @Override
    public String serialize(NodeInfo node) {
        return node.toString();
    }
}

class IntegerLabelParser implements LabelParser<Integer> {
    private static final long serialVersionUID = 4752511081764291545L;

    @Override
    public Integer parse(String s) throws ParseException {
        return Integer.parseInt(s);
    }

    @Override
    public String serialize(Integer integer) {
        return integer.toString();
    }
}