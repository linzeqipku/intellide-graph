package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import cn.edu.pku.sei.intellide.graph.extraction.task.utils.UUIDInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GraphInfo implements Serializable, UUIDInterface {
	private static final long serialVersionUID = 5189153241004867175L;

	private String			uuid;
	private String			name;
	private NodeInfo		root;
	private List<NodeInfo>	nodeList;
	private List<EdgeInfo>	edgeList;	// edge是有序的，未来从图中恢复出短语时还要按照加入的顺序，也就是下标，所以不能是set

	public GraphInfo() {
		uuid = UUID.randomUUID().toString();
		nodeList = new ArrayList<>();
		edgeList = new ArrayList<>();
	}

	public GraphInfo(String name) {
		this();
		setName(name);
	}

	/**
	 * 按照值来比较，如果使用contains不添加重复结点，则有可能使得值同实不同的结点没有加进去。 因此所有想加的都加进去，不添加重复结点要靠建图时的逻辑来保证
	 * 
	 * @param graphs
	 * @return
	 */
	public GraphInfo merge(GraphInfo... graphs) {
		// 不能用addAll，万一已经存在则重复了！
		for (int i = 0; i < graphs.length; i++) {
			for (int j = 0; j < graphs[i].getNodeList().size(); j++) {
				NodeInfo node = graphs[i].getNodeList().get(j);
				int index = addNode(node);
				node.setNumber(index);// 要更新index, 把原來图中的顺序编号更换成新图中的编号

			}
			for (int j = 0; j < graphs[i].getEdgeList().size(); j++) {
				EdgeInfo edge = graphs[i].getEdgeList().get(j);
				addEdge(edge);
			}
		}
		return this;
	}

	public List<EdgeInfo> getOutEdges(NodeInfo node) {
		List<EdgeInfo> edges = new ArrayList<>();
		if (node == null || !contains(node))
			return edges;

		for (EdgeInfo edge : edgeList) {
			switch (edge.getDirection()) {
			case EdgeInfo.INCOMING:// A <- B
				if (node.equalsObject(edge.getNodeB()))
					edges.add(edge);
				break;
			case EdgeInfo.OUTGOING: // A -> B
				if (node.equalsObject(edge.getNodeA()))
					edges.add(edge);
				break;
			case EdgeInfo.UNDIRECTED: // A -- B
				if (node.equalsObject(edge.getNodeA()) || node.equalsObject(edge.getNodeB()))
					edges.add(edge);
				break;
			default:
				break;
			}
		}
		return edges;
	}

	public List<EdgeInfo> getInEdges(NodeInfo node) {
		List<EdgeInfo> edges = new ArrayList<>();
		if (node == null || !contains(node))
			return edges;

		for (EdgeInfo edge : edgeList) {
			switch (edge.getDirection()) {
			case EdgeInfo.INCOMING:// A <- B
				if (node.equalsObject(edge.getNodeA()))
					edges.add(edge);
				break;
			case EdgeInfo.OUTGOING: // A -> B
				if (node.equalsObject(edge.getNodeB()))
					edges.add(edge);
				break;
			case EdgeInfo.UNDIRECTED: // A -- B
				if (node.equalsObject(edge.getNodeA()) || node.equalsObject(edge.getNodeB()))
					edges.add(edge);
				break;
			default:
				break;
			}
		}
		return edges;
	}

	public List<NodeInfo> getRoots() {
		List<NodeInfo> roots = new ArrayList<>();

		for (NodeInfo node : nodeList) {
			if (node != null && getInEdges(node).size() == 0)
				roots.add(node);
		}
		return roots;
	}

	public List<NodeInfo> getPredecessors(NodeInfo node) { // 获得前驱结点，即父结点
		List<NodeInfo> predecessors = new ArrayList<>();
		if (node == null || !contains(node))
			return predecessors;

		for (EdgeInfo edge : edgeList) {
			switch (edge.getDirection()) {
			case EdgeInfo.INCOMING:// A <- B
				if (node.equalsObject(edge.getNodeA()))
					predecessors.add(edge.getNodeB());
				break;
			case EdgeInfo.OUTGOING: // A -> B
				if (node.equalsObject(edge.getNodeB()))
					predecessors.add(edge.getNodeA());
				break;
			case EdgeInfo.UNDIRECTED: // A -- B
				if (node.equalsObject(edge.getNodeA()))
					predecessors.add(edge.getNodeB());
				else if (node.equalsObject(edge.getNodeB()))
					predecessors.add(edge.getNodeA());
				break;
			default:
				break;
			}
		}
		return predecessors;
	}

	public List<NodeInfo> getSuccessors(NodeInfo node) { // 获得后继结点，即儿子结点
		List<NodeInfo> successors = new ArrayList<>();
		if (node == null || !contains(node))
			return successors;

		for (EdgeInfo edge : edgeList) {
			switch (edge.getDirection()) {
			case EdgeInfo.INCOMING:// A <- B
				if (node.equalsObject(edge.getNodeB()))
					successors.add(edge.getNodeA());
				break;
			case EdgeInfo.OUTGOING: // A -> B
				if (node.equalsObject(edge.getNodeA()))
					successors.add(edge.getNodeB());
				break;
			case EdgeInfo.UNDIRECTED: // A -- B
				if (node.equalsObject(edge.getNodeA()))
					successors.add(edge.getNodeB());
				else if (node.equalsObject(edge.getNodeB()))
					successors.add(edge.getNodeA());
				break;
			default:
				break;
			}
		}
		return successors;
	}

	/**
	 * 因为新加结点涉及较多相关更新操作，必须调用此方法。
	 * 
	 * @param node
	 * @return The index of added node in the nodeList of graph
	 */
	public int addNode(NodeInfo node) {
		try {
			if (node == null)
				return -1;
			// 按照添加结点的顺序为结点添加编号！
			// node.setNumber(nodeList.size());
			nodeList.add(node);
			return nodeList.size() - 1; // node index
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public EdgeInfo addEdge(NodeInfo nodeA, NodeInfo nodeB, int direction) {
		EdgeInfo edge = new EdgeInfo(nodeA, nodeB, direction);
		if (addEdge(edge)) // 添加成功返回true
			return edge;
		else
			return null;
	}

	/**
	 * 结果表示该边已经加入图中。本应同时添加未存于列表中的结点，可是也需要检查，所以也放弃了，不存在的结点不添加
	 * 
	 * @param edge
	 * @return
	 */
	public boolean addEdge(EdgeInfo edge) {
		try {
			if (edge == null)
				return false;

			if (!contains(edge))
				edgeList.add(edge);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<NodeInfo> getNodeList() {
		return nodeList;
	}

	public List<EdgeInfo> getEdgeList() {
		return edgeList;
	}

	public NodeInfo findEqualNode(NodeInfo node) {
		if (node == null || nodeList == null)
			return null;

		for (NodeInfo nodeInfo : nodeList) {
			if (nodeInfo.equalsObject(node)) // 内存对象相同才算重复
				return nodeInfo;
		}
		return null;
	}

	public boolean contains(NodeInfo node) {
		if (node == null || nodeList == null)
			return false;

		for (NodeInfo nodeInfo : nodeList) {
			if (nodeInfo.equalsObject(node)) // 内存对象相同才算重复
				return true;
		}
		return false;
	}

	public boolean contains(EdgeInfo edge) {
		if (edge == null || edgeList == null)
			return false;

		for (EdgeInfo edgeInfo : edgeList) {
			if (edgeInfo.equalsObject(edge)) // 结点内存对象相同，结点间方向一致才算重复
				return true;
		}
		return false;
	}

	public String toStringWithOrderNumber() {
		StringBuilder str = new StringBuilder("[graph: [node:");
		for (int i = 0; i < nodeList.size(); i++) {
			str.append(nodeList.get(i).toStringWithOrderNumber());
			if (i < nodeList.size() - 1)
				str.append(", ");
		}
		str.append("][edge:");
		for (int i = 0; i < edgeList.size(); i++) {
			str.append(edgeList.get(i).toStringWithOrderNumber());
			if (i < edgeList.size() - 1)
				str.append(", ");
		}
		str.append("]]");
		return str.toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("[graph: [node:");
		for (int i = 0; i < nodeList.size(); i++) {
			str.append(nodeList.get(i).toString());
			if (i < nodeList.size() - 1)
				str.append(", ");
		}
		str.append("][edge:");
		for (int i = 0; i < edgeList.size(); i++) {
			str.append(edgeList.get(i).toString());
			if (i < edgeList.size() - 1)
				str.append(", ");
		}
		str.append("]]");
		return str.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeInfo getRoot() {
		if (root == null)
			return nodeList.get(0);
		return root;
	}

	public void setRoot(NodeInfo root) {
		this.root = root;
	}

	@Override
	public String getUuid() {
		if (this.uuid == null)
			this.uuid = UUID.randomUUID().toString();
		return uuid;
	}

}
