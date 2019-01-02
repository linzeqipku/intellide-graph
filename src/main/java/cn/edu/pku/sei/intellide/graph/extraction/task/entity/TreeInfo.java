package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import java.util.ArrayList;
import java.util.List;

public class TreeInfo extends NodeInfo {
	private static final long serialVersionUID = -8126853559535796372L;

	private List<TreeInfo>	children;
	private TreeInfo		parent	= null;

	public TreeInfo(NodeLabel label, String value) {
		super(label, value);
		children = new ArrayList<>();
	}

	public TreeInfo(NodeInfo rootNode) {
		this(rootNode.getLabel(), rootNode.getValue());
	}

	public List<TreeInfo> getChildren() {
		return children;
	}

	public void setChildren(List<TreeInfo> children) {
		this.children = children;
	}

	public TreeInfo getParent() {
		return parent;
	}

	public void setParent(TreeInfo parent) {
		this.parent = parent;
	}

	// 不做重复检查，调用逻辑来保证
	public void addChild(TreeInfo childTree) {
		if (childTree != null) {
			children.add(childTree);
			childTree.setParent(this);
		}
	}

	// 不做重复检查，调用逻辑来保证
	public TreeInfo addChild(NodeInfo childNode) {
		if (childNode != null) {
			TreeInfo childTree = new TreeInfo(childNode);
			children.add(childTree);
			childTree.setParent(this);
			return childTree;
		}
		return null;
	}

	// equal之类的比较还是按照父类NodeInfo的
}
