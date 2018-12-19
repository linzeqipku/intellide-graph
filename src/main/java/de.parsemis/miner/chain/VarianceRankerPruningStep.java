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
 * This class implements the pruning of fragments according to the variance of 
 * their edge weights.
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
public class VarianceRankerPruningStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {
	
	private final int minSize; // minimal size of fragments before they are checked by this Class
	
	private final double lowerBound;
	
	private final double upperBound;
	
	private final boolean subsequentBugs;
	
	public VarianceRankerPruningStep(final MiningStep<NodeType, EdgeType> next,
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
		
		
		int e;
		double maxVar = 0;
		double var;
		double avg;
		int i,j;
		int maxRankedEdgeId = 0;
		
		boolean oldNode = false;
		
		
		
		ArrayList<Integer> weights = new ArrayList<Integer>();
		// fragment has minimum pruning size
		if (graph.getEdgeCount() > minSize) {
			// determine ranking for each edge
			for (i = 0; i < graph.getEdgeCount(); i++) {
				// ranking did not change
				if (node.getLastNumberOfEmbeddings() == embeddings.size() && node.getEdgeRanking(i) > 0.0) {
					
				// ranking did change, calculate new ranking
				} else {
					weights = new ArrayList<Integer>();
					// get edge weight and class of each supergraph for current edge
					for (final Embedding<NodeType, EdgeType> embedding : embeddings) {
						final HPEmbedding<NodeType, EdgeType> emb = embedding.toHPEmbedding();
						emb.getSuperGraphEdge(i);
						e = emb.getSuperGraphEdge(i);
						weights.add(emb.getSuperGraph().getEdgeWeigth(e));
					}
					// set ranking
					avg = 0.0;
					var = 0.0;
	
					for (j = 0; j < weights.size(); j++) {
						avg += (weights.get(j)).doubleValue();
					}
					avg = avg / ((Integer) weights.size()).doubleValue();
				
					for (j = 0; j < weights.size(); j++) {
						var += ((weights.get(j)).doubleValue() - avg) * ((weights.get(j)).doubleValue() - avg); 
					}
					var = var / ( ((Integer) weights.size()).doubleValue() -1);
					node.setEdgeRanking(i, var);
				}
			}
			// determine maximum ranking
			maxVar = 0.0;

			ArrayList<Integer> maxRankedEdgeIds = node.getMaxRankedEdgeIds();
			
			for (i = 0; i < graph.getEdgeCount(); i++) {
				if (maxRankedEdgeIds.indexOf(i) != -1) {
					if (node.getEdgeRanking(i) >= maxVar) {
						maxVar = node.getEdgeRanking(i);
						maxRankedEdgeId = i;
						oldNode = true;
					}
				}
			}
			for (i = 0; i < graph.getEdgeCount(); i++) {
				if (maxRankedEdgeIds.indexOf(i) == -1) {
					if (node.getEdgeRanking(i) >= maxVar) {
						maxVar = node.getEdgeRanking(i);
						maxRankedEdgeId = i;
						oldNode = false;
					}
				}
			}
		
			// look for subsequent bugs
			if (this.subsequentBugs) {
				// node has been marked for pruning
				if (node.getPruningMark()) {
					// subsequent bug found -> continue
					if (maxVar == node.getRanking() && !oldNode) {
						node.setRanking(maxVar);
						node.addMaxRankedEdgeId(maxRankedEdgeId);
						node.setLastNumberOfEmbeddings(embeddings.size());
						callNext(node, extensions);
					// no further subsequent bugs found -> save fragment ant prune search branch
					} else {
						node.store(true);
					}
				// node has not been marked for pruning
				} else {
					// fragment ranking is in between lower and upper bound -> continue 
					if (lowerBound <= maxVar && maxVar <= upperBound) {
						node.setRanking(maxVar);
						node.setLastNumberOfEmbeddings(embeddings.size());
						if (!oldNode) {
							node.addMaxRankedEdgeId(maxRankedEdgeId);
						}
						callNext(node, extensions);
					// fragment ranking is higher than upper bound -> save fragment and prune search branch
					} else if (maxVar > upperBound) {
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
				if (lowerBound <= maxVar && maxVar <= upperBound) {
					node.setRanking(maxVar);
					node.setLastNumberOfEmbeddings(embeddings.size());
					callNext(node, extensions);
				// fragment ranking is higher than upper bound -> save fragment and prune search branch
				} else if (maxVar > upperBound) {
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
	}
}