package cn.edu.pku.sei.intellide.graph.extraction.task.graph;

import cn.edu.pku.sei.intellide.graph.extraction.task.utils.GraphUtils;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.*;
//import cn.edu.pku.sei.tsr.dragon.nlp.entity.NounPhraseStructureInfo;
//import cn.edu.pku.sei.tsr.dragon.nlp.entity.PrepPhraseStructureInfo;
//import cn.edu.pku.sei.tsr.dragon.nlp.entity.VerbalPhraseStructureInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.*;
//import cn.edu.pku.sei.tsr.dragon.test.GraphTest;
import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

public class GraphParser {
//	public static final Logger logger = Logger.getLogger(GraphParser.class);

	public static GraphInfo parseGraphAndValidate(Graph<NodeInfo, Integer> graph) {
		if (graph == null)
			return null;
		GraphInfo graphInfo = new GraphInfo();
		boolean isVPRoot = false;
		boolean containsVerbLeaf = false;
		boolean containsNounLeaf = false;

		HashMap<Integer, NodeInfo> nodeIndex = new HashMap<>();// 原图中的index，新的node

		Iterator<Node<NodeInfo, Integer>> nodeIterator = graph.nodeIterator();
		while (nodeIterator.hasNext()) {
			Node<NodeInfo, Integer> curNode = nodeIterator.next();
			NodeInfo nodeInfo = curNode.getLabel();
			if (nodeInfo != null) {
				// NodeInfo existed = graphInfo.findEqualNode(nodeInfo);
				// if (existed != null) {
				// // 内存对象重复，也就是Parsemis结点的label是内存相同的
				// // 如果该节点已存在，就把index关联到存在的节点上，这样建边时如有这个index的相关边，则从那个节点上建
				// nodeIndex.put(curNode.getIndex(), existed);
				// }
				graphInfo.addNode(nodeInfo);// 带着从原来图中得到的，随着挖掘子图带出来的，orderNumber
				nodeIndex.put(curNode.getIndex(), nodeInfo);// 按照原图中的index添加到索引，便于建边时查找

				if (!isVPRoot && curNode.getInDegree() == 0) {
					// 还没找到根的时候发现一个入度为0，树根
					graphInfo.setRoot(nodeInfo);
					if (nodeInfo.getLabel() == NodeLabel.VP)
						isVPRoot = true;
				}
				if (nodeInfo.getLabel() == NodeLabel.verb)
					containsVerbLeaf = true;
				if (nodeInfo.getLabel() == NodeLabel.noun)
					containsNounLeaf = true;
			}
		}

		if (!isVPRoot || !containsNounLeaf || !containsVerbLeaf) {
			return null;
		}

		Iterator<Edge<NodeInfo, Integer>> edgeIterator = graph.edgeIterator();
		while (edgeIterator.hasNext()) {
			Edge<NodeInfo, Integer> curEdge = edgeIterator.next();
			int nodeAIndex = curEdge.getNodeA().getIndex();
			int nodeBIndex = curEdge.getNodeB().getIndex();
			int direction = curEdge.getDirection();

			if (direction == Edge.INCOMING) // 都存成出边
				graphInfo.addEdge(nodeIndex.get(nodeBIndex), nodeIndex.get(nodeAIndex),
						EdgeInfo.OUTGOING);
			else
				graphInfo.addEdge(nodeIndex.get(nodeAIndex), nodeIndex.get(nodeBIndex), direction);
		}

		return graphInfo;
	}

	public static VerbalPhraseStructureInfo parseGraphToPhraseStructure(GraphInfo graph) {
		if (graph != null) {
			TreeInfo tree = parseGraphToTree(graph);
			if (tree != null)
				return parseTreeToVPStructure(tree);
		}
		return null;
	}

	public static VerbalPhraseStructureInfo parseTreeToVPStructure(TreeInfo tree) {
		VerbalPhraseStructureInfo vpStructure = new VerbalPhraseStructureInfo();
		tree.getChildren().forEach(x -> {
			NodeLabel label = x.getLabel();
			switch (label) {
			case verb:
				vpStructure.setVerb(new VerbInfo(x.getValue()));
				break;
			case conj:
				vpStructure.setParticle(new ConjunctionInfo(x.getValue()));
				break;
			case NP:
				vpStructure.setSubNP(parseTreeToNPStructure(x));
				break;
			case PP:
				vpStructure.addSubPP(parseTreeToPPStructure(x));
				break;
			default:
				break;
			}
		});
		return vpStructure;
	}

