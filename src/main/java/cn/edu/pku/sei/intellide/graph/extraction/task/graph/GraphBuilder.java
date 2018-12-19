package cn.edu.pku.sei.intellide.graph.extraction.task.graph;

import cn.edu.pku.sei.intellide.graph.extraction.task.utils.GraphUtils;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.EdgeInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.GraphInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NodeInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NodeLabel;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NounPhraseStructureInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PrepPhraseStructureInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.VerbalPhraseStructureInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.*;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.NLPParser;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.PhraseExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.PhraseFilter;
//import cn.edu.pku.sei.tsr.dragon.test.GraphTest;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Stemmer;
import de.parsemis.graph.Graph;
import de.parsemis.graph.ListGraph;
import de.parsemis.graph.Node;
import edu.stanford.nlp.trees.Tree;

import java.util.HashMap;
import java.util.Iterator;

//import cn.edu.pku.sei.tsr.dragon.content.entity.PhraseInfo;

public class GraphBuilder {
//	public static final Logger	logger		= Logger.getLogger(GraphBuilder.class);
	public static final int		EDGETYPE	= 0;

	public static ListGraph<NodeInfo, Integer> buildGraph(VerbalPhraseStructureInfo vps) {
		GraphInfo graphInfo = buildGraphInfoFromStructure(vps);
		return convertToParsemisGraph(graphInfo);
	}

	public static GraphInfo addNodeIndexNumber(GraphInfo graph) {
		if (graph == null || graph.getNodeList().size() <= 0)
			return null;
		for (int i = 0; i < graph.getNodeList().size(); i++) {
			graph.getNodeList().get(i).setNumber(i);
		}
		return graph;
	}

	/**
	 * Convert our GraphInfo object to ParSeMis graph.
	 * 
	 * @param graphInfo
	 * @return
	 */
	public static ListGraph<NodeInfo, Integer> convertToParsemisGraph(GraphInfo graphInfo) {
		if (graphInfo == null || graphInfo.getNodeList().size() <= 0)
			return null;

		ListGraph<NodeInfo, Integer> graph = new ListGraph<>(graphInfo.getName());

		HashMap<String, Node<NodeInfo, Integer>> nodeMap = new HashMap<>();
		for (int i = 0; i < graphInfo.getNodeList().size(); i++) {
			NodeInfo node = graphInfo.getNodeList().get(i);
			if (node.getUuid() == null)
				node.setUuid();
			Node<NodeInfo, Integer> addedNode = graph.addNode(node);
			nodeMap.put(node.getUuid(), addedNode);
		}

		for (EdgeInfo edge : graphInfo.getEdgeList()) {
			Node<NodeInfo, Integer> nodeA = nodeMap.get(edge.getNodeA().getUuid());
			Node<NodeInfo, Integer> nodeB = nodeMap.get(edge.getNodeB().getUuid());
			if (graph.addEdge(nodeA, nodeB, EDGETYPE, edge.getDirection()) == null) {
				System.err.println("==============================");
				System.err.println(graphInfo);
				System.err.println(GraphUtils.toString(graph));
				System.err.println(edge);
			}
		}
		return graph;
	}

	/**
	 * Build graphInfo from VP structure.
	 * 
	 * @param vps
	 * @return
	 */
	public static GraphInfo buildGraphInfoFromStructure(VerbalPhraseStructureInfo vps) {
		GraphInfo graph = new GraphInfo(vps.toString());

		// 加入根节点：VP
		NodeInfo root = new NodeInfo(NodeLabel.VP, null);
		int index = graph.addNode(root);
		root.setNumber(index);
		graph.setRoot(root);

		// VP一定要有verb
		NodeInfo verb = new NodeInfo(NodeLabel.verb, vps.getVerb().toString());
		index = graph.addNode(verb);
		verb.setNumber(index);
		graph.addEdge(root, verb, EdgeInfo.OUTGOING);

		// 可能有助词particle
		if (vps.getParticle() != null) {
			NodeInfo particle = new NodeInfo(NodeLabel.conj, vps.getParticle().toString());
			index = graph.addNode(particle);
			particle.setNumber(index);
			graph.addEdge(root, particle, EdgeInfo.OUTGOING);
		}

		// subNP
		if (vps.getSubNP() != null) {
			GraphInfo subNPGraph = buildGraphInfoFromStructure(vps.getSubNP()); // np建一个子图
			// 根据现在的逻辑，添加边的时候已经不会新加结点了
			graph.addEdge(root, subNPGraph.getRoot(), EdgeInfo.OUTGOING); // 把子图的根np挂在root下
			graph.merge(subNPGraph); // 这样的话，添加节点和边的顺序都是按照深度优先遍历的过程
		}

		// 多个PP
		for (int i = 0; i < vps.getSubPPList().size(); i++) {
			GraphInfo subPPGraph = buildGraphInfoFromStructure(vps.getSubPPList().get(i)); // pp建一个子图
			graph.addEdge(root, subPPGraph.getRoot(), EdgeInfo.OUTGOING); // 把子图的根pp挂在root下
			graph.merge(subPPGraph);
		}

		return graph;
	}

	/**
	 * Build graphInfo from NP structure.
	 * 
	 * @param nps
	 * @return
	 */
	public static GraphInfo buildGraphInfoFromStructure(NounPhraseStructureInfo nps) {
		GraphInfo graph = new GraphInfo(nps.toString());

		// 加入根节点：NP
		NodeInfo root = new NodeInfo(NodeLabel.NP, null);
		int index = graph.addNode(root);
		root.setNumber(index);
		graph.setRoot(root);

		// 处理wordchain
		for (int i = 0; i < nps.getWordChain().size(); i++) {
			WordInfo word = nps.getWordChain().get(i);
			NodeInfo wordNode;
			if (word instanceof VerbInfo)
				wordNode = new NodeInfo(NodeLabel.verb, word.toString());
			else if (word instanceof NounInfo)
				wordNode = new NodeInfo(NodeLabel.noun, word.toString());
			else if (word instanceof AdjectiveInfo)
				wordNode = new NodeInfo(NodeLabel.adj, word.toString());
			else if (word instanceof ConjunctionInfo)
				wordNode = new NodeInfo(NodeLabel.conj, word.toString());
			else
				wordNode = new NodeInfo(NodeLabel.other, word.toString());
			index = graph.addNode(wordNode);
			wordNode.setNumber(index);
			graph.addEdge(root, wordNode, EdgeInfo.OUTGOING);
		}
		// subPP
		if (nps.getSubPP() != null) {
			GraphInfo subPPGraph = buildGraphInfoFromStructure(nps.getSubPP());// pp建一个子图
			graph.addEdge(root, subPPGraph.getRoot(), EdgeInfo.OUTGOING);// 把子图的根np挂在root下
			graph.merge(subPPGraph);// 这样的话，添加节点和边的顺序都是按照深度优先遍历的过程
		}

		return graph;
	}

	/**
	 * Build graphInfo from PP structure.
	 * 
	 * @param pps
	 * @return
	 */
	public static GraphInfo buildGraphInfoFromStructure(PrepPhraseStructureInfo pps) {
		GraphInfo graph = new GraphInfo(pps.toString());

		// 加入根节点：PP
		NodeInfo root = new NodeInfo(NodeLabel.PP, null);
		int index = graph.addNode(root);
		root.setNumber(index);
		graph.setRoot(root);

		// 介词短语中的核心介词
		if (pps.getConjunction() != null) {
			NodeInfo conj = new NodeInfo(NodeLabel.conj, pps.getConjunction().toString());
			int idx = graph.addNode(conj);
			conj.setNumber(idx);
			graph.addEdge(root, conj, EdgeInfo.OUTGOING);
		}
		// subNP
		if (pps.getSubNP() != null) {
			GraphInfo subNPGraph = buildGraphInfoFromStructure(pps.getSubNP()); // np建一个子图
			graph.addEdge(root, subNPGraph.getRoot(), EdgeInfo.OUTGOING); // 把子图的根np挂在root下
			graph.merge(subNPGraph);
		}
		// subPP
		if (pps.getSubPP() != null) {
			GraphInfo subPPGraph = buildGraphInfoFromStructure(pps.getSubPP());// pp建一个子图
			graph.addEdge(root, subPPGraph.getRoot(), EdgeInfo.OUTGOING);// 把子图的根pp挂在root下
			graph.merge(subPPGraph);
		}

		return graph;
	}

	public static void main(String[] args) {
		String string = "This program is designed to iterate over all the row cells and convert date strings from American formats to euro format.";
		// "I tried to add an object to files.";
		// "This program is designed to convert all the time string from American formats to either
		// Chinese format or the euro format.";
		// "Would you please set up the database for further processing?";
		// "I've created JTextArea to append the elements but the results is 'loaded' instead of
		// being listed out.";
		// "I'm playing around with natural language parse trees, he want to parse strings to
		// trees.";
		// " it's tree parsing.";
		// "He wants to put that into this box, I hope I can move these from there to his room.";
		// "he'll be a dancer, and he wouldn't said that his father might be a farmer, his family
		// will never be Chinese.";
		// "Android: how to get the ConnectionTimeOut Value set to a HttpClient";
		// "Here is the code I'm using to create my multithreaded httpclient object. I'm trying to
		// have a 4 second timeout across the board so if nothing happens for 4 seconds to drop the
		// requests. ";
		// "Well,I think you should go with the 1 st Approach by using Front Controller pattern. It
		// should consist of only a SINGLE SERVLET which provides a centralized entry point for all
		// requests.This servlet will delegate all request to the required servlet. You need to do
		// only following thing to apply the front controller pattern in your application:";

//		SentenceInfo sentence = new SentenceInfo(string);
//		SentenceParser.parseGrammaticalTree(sentence);
//		sentence.getGrammaticalTree().pennPrint();
//		PhraseExtractor.extractVerbPhrases(sentence);

//		for (PhraseInfo phrase : sentence.getPhrases()) {
//			PhraseFilter.filter(phrase, sentence);
		Tree t = NLPParser.parseGrammaticalTree(string);
		t.pennPrint();
		PhraseInfo[] ps = PhraseExtractor.extractVerbPhrases(t);

		for (PhraseInfo phrase : ps) {
			// if (phrase.getProofTotalScore() >= Proof.MID) {
//			VerbalPhraseStructureInfo vp = new VerbalPhraseStructureInfo(phrase);
			phrase.getSyntaxTree();
			PhraseFilter.filter(phrase, string);
			if (phrase.getProofScore() <= 0)
				continue;
			Tree stemmedPhraseTree = Stemmer.stemTree(Tree.valueOf(phrase.getSyntaxTree()));
			VerbalPhraseStructureInfo vp = new VerbalPhraseStructureInfo(stemmedPhraseTree);
			GraphInfo graphInfo = buildGraphInfoFromStructure(vp);
			System.out.println("=================");
			System.out.println(vp);
			System.out.println(graphInfo);
			Graph<NodeInfo, Integer> g = convertToParsemisGraph(graphInfo);
			System.out.println(GraphUtils.toString(g));
			Iterator<Node<NodeInfo, Integer>> ite = g.nodeIterator();
			while (ite.hasNext()) {
				Node<NodeInfo, Integer> n = ite.next();
				System.out.println(n.getLabel().toStringWithOrderNumber()); // label 是NodeInfo类型
			}
		}
//		GraphInfo graphInfo = GraphTest.graph();
//		System.out.println("=================");
//		System.out.println(graphInfo);
//		System.out.println(GraphUtils.toString(convertToParsemisGraph(graphInfo)));

		// for (NodeInfo node : graphInfo.getNodeList()) {
		// String str = node.toString();
		// System.out.println(str);
		// System.out.println(parseStringToNodeInfo(str));
		// }
	}

}
