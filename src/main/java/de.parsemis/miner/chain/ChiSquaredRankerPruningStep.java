/**
 * created May 25, 2006
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

package de.parsemis.miner.chain;

import java.util.ArrayList;
import java.util.Collection;

import de.parsemis.graph.Graph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.HPEmbedding;

/**
 * This class implements the pruning of fragments according to the ChiSquared Value of 
 * their edge weights in respect to the classes.
 * 
 * @author Matthias Huber (huberm@ipd.uni-karlsruhe.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */

public class ChiSquaredRankerPruningStep<NodeType, EdgeType> extends
	MiningStep<NodeType, EdgeType> {
	
	private final int minSize; // minimal size of fragments before they are checked by this Class
	private final double lowerBound;
	private final double upperBound;
	private final boolean subsequentBugs;
	
	public ChiSquaredRankerPruningStep(final MiningStep<NodeType, EdgeType> next,
			final int minSize, final double lowerBound, final double upperBound, final boolean subsequentBugs) {
		super(next);
		this.minSize = minSize;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.subsequentBugs = subsequentBugs;
	}
	
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		
		final Fragment<NodeType, EdgeType> fragment = node.toFragment();
		final Collection<Embedding<NodeType, EdgeType>> embeddings = fragment.getEmbeddings();
		final Graph<NodeType, EdgeType> graph = fragment.toGraph();
		
		int i, e;
		
		ArrayList<Integer> classes = new ArrayList<Integer>();
		ArrayList<Integer> weights = new ArrayList<Integer>();
		
		double maxChiSquare = 0.0;
		
		int maxRankedEdgeId = 0;
		
		// fragment has minimum pruning size
		if (graph.getEdgeCount() > minSize) {
			// determine ranking for each edge
			for (i = 0; i < graph.getEdgeCount(); i++) {
				// ranking did not change
				if (node.getLastNumberOfEmbeddings() == embeddings.size() && node.getEdgeRanking(i) > 0.0) {
					
				// ranking did change, calculate new ranking
				} else {
					classes = new ArrayList<Integer>();
					weights = new ArrayList<Integer>();
					// get edge weight and class of each supergraph for current edge
					for (final Embedding<NodeType, EdgeType> embedding : embeddings) {
						final HPEmbedding<NodeType, EdgeType> emb = embedding.toHPEmbedding();
						emb.getSuperGraphEdge(i);
						e = emb.getSuperGraphEdge(i);
						classes.add(emb.getSuperGraph().getClassNumber());
						weights.add(emb.getSuperGraph().getEdgeWeigth(e));
					}
					// set ranking
					node.setEdgeRanking(i, chiSquare(weights, classes));
				}
			}
			// determine maximum ranking
			maxChiSquare = 0.0;
			for (i = 0; i < graph.getEdgeCount(); i++) {
				if (node.getEdgeRanking(i) >= maxChiSquare ) {
					maxChiSquare = node.getEdgeRanking(i);
					maxRankedEdgeId = i;
				}
			}
			// look for subsequent bugs
			if (this.subsequentBugs) {
				// node has been marked for pruning
				if (node.getPruningMark()) {
					// subsequent bug found -> continue
					if (maxChiSquare == node.getRanking()) {
						callNext(node, extensions);
					// no further subsequent bugs found -> save fragment ant prune search branch
					} else {
						node.store(true);
					}
				// node has not been marked for pruning
				} else {
					// fragment ranking is in between lower and upper bound -> continue 
					if (lowerBound <= maxChiSquare && maxChiSquare <= upperBound) {
						node.setRanking(maxChiSquare);
						node.setLastNumberOfEmbeddings(embeddings.size());
						node.addMaxRankedEdgeId(maxRankedEdgeId);
						callNext(node, extensions);
					// fragment ranking is higher than upper bound -> save fragment and prune search branch
					} else if (maxChiSquare > upperBound) {
						node.setPruningMark(true);
						callNext(node, extensions);
					//fragment ranking is lower than lower bound -> prune search branch
					} else {
						node.store(false);
					}
				}
			// do not look for subsequent bugs
			} else {
				// fragment ranking is in between lower and upper bound -> continue 
				if (lowerBound <= maxChiSquare && maxChiSquare <= upperBound) {
					node.setRanking(maxChiSquare);
					node.setLastNumberOfEmbeddings(embeddings.size());
					node.addMaxRankedEdgeId(maxRankedEdgeId);
					callNext(node, extensions);
				// fragment ranking is higher than upper bound -> save fragment and prune search branch
				} else if (maxChiSquare > upperBound) {
					node.store(true);
					//callNext(node, extensions);
				//fragment ranking is lower than lower bound -> prune search branch
				} else {
					node.store(false);
				}
			}
		// fragment does not have minimum pruning size
		} else {
			callNext(node, extensions);
		}
		
		/*
		if (graph.getEdgeCount() > minSize) {
			for (i = 0; i < graph.getEdgeCount(); i++) {
				classes = new ArrayList<Integer>();
				weights = new ArrayList<Integer>();
				if (node.getLastNumberOfEmbeddings() == embeddings.size() && node.getEdgeRanking(i) > 0.0) {
					// do nothing
				} else {
					for (final Embedding<NodeType, EdgeType> embedding : embeddings) {
						final HPEmbedding<NodeType, EdgeType> emb = embedding.toHPEmbedding();
						emb.getSuperGraphEdge(i);
						e = emb.getSuperGraphEdge(i);
						classes.add(emb.getSuperGraph().getClassNumber());
						weights.add(emb.getSuperGraph().getEdgeWeigth(e));
					}
					curChiSquare = chiSquare(weights, classes);
					if (curChiSquare >= maxChiSquare) {
						maxChiSquare = curChiSquare;
						maxRankedEdgeId = i;
					}
				}
			}
			if (this.subsequentBugs) {
				if (lowerBound <= maxChiSquare && maxChiSquare <= upperBound) {
					if (node.getPruningMark()) {
						// no subsequent bug found - pruning
						node.store(false);
					} else {
						node.setRanking(maxChiSquare);
						node.setLastNumberOfEmbeddings(embeddings.size());
						node.setMaxRankedEdgeId(maxRankedEdgeId);
						node.store(true);
						callNext(node, extensions);
					}
				} else if (maxChiSquare > upperBound){
					if (node.getPruningMark()) {
						if (node.getMaxRankedEdgeId() == maxRankedEdgeId && node.getRanking() == maxChiSquare) {
							// subsequent bug found - look for further subsequent bugs
							node.store(true);
							node.setPruningMark(true);
							callNext(node, extensions);
						//} else if (node.getMaxRankedEdgeId() == maxRankedEdgeId && node.getRanking() <= maxVar) {
							// 'better' bug found
						//	node.setRanking(maxVar);
						//	node.setLastNumberOfEmbeddings(embeddings.size());
						//	node.setMaxRankedEdgeId(maxRankedEdgeId);
						//	node.store(true);
						//	node.setPruningMark(false);
						//	callNext(node, extensions);
						} else {
							// no subsequent bug found - pruning
							node.store(false);
						}
					} else {
						// look for subsequent bugs
						node.setRanking(maxChiSquare);
						node.setLastNumberOfEmbeddings(embeddings.size());
						node.setMaxRankedEdgeId(maxRankedEdgeId);
						node.store(true);
						node.setPruningMark(true);
						callNext(node, extensions);
					}
				} else { // lowerBound > maxVar
					// pruning
					node.store(false);
				}
			} else {
				// do not look for subsequent bugs
				if (lowerBound <= maxChiSquare && maxChiSquare <= upperBound) {
					node.setRanking(maxChiSquare);
					node.setLastNumberOfEmbeddings(embeddings.size());
					node.setMaxRankedEdgeId(maxRankedEdgeId);
					node.store(true);
					callNext(node, extensions);
				} else if (maxChiSquare > upperBound){
					node.store(true);
				} else {
					node.store(false);
				}
			}
		} else {
			node.store(true);
			callNext(node, extensions);
		}*/
		
	}
	
	public double chiSquare(ArrayList<Integer> attribute, ArrayList<Integer> classes) {
		int i;
		
		double attribute_sum = 0.0;
		for (i = 0; i < attribute.size(); i++) {
			attribute_sum += attribute.get(i);
		}
		double classes_sum = 0.0;
		for (i = 0; i < classes.size(); i++) {
			classes_sum += classes.get(i);
		}
		
		double chi = 0.0;
		double eij = 0.0;
		for (i = 0; i < attribute.size(); i++) {
			for (i = 0; i < classes.size(); i++) {
				eij = (attribute.get(i) + classes.get(i) / (attribute_sum + classes_sum));
				chi += Math.pow(attribute.get(i) - (attribute_sum * eij),2) / eij; 
			}
		}
		return chi;
	}
}
