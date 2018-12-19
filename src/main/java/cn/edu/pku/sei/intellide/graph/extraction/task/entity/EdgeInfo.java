package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import cn.edu.pku.sei.intellide.graph.extraction.task.utils.UUIDInterface;
import de.parsemis.graph.Edge;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author ZHUZixiao
 *
 */
public class EdgeInfo implements Serializable, UUIDInterface {
	private static final long serialVersionUID = 6038310513494512664L;

	public static final int	INCOMING	= Edge.INCOMING;
	public static final int	OUTGOING	= Edge.OUTGOING;
	public static final int	UNDIRECTED	= Edge.UNDIRECTED;

	private String		uuid;
	private NodeInfo	nodeA;
	private NodeInfo	nodeB;
	private int			direction;

	public EdgeInfo(NodeInfo nodeA, NodeInfo nodeB, int direction) {
		uuid = UUID.randomUUID().toString();
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.direction = direction;
	}

	public NodeInfo getNodeA() {
		return nodeA;
	}

	public void setNodeA(NodeInfo nodeA) {
		this.nodeA = nodeA;
	}

	public NodeInfo getNodeB() {
		return nodeB;
	}

	public void setNodeB(NodeInfo nodeB) {
		this.nodeB = nodeB;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	// 取决于NodeInfo的equal比较方式
	// 如果两条边的两端对象值相同、类型相同，那就是同一条边
	@Override
	public boolean equals(Object obj) { // 值相同
		if (obj == null || !(obj instanceof EdgeInfo))
			return false;
		EdgeInfo objEdge = (EdgeInfo) obj;
		if (this.nodeA.equals(objEdge.nodeA) && this.nodeB.equals(objEdge.nodeB)
				&& this.direction == objEdge.direction)
			return true;
		else if (this.nodeA.equals(objEdge.nodeB) && this.nodeB.equals(objEdge.nodeA)
				&& getOppositeDirection(this.direction) == objEdge.direction)
			return true; // A B颠倒，方向相反的情况
		else
			return false;
	}

	// 取决于node的hashCode实现
	@Override
	public int hashCode() {
		return nodeA.hashCode() + nodeB.hashCode() + direction;
	}

	// 取决于NodeInfo的equalObject比较方式，一般是内存对象比较
	public boolean equalsObject(Object obj) { // 内存相同
		if (obj == null || !(obj instanceof EdgeInfo))
			return false;
		EdgeInfo objEdge = (EdgeInfo) obj;
		if (this.nodeA.equalsObject(objEdge.nodeA) && this.nodeB.equalsObject(objEdge.nodeB)
				&& this.direction == objEdge.direction)
			return true;
		else if (this.nodeA.equalsObject(objEdge.nodeB) && this.nodeB.equalsObject(objEdge.nodeA)
				&& getOppositeDirection(this.direction) == objEdge.direction)
			return true; // A B颠倒，方向相反的情况
		else
			return false;
	}

	@Override
	public String toString() {
		String directionStr;
		switch (direction) {
		case INCOMING:
			directionStr = "<-";
			break;
		case OUTGOING:
			directionStr = "->";
			break;
		case UNDIRECTED:
			directionStr = "--";
			break;
		default:
			directionStr = ", ";
			break;
		}
		return "(" + nodeA + " " + directionStr + " " + nodeB + ")";
	}

	public String toStringWithOrderNumber() {
		String directionStr;
		switch (direction) {
		case INCOMING:
			directionStr = "<-";
			break;
		case OUTGOING:
			directionStr = "->";
			break;
		case UNDIRECTED:
			directionStr = "--";
			break;
		default:
			directionStr = ", ";
			break;
		}
		return "(" + nodeA.toStringWithOrderNumber() + " " + directionStr + " "
				+ nodeB.toStringWithOrderNumber() + ")";
	}
	public static int getOppositeDirection(int direction) {
		switch (direction) {
		case INCOMING:
			return OUTGOING;
		case OUTGOING:
			return INCOMING;
		case UNDIRECTED:
			return UNDIRECTED;
		default:
			return -1;
		}
	}

	@Override
	public String getUuid() {
		return uuid;
	}
}
