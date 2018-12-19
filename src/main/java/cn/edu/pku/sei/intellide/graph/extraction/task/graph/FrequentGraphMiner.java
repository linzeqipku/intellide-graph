package cn.edu.pku.sei.intellide.graph.extraction.task.graph;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    public static GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("E:/graphs/graph-POI"));

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
        mineGraphOfLibrary();
    }

    public static Map<Long,String> getNodesFromNeo4j(Label label) {
        Map<Long,String> textMap = new LinkedHashMap<Long,String>();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<org.neo4j.graphdb.Node> nodes = db.findNodes(label);
            while(nodes.hasNext()){
                org.neo4j.graphdb.Node node = nodes.next();
                String text = (String)node.getProperty("body");
                if(!text.equals("")){
                    textMap.put(node.getId(),text);
                }
            }
            tx.success();
        }
        return textMap;
    }

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
    public static void parseNode(long id) {
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
                    obj.add(graphInfo);
                    Graph<NodeInfo, Integer> g = GraphBuilder.convertToParsemisGraph(graphInfo);
                    Iterator<Node<NodeInfo, Integer>> ite = g.nodeIterator();
                    while (ite.hasNext()) {
                        Node<NodeInfo, Integer> n = ite.next();
                    }
                }
            }
        }
    }

    public static void mineGraphOfLibrary() {
        Map<Long, String> questionMap = getNodesFromNeo4j(Label.label("StackOverflowQuestion"));
        Map<Long, String> answerMap = getNodesFromNeo4j(Label.label("StackOverflowAnswer"));
        try(Transaction tx = db.beginTx()){
            for (Map.Entry<Long, String> entry : questionMap.entrySet()) {
				if (obj.size() > 500)
					break;
                parseNode(entry.getKey());
            }
            for (Map.Entry<Long, String> entry : answerMap.entrySet()) {
				if (obj.size() > 1000)
					break;
                parseNode(entry.getKey());
            }
            tx.success();
        }

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
            int cnt = 0;
            for (GraphInfo graphInfo: frequentSubgraphsFromFilter) {
                VerbalPhraseStructureInfo vp = GraphParser.parseGraphToPhraseStructure(graphInfo);
                if (vp != null) {
                    System.out.println(vp);
                    cnt++;
                }
            }
            System.out.println("*************************");
            System.out.println(cnt);
        }
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
        settings.minFreq = new IntFrequency(5);// iTextPdf项目只能用3，否则内存就不够了
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