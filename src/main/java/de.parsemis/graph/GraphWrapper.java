/**
 * created May 31, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.graph;

import java.util.BitSet;
import java.util.Iterator;

import de.parsemis.miner.environment.Relabler;
import de.parsemis.utils.GraphUtils;
import de.parsemis.utils.IntIterator;

/**
 * A wrapper to use normal Graphs like HPGraphs.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
class GraphWrapper<NodeType, EdgeType> implements
		HPGraph<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 141322960645199554L;

	protected final Graph<NodeType, EdgeType> master;

	protected transient BitSet edges;

	protected transient BitSet nodes;

	transient int[] partitions = null;

	/**
	 * creates a wrapper for the given master graph
	 * 
	 * @param master
	 */
	GraphWrapper(final Graph<NodeType, EdgeType> master) {
		this.master = master;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.utils.Cloneable#clone()
	 */
	@Override
	public HPGraph<NodeType, EdgeType> clone() {
		return new GraphWrapper<NodeType, EdgeType>(master.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#edgeIndexIterator()
	 */
	@Override
	public IntIterator edgeIndexIterator() {
		return new IntIterator() {
			final Iterator<Edge<NodeType, EdgeType>> it = master.edgeIterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int next() {
				return it.next().getIndex();
			}

			@Override
			public void remove() {
				it.remove();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getDegree(int)
	 */
	@Override
	public int getDegree(final int nodeIdx) {
		return master.getNode(nodeIdx).getDegree();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getDirection(int)
	 */
	@Override
	public int getDirection(final int edgeIdx) {
		return master.getEdge(edgeIdx).getDirection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getDirection(int, int)
	 */
	@Override
	public int getDirection(final int edgeIdx, final int nodeIdx) {
		return master.getEdge(edgeIdx).getDirection(master.getNode(nodeIdx));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdge(int, int)
	 */
	@Override
	public int getEdge(final int nodeAIdx, final int nodeBIdx) {
		final Edge<NodeType, EdgeType> edge = master.getEdge(master
				.getNode(nodeAIdx), master.getNode(nodeBIdx));
		return (edge == null ? -1 : edge.getIndex());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		return master.getEdgeCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeIndices(int)
	 */
	@Override
	public IntIterator getEdgeIndices(final int nodeIdx) {
		return new IntIterator() {
			final Iterator<Edge<NodeType, EdgeType>> it = master.getNode(
					nodeIdx).edgeIterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int next() {
				return it.next().getIndex();
			}

			@Override
			public void remove() {
				it.remove();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeLabel(int)
	 */
	@Override
	public EdgeType getEdgeLabel(final int edgeIdx) {
		return master.getEdge(edgeIdx).getLabel();
	}
	
	// added
	@Override
	public int getEdgeWeigth(final int edgeIdx) {
		return master.getEdge(edgeIdx).getWeigth();
	}
	
	@Override
	public void setEdgeRanking (final int edgeIdx, double r) {
		master.setEdgeRanking(edgeIdx, r);
	}
	@Override
	public double getEdgeRanking (final int edgeIdx) {
		return master.getEdgeRanking(edgeIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeLabelIndex(int,
	 *      de.parsemis.utils.Relabler)
	 */
	@Override
	public int getEdgeLabelIndex(final int edgeIdx,
			final Relabler<NodeType, EdgeType> rel) {
		return rel.getEdgeLabelIndex(master.getEdge(edgeIdx).getLabel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdges()
	 */
	@Override
	public BitSet getEdges() {
		if (edges == null) {
			edges = new BitSet(this.getMaxEdgeIndex());
			for (int i = this.getMaxEdgeIndex() - 1; i >= 0; --i) {
				if (this.isValidEdge(i)) {
					edges.set(i);
				}
			}
		}
		return edges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getID()
	 */
	@Override
	public int getID() {
		return master.getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getInDegree(int)
	 */
	@Override
	public int getInDegree(final int nodeIdx) {
		return master.getNode(nodeIdx).getInDegree();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getInEdgeIndices(int)
	 */
	@Override
	public IntIterator getInEdgeIndices(final int nodeIdx) {
		return new IntIterator() {
			final Iterator<Edge<NodeType, EdgeType>> it = master.getNode(
					nodeIdx).incommingEdgeIterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int next() {
				return it.next().getIndex();
			}

			@Override
			public void remove() {
				it.remove();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getMaxEdgeIndex()
	 */
	@Override
	public int getMaxEdgeIndex() {
		return master.getMaxEdgeIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getMaxNodeIndex()
	 */
	@Override
	public int getMaxNodeIndex() {
		return master.getMaxNodeIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getName()
	 */
	@Override
	public String getName() {
		return master.getName();
	}
	
	// added 2008.09.12 (matthias)
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getName()
	 */
	@Override
	public int getClassNumber() {
		return master.getClassNumber();
	}
	@Override
	public void setClassNumber(int classNumber) {
		master.setClassNumber(classNumber);
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeA(int)
	 */
	@Override
	public int getNodeA(final int edgeIdx) {
		return master.getEdge(edgeIdx).getNodeA().getIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeB(int)
	 */
	@Override
	public int getNodeB(final int edgeIdx) {
		return master.getEdge(edgeIdx).getNodeB().getIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeCount()
	 */
	@Override
	public int getNodeCount() {
		return master.getNodeCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeEdge(int, int)
	 */
	@Override
	public int getNodeEdge(final int nodeIdx, final int pos) {
		final Iterator<Edge<NodeType, EdgeType>> it = master.getNode(nodeIdx)
				.edgeIterator();
		for (int i = pos; i > 0; --i) {
			it.next();
		}
		return it.next().getIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeLabel(int)
	 */
	@Override
	public NodeType getNodeLabel(final int nodeIdx) {
		return master.getNode(nodeIdx).getLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeLabelIndex(int,
	 *      de.parsemis.utils.Relabler)
	 */
	@Override
	public int getNodeLabelIndex(final int nodeIdx,
			final Relabler<NodeType, EdgeType> rel) {
		return rel.getNodeLabelIndex(master.getNode(nodeIdx).getLabel());
	}

	@Override
	public int getNodeNeigbour(final int nodeIdx, final int pos) {
		return getOtherNode(getNodeEdge(nodeIdx, pos), nodeIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodePartion()
	 */
	@Override
	public int[] getNodePartions(final Relabler<NodeType, EdgeType> rel) {
		if (partitions == null) {
			partitions = GraphUtils.computePartitions2(this, rel);
		}
		return partitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodes()
	 */
	@Override
	public BitSet getNodes() {
		if (nodes == null) {
			nodes = new BitSet(this.getMaxNodeIndex());
			for (int i = this.getMaxNodeIndex() - 1; i >= 0; --i) {
				if (this.isValidEdge(i)) {
					nodes.set(i);
				}
			}
		}
		return nodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getOtherNode(int, int)
	 */
	@Override
	public int getOtherNode(final int edgeIdx, final int nodeIdx) {
		return master.getEdge(edgeIdx).getOtherNode(master.getNode(nodeIdx))
				.getIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getOutDegree(int)
	 */
	@Override
	public int getOutDegree(final int nodeIdx) {
		return master.getNode(nodeIdx).getOutDegree();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getOutEdgeIndices(int)
	 */
	@Override
	public IntIterator getOutEdgeIndices(final int nodeIdx) {
		return new IntIterator() {
			final Iterator<Edge<NodeType, EdgeType>> it = master.getNode(
					nodeIdx).outgoingEdgeIterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int next() {
				return it.next().getIndex();
			}

			@Override
			public void remove() {
				it.remove();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#isValidEdge(int)
	 */
	@Override
	public boolean isValidEdge(final int edgeIdx) {
		return master.getEdge(edgeIdx) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#isValidNode(int)
	 */
	@Override
	public boolean isValidNode(final int nodeIdx) {
		return master.getNode(nodeIdx) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#nodeIndexIterator()
	 */
	@Override
	public IntIterator nodeIndexIterator() {
		return new IntIterator() {
			final Iterator<Node<NodeType, EdgeType>> it = master.nodeIterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int next() {
				return it.next().getIndex();
			}

			@Override
			public void remove() {
				it.remove();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#setEdgeLabel(int, EdgeType)
	 */
	@Override
	public void setEdgeLabel(final int edgeIdx, final EdgeType label) {
		master.getEdge(edgeIdx).setLabel(label);
		partitions = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#setNodeLabel(int, NodeType)
	 */
	@Override
	public void setNodeLabel(final int nodeIdx, final NodeType label) {
		master.getNode(nodeIdx).setLabel(label);
		partitions = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#toGraph()
	 */
	@Override
	public Graph<NodeType, EdgeType> toGraph() {
		return master;
	}

}