	public static NounPhraseStructureInfo parseTreeToNPStructure(TreeInfo tree) {
		NounPhraseStructureInfo npStructure = new NounPhraseStructureInfo();
		List<WordInfo> wordChain = new ArrayList<>();
		wordChain.addAll(tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.other)
				.map(x -> new WordInfo(x.getValue())).collect(Collectors.toList()));
		wordChain.addAll(tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.conj)
				.map(x -> new ConjunctionInfo(x.getValue())).collect(Collectors.toList()));
		wordChain.addAll(tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.adj)
				.map(x -> new AdjectiveInfo(x.getValue())).collect(Collectors.toList()));
		wordChain.addAll(tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.noun)
				.map(x -> new NounInfo(x.getValue())).collect(Collectors.toList()));
		npStructure.setWordChain(wordChain);
		TreeInfo ppTree = tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.PP)
				.findFirst().orElse(null);
		if (ppTree != null)
			npStructure.setSubPP(parseTreeToPPStructure(ppTree));
		return npStructure;
	}

	public static PrepPhraseStructureInfo parseTreeToPPStructure(TreeInfo tree) {
		PrepPhraseStructureInfo ppStructure = new PrepPhraseStructureInfo();
		tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.conj).findFirst()
				.ifPresent(x -> ppStructure.setConjunction(new ConjunctionInfo(x.getValue())));
		tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.NP).findFirst()
				.ifPresent(x -> ppStructure.setSubNP(parseTreeToNPStructure(x)));
		tree.getChildren().stream().filter(x -> x.getLabel() == NodeLabel.PP).findFirst()
				.ifPresent(x -> ppStructure.setSubPP(parseTreeToPPStructure(x)));
		return ppStructure;
	}

	// 将图转化为树
	public static TreeInfo parseGraphToTree(GraphInfo graph) {
		List<NodeInfo> roots = graph.getRoots();
		if (roots.size() != 1)
			return null;
		NodeInfo root = roots.get(0);
		TreeInfo tree = generateTreeFromNodeInGraph(root, graph);
		return tree;
	}

	// 因为图中存在回路，所以建树时遇到不是树的图，就要跳过，为此调用修改后的方法
	// 从图中一个结点生成图中以其为根的树
	public static TreeInfo generateTreeFromNodeInGraph(NodeInfo node, GraphInfo graph) {
		return generateTreeFromNodeInGraph(node, graph, new ArrayList<NodeInfo>());
	}

	// 从图中一个结点生成图中以其为根的树，考虑环路处理，增加记录祖先历史
	public static TreeInfo generateTreeFromNodeInGraph(NodeInfo node, GraphInfo graph,
			List<NodeInfo> ancestors) {
		if (!graph.contains(node))
			return null;
		if (GraphUtils.containsNodeObj(ancestors, node)) // 祖先中包含了当前结点，说明存在环路，退出
			return null;
		ancestors.add(node);

		TreeInfo tree = new TreeInfo(node);
		List<NodeInfo> successors = graph.getSuccessors(node);
		successors.sort(new Comparator<NodeInfo>() {// 按照结点编号（先根遍历的顺序号）的升序排列
			@Override
			public int compare(NodeInfo n1, NodeInfo n2) {
				return n1.getNumber() - n2.getNumber();
			}
		});
		for (NodeInfo successor : successors) {
			TreeInfo child = generateTreeFromNodeInGraph(successor, graph, ancestors);
			if (child == null)
				return null;
			tree.addChild(child);
		}
		return tree;
	}

	public static void main(String[] args) {
//		GraphInfo g1 = GraphTest.graph();
//		System.out.println(g1);
//		VerbalPhraseStructureInfo vp = parseTreeToVPStructure(parseGraphToTree(g1));
//		System.out.println(vp);
		// Graph<NodeInfo, Integer> g2 = GraphBuilderOfStringNode.convertToParsemisGraph(g1);
		// System.out.println("G1:" + g1);
		// System.out.println("G2:" + GraphBuilderOfStringNode.getGraphString(g2));
		// GraphInfo g3 = parseGraphAndValidate(g2);
		// System.out.println("G3:" + g3);
	}

	// 只parse，不验证，已废弃
	@Deprecated
	public static GraphInfo parseGraph(Graph<NodeInfo, Integer> graph) {
		if (graph == null)
			return null;
		GraphInfo graphInfo = new GraphInfo();

		HashMap<Integer, NodeInfo> nodeIndex = new HashMap<>();// 原图中的index，新的node

		Iterator<Node<NodeInfo, Integer>> nodeIterator = graph.nodeIterator();
		while (nodeIterator.hasNext()) {
			Node<NodeInfo, Integer> curNode = nodeIterator.next();
			NodeInfo nodeInfo = curNode.getLabel();
			if (nodeInfo != null) {
				graphInfo.addNode(nodeInfo);// 带着从原来图中得到的，随着挖掘子图带出来的，orderNumber
				nodeIndex.put(curNode.getIndex(), nodeInfo);// 按照原图中的index添加到索引，便于建边时查找
				if (curNode.getInDegree() == 0) // 入度为0，树根
					graphInfo.setRoot(nodeInfo);
			}
		}

		Iterator<Edge<NodeInfo, Integer>> edgeIterator = graph.edgeIterator();
		while (edgeIterator.hasNext()) {
			Edge<NodeInfo, Integer> curEdge = edgeIterator.next();
			int nodeAIndex = curEdge.getNodeA().getIndex();
			int nodeBIndex = curEdge.getNodeB().getIndex();
			int direction = curEdge.getDirection();

			if (direction == Edge.INCOMING) // 都存成出边
				graphInfo.addEdge(nodeIndex.get(nodeBIndex), nodeIndex.get(nodeAIndex),
						EdgeInfo.OUTGOING);
			else
				graphInfo.addEdge(nodeIndex.get(nodeAIndex), nodeIndex.get(nodeBIndex), direction);
		}

		return graphInfo;
	}
}
