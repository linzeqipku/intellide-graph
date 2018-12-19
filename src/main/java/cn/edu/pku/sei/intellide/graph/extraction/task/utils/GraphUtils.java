package cn.edu.pku.sei.intellide.graph.extraction.task.utils;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.GraphInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NodeInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NodeLabel;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Rules;
import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GraphUtils {

	public static boolean containsNodeObj(List<NodeInfo> list, NodeInfo node) {
		for (NodeInfo nodeInfo : list) {
			if (nodeInfo.equalsObject(node))
				return true;
		}
		return false;
	}

	public static String toStringWithOrderNumber(Graph<NodeInfo, Integer> graph) {
		StringBuilder str = new StringBuilder("[graph: [node:");
		Iterator<Node<NodeInfo, Integer>> iteratorNode = graph.nodeIterator();
		while (iteratorNode.hasNext()) {
			Node<NodeInfo, Integer> curNode = iteratorNode.next();
			str.append(curNode.getLabel().toStringWithOrderNumber());
			if (iteratorNode.hasNext())
				str.append(", ");
		}
		str.append("][edge:");
		Iterator<Edge<NodeInfo, Integer>> iteratorEdge = graph.edgeIterator();
		while (iteratorEdge.hasNext()) {
			Edge<NodeInfo, Integer> curEdge = iteratorEdge.next();
			String direction;
			switch (curEdge.getDirection()) {
			case Edge.INCOMING:
				direction = "<-";
				break;
			case Edge.OUTGOING:
				direction = "->";
				break;
			case Edge.UNDIRECTED:
				direction = "--";
				break;
			default:
				direction = ", ";
				break;
			}
			str.append(
					"(" + curEdge.getNodeA().getLabel().toStringWithOrderNumber() + " " + direction
							+ " " + curEdge.getNodeB().getLabel().toStringWithOrderNumber() + ")");
			if (iteratorEdge.hasNext())
				str.append(", ");
		}
		str.append("]]");
		return str.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String toString(Graph graph) {
		StringBuilder str = new StringBuilder("[graph: [node:");
		Iterator<Node> iteratorNode = graph.nodeIterator();
		while (iteratorNode.hasNext()) {
			Node curNode = iteratorNode.next();
			str.append(curNode.getLabel());
			if (iteratorNode.hasNext())
				str.append(", ");
		}
		str.append("][edge:");
		Iterator<Edge> iteratorEdge = graph.edgeIterator();
		while (iteratorEdge.hasNext()) {
			Edge curEdge = iteratorEdge.next();
			String direction;
			switch (curEdge.getDirection()) {
			case Edge.INCOMING:
				direction = "<-";
				break;
			case Edge.OUTGOING:
				direction = "->";
				break;
			case Edge.UNDIRECTED:
				direction = "--";
				break;
			default:
				direction = ", ";
				break;
			}
			str.append("(" + curEdge.getNodeA().getLabel() + " " + direction + " "
					+ curEdge.getNodeB().getLabel() + ")");
			if (iteratorEdge.hasNext())
				str.append(", ");
		}
		str.append("]]");
		return str.toString();
	}

	/**
	 * @param text,
	 *            "<label value>" "<label>"
	 * @return
	 */
	public static NodeInfo parseStringToNodeInfo(String text) {
		String[] splitted = text.split("\\<|\\>| ");
		// 长度为3：#<label value>，长度为2：#<label>
		if (splitted == null || splitted.length < 2)
			return null;

		NodeLabel label = NodeLabel.getLabel(splitted[1]);
		String value = splitted.length == 3 ? splitted[2] : null;

		NodeInfo node = new NodeInfo(label, value);

		// 修改过后的NodeString 以宽度优先搜索（先根顺序）的顺序编号开头，然后<>
		// 如果有这个数字就parse出来，没有就不管啦
		try {
			int number = Integer.parseInt(splitted[0]);
			node.setNumber(number);
		}
		catch (NumberFormatException e) {
		}

		return node;
	}

	public static boolean isValidGraph(GraphInfo graph) {
		if (graph == null || graph.getNodeList().size() <= 0)
			return false;
		for (int i = 0; i < graph.getNodeList().size(); i++) {
			NodeInfo node = graph.getNodeList().get(i);
			if (!isValidNode(node))
				return false;
		}
		return true;
	}

	public static boolean isValidNode(NodeInfo node) {
		if (node == null)
			return false;
		String nodeValue = node.getValue();
		if (nodeValue == null) // 短语结点的value为null
			return true;

		if (isTooShort(nodeValue))
			return false;// 单词太短没有意义
		if (!isValidCharacters(nodeValue))
			return false;// 字符不合格
		if (isCODEPlaceholder(nodeValue))
			return false;// 不能是CODExx
		if (node.getLabel() == NodeLabel.verb)
			if (isStopVerb(nodeValue)) // 停用动词
				return false;
		if (node.getLabel() == NodeLabel.noun)
			if (isStopNoun(nodeValue)) // 停用名词
				return false;

		return true;// 经历各种考验之后
	}

	public static boolean isTooShort(String str) {
		boolean isSingleChar = str.length() <= 1 && (!str.matches("[aA]"));// a、an是有效单词
		return isSingleChar;
	}

	public static boolean isValidCharacters(String str) {
		String regexp = "[a-zA-Z0-9|\\-| ]*"; // 严格匹配 不包括下划线、空格以外的字符
		// String regexp = "[\\w|\\-|\\s]*"; // 匹配单词字符 包括所有数字字母下划线连字符空白符
		return str.matches(regexp);
	}

	public static boolean isCODEPlaceholder(String str) {
		String regexp = "(CODE|code)[0-9]+"; // 匹配单词字符 包括所有数字字母下划线
		return str.matches(regexp);
	}

	public static boolean isStopVerb(String str) {// 在规则制定的停用词列表里面过滤动词
		return Rules.qa_verbs.toString().contains(str) || Rules.stop_verbs.toString().contains(str)
				|| Rules.unlike_verbs.toString().contains(str) || Arrays.asList(Rules.BE_VERBS).contains(str)
				|| Arrays.asList(Rules.MODAL_VERBS).contains(str)
				|| Arrays.asList(Rules.HAVE_VERBS).contains(str);
	}

	public static boolean isStopNoun(String str) {// 在规则制定的停用词列表里面过滤名词
		return Rules.qa_nouns.toString().contains(str) || Rules.stop_nouns.toString().contains(str)
				|| Rules.unlike_nouns.toString().contains(str);
	}

	public static void main(String[] args) {
		
		String str="9f0af464-f337-4298-a383-ba50242af265\t2";
		String[] ss=str.split("\\t");
		for (String string : ss) {
			System.err.println("=="+string+"==");
		}
		System.out.println(GraphUtils.isValidCharacters("CODE0"));
		System.out.println(GraphUtils.isValidCharacters("dguey"));
		System.out.println(GraphUtils.isValidCharacters("``xds"));
		System.out.println(GraphUtils.isValidCharacters("$#jck"));
		System.out.println(GraphUtils.isValidCharacters("low9e0Wwcei"));
		System.out.println(GraphUtils.isValidCharacters("isValidCharacters"));
		System.out.println(GraphUtils.isValidCharacters("sub-graph"));
		System.out.println(GraphUtils.isValidCharacters("subGraph"));
		System.out.println(GraphUtils.isValidCharacters("sub_graph"));
		System.out.println(GraphUtils.isValidCharacters("sub graph"));
		System.out.println(GraphUtils.isValidCharacters("get\nsub_graph"));
		System.out.println(GraphUtils.isValidCharacters("get\tsub-graph"));
		System.out.println();
		System.out.println(GraphUtils.isCODEPlaceholder(""));
		System.out.println(GraphUtils.isCODEPlaceholder("isCODE"));
		System.out.println(GraphUtils.isCODEPlaceholder("isCODE9"));
		System.out.println(GraphUtils.isCODEPlaceholder("CODE203280"));
		System.out.println(GraphUtils.isCODEPlaceholder("CODE"));
		System.out.println(GraphUtils.isCODEPlaceholder("code22"));
		System.out.println(GraphUtils.isCODEPlaceholder("CODE1"));
		System.out.println(GraphUtils.isCODEPlaceholder("code0"));
		System.out.println(GraphUtils.isCODEPlaceholder("code"));
		System.out.println();
		System.out.println(GraphUtils.isValidNode(new NodeInfo(NodeLabel.verb, "fuck")));
		System.out.println();
		System.out.println(GraphUtils.isTooShort("a"));
		System.out.println(GraphUtils.isTooShort("an"));
		System.out.println(GraphUtils.isTooShort("A"));
		System.out.println(GraphUtils.isTooShort("An"));
		System.out.println(GraphUtils.isTooShort("b"));
		System.out.println(GraphUtils.isTooShort("x"));
		System.out.println(GraphUtils.isTooShort("M"));
		System.out.println(GraphUtils.isTooShort("_"));
		System.out.println(GraphUtils.isTooShort("-"));
		System.out.println(GraphUtils.isTooShort(""));
	}

}
