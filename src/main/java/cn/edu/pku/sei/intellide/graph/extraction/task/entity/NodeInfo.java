package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import cn.edu.pku.sei.intellide.graph.extraction.task.utils.UUIDInterface;

import java.io.Serializable;
import java.util.UUID;

public class NodeInfo implements Serializable, UUIDInterface, Comparable<NodeInfo> {
	private static final long serialVersionUID = 9135849985209562786L;

	private String		uuid	= null;
	private NodeLabel	label;
	private String		value;
	private int			number	= -1;	// 在Graph中的先根顺序编号，只在Parsemis中有意义，GraphInfo下没有意义

	public NodeInfo(NodeLabel label, String value) {
		uuid = UUID.randomUUID().toString();
		this.setLabel(label);
		this.setValue(value);
	}

	public NodeLabel getLabel() {
		return label;
	}

	public void setLabel(NodeLabel label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (value == "")
			this.value = null;
		else
			this.value = value;
	}

	public String toStringWithOrderNumber() {
		return number + this.toString();
	}

	@Override
	public String toString() {
		if (value == null)
			return "<" + label.toString() + ">";
		else
			return "<" + label.toString() + " " + value + ">";
	}

	// 按照内存对象比较相同，自己的GraphInfo
	public boolean equalsObject(Object obj) {
		return super.equals(obj);
	}

	// 完全按照节点的值来比较是否相同 - For Parsemis
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof NodeInfo))
			return false;
		NodeInfo comparedTo = (NodeInfo) obj;
		return this.toString().equals(comparedTo.toString());
	}

	@Override
	public int hashCode() {
		return this.getClass().getName().hashCode() + this.toString().hashCode();
	}

	@Override
	public int compareTo(NodeInfo other) {
		if (other != null) {
			int strComp = this.toString().compareTo(other.toString());
			// if(strComp==0)
			// return this.number-other.number;
			// else
			return strComp;
		}
		return this.toString().compareTo(null);
	}

	public static void main(String[] args) {
		NodeInfo node = new NodeInfo(NodeLabel.NP, "");
		NodeInfo n2 = new NodeInfo(NodeLabel.NP, "a");
		NodeInfo n3 = new NodeInfo(NodeLabel.NP, null);
		NodeInfo n4 = new NodeInfo(NodeLabel.NP, null);
		NodeInfo n5 = new NodeInfo(NodeLabel.VP, null);
		n3.setNumber(2);
		n4.setNumber(4);
		node.setNumber(2);
		System.out.println(node.compareTo(n3));
		System.out.println(node.compareTo(n4));
		System.out.println(n3.compareTo(n4));
		System.out.println(n4.compareTo(n3));
		System.out.println(n3.compareTo(node));
		System.out.println(n4.compareTo(node));
		System.out.println();
		System.out.println(node + "\t" + n2 + "\t" + n3 + "\t" + n4);
		System.out.println(node.equals(n2));
		System.out.println(node.equals(n3));
		System.out.println(n2.equals(n3));
		System.out.println(n4.equals(n3));
		System.out.println(n4.equals(n5));
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	public void setUuid() {
		this.uuid = UUID.randomUUID().toString();
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
}
