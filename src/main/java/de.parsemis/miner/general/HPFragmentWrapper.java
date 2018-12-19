/**
 * Created on 12.01.2008
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * Copyright 2008 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.miner.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;

/**
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
public final class HPFragmentWrapper<NodeType, EdgeType> implements
		Fragment<NodeType, EdgeType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7287249170550634823L;

	/**
	 * 
	 */
	final HPFragment<NodeType, EdgeType> fragment;
	
	// added
	@Override
	public void setRanking(double ranking) {this.fragment.setRanking(ranking);}
	@Override
	public double getRanking() {return this.fragment.getRanking();}
	
	@Override
	public void setLastNumberOfEmbeddings(int size) {this.fragment.setLastNumberOfEmbeddings(size);}
	@Override
	public int getLastNumberOfEmbeddings() {return this.fragment.getLastNumberOfEmbeddings();}
	@Override
	public void addMaxRankedEdgeId(int id)  {this.fragment.addMaxRankedEdgeId(id);}
	@Override
	public void setMaxRankedEdgeIds(ArrayList<Integer> ids)  {this.fragment.setMaxRankedEdgeIds(ids);}
	@Override
	public ArrayList<Integer> getMaxRankedEdgeIds() {return this.fragment.getMaxRankedEdgeIds();}

	@Override
	public void setEdgeRanking(int edgeIdx, double r) {this.fragment.setEdgeRanking(edgeIdx, r);}
	@Override
	public double getEdgeRanking(int edgeIdx) {return this.fragment.getEdgeRanking(edgeIdx);}
	
	@Override
	public void setPruningMark(boolean flag) {this.fragment.setPruningMark(flag);}
	@Override
	public boolean getPruningMark() {return this.fragment.getPruningMark();}


	private transient Collection<Embedding<NodeType, EdgeType>> mc;

	/**
	 * @param fragment
	 */
	public HPFragmentWrapper(final HPFragment<NodeType, EdgeType> fragment) {
		this.fragment = fragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#add(de.parsemis.miner.DataBaseGraph)
	 */
	@Override
	public void add(final DataBaseGraph<NodeType, EdgeType> graph)
			throws RuntimeException {
		this.fragment.add(graph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(E)
	 */
	@Override
	public boolean add(final Embedding<NodeType, EdgeType> arg0) {
		return this.fragment.add(arg0.toHPEmbedding());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(
			final Collection<? extends Embedding<NodeType, EdgeType>> arg0) {
		for (final Embedding<NodeType, EdgeType> e : arg0) {
			if (!this.fragment.add(e.toHPEmbedding())) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		this.fragment.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(final Object arg0) {
		final Embedding<NodeType, EdgeType> emb = (Embedding<NodeType, EdgeType>) arg0;
		return this.fragment.contains(emb.toHPEmbedding());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> arg0) {
		boolean ret = true;
		for (final Object g : arg0) {
			ret &= contains(g);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#copy()
	 */
	@Override
	public Fragment<NodeType, EdgeType> copy() {
		return this.fragment.copy().toFragment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	@Override
	public Edge<NodeType, EdgeType> embeddingToFragmentEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> embeddingEdge) {
		assert (contains(emb));
		return embeddingEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	@Override
	public Node<NodeType, EdgeType> embeddingToFragmentNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> embeddingNode) {
		assert (contains(emb));
		return embeddingNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	@Override
	public Edge<NodeType, EdgeType> fragmentToEmbeddingEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> fragmentEdge) {
		assert (contains(emb));
		return fragmentEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	@Override
	public Node<NodeType, EdgeType> fragmentToEmbeddingNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> fragmentNode) {
		assert (contains(emb));
		return fragmentNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	@Override
	public Frequency frequency() {
		return this.fragment.frequency();
	}

	@Override
	public Collection<Embedding<NodeType, EdgeType>> getEmbeddings() {
		return this;
	}

	@Override
	public Collection<Embedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		if (mc == null) {
			mc = new Collection<Embedding<NodeType, EdgeType>>() {

				@Override
				public boolean add(final Embedding<NodeType, EdgeType> arg0) {
					throw new UnsupportedOperationException(
							"add not allowed for that Wrapper");
				}

				@Override
				public boolean addAll(
						final Collection<? extends Embedding<NodeType, EdgeType>> arg0) {
					throw new UnsupportedOperationException(
							"addAll not allowed for that Wrapper");
				}

				@Override
				public void clear() {
					throw new UnsupportedOperationException(
							"clear not allowed for that Wrapper");
				}

				@Override
				@SuppressWarnings("unchecked")
				public boolean contains(final Object arg0) {
					final Embedding<NodeType, EdgeType> emb = (Embedding<NodeType, EdgeType>) arg0;
					return fragment.getMaximalNonOverlappingSubSet().contains(
							emb.toHPEmbedding());
				}

				@Override
				@SuppressWarnings("unchecked")
				public boolean containsAll(final Collection<?> arg0) {
					for (final Embedding<NodeType, EdgeType> e : (Collection<Embedding<NodeType, EdgeType>>) arg0) {
						if (!fragment.getMaximalNonOverlappingSubSet()
								.contains(e.toHPEmbedding())) {
							return false;
						}
					}
					return true;
				}

				@Override
				public boolean isEmpty() {
					return fragment.getMaximalNonOverlappingSubSet().isEmpty();
				}

				@Override
				public Iterator<Embedding<NodeType, EdgeType>> iterator() {
					return new Iterator<Embedding<NodeType, EdgeType>>() {
						Iterator<HPEmbedding<NodeType, EdgeType>> it = fragment
								.getMaximalNonOverlappingSubSet().iterator();

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public Embedding<NodeType, EdgeType> next() {
							return it.next().toEmbedding();
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException(
									"remove not allowed for that Wrapper");
						}
					};
				}

				@Override
				public boolean remove(final Object arg0) {
					throw new UnsupportedOperationException(
							"remove not allowed for that Wrapper");
				}

				@Override
				public boolean removeAll(final Collection<?> arg0) {
					throw new UnsupportedOperationException(
							"removeAll not allowed for that Wrapper");
				}

				@Override
				public boolean retainAll(final Collection<?> arg0) {
					throw new UnsupportedOperationException(
							"retainAll not allowed for that Wrapper");
				}

				@Override
				public int size() {
					return fragment.getMaximalNonOverlappingSubSet().size();
				}

				@Override
				@SuppressWarnings("unchecked")
				public Object[] toArray() {
					return toArray((Embedding<NodeType, EdgeType>[]) new Embedding[size()]);
				}

				@Override
				@SuppressWarnings("unchecked")
				public <T> T[] toArray(final T[] arg0) {
					final Embedding<NodeType, EdgeType>[] ret = (Embedding<NodeType, EdgeType>[]) arg0;
					int i = 0;
					for (final Embedding<NodeType, EdgeType> e : this) {
						ret[i++] = e;
					}
					return arg0;
				}

			};
		}
		return mc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#toGraph()
	 */
	@Override
	public Graph<NodeType, EdgeType> toGraph() {
		return this.fragment.toHPGraph().toGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#graphIterator()
	 */
	@Override
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		return this.fragment.graphIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.fragment.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<Embedding<NodeType, EdgeType>> iterator() {
		return new Iterator<Embedding<NodeType, EdgeType>>() {
			Iterator<HPEmbedding<NodeType, EdgeType>> it = fragment.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Embedding<NodeType, EdgeType> next() {
				return it.next().toEmbedding();
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
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(final Object arg0) {
		return this.fragment.remove(((Embedding<NodeType, EdgeType>) arg0)
				.toHPEmbedding());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean removeAll(final Collection<?> arg0) {
		for (final Embedding<NodeType, EdgeType> e : (Collection<Embedding<NodeType, EdgeType>>) arg0) {
			if (!this.fragment.remove(e.toHPEmbedding())) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException("retainAll is not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return this.fragment.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object[] toArray() {
		return toArray((Embedding<NodeType, EdgeType>[]) new Embedding[size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] arg0) {
		final Embedding<NodeType, EdgeType>[] ret = (Embedding<NodeType, EdgeType>[]) arg0;
		int i = 0;
		for (final Embedding<NodeType, EdgeType> e : this) {
			ret[i++] = e;
		}
		return arg0;
	}

	@Override
	public HPFragment<NodeType, EdgeType> toHPFragment() {
		return this.fragment;
	}
}