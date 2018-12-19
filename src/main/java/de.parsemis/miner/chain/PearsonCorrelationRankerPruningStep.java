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
 * This class implements the pruning of fragments according to the Pearson product-moment correlation coefficien of
 * their edge weights and the class of the graph.
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
public class PearsonCorrelationRankerPruningStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {
	
	private final int minSize; // minimal size of fragments before they are checked by this Class
	
	private final double lowerBound;
	
	private final double upperBound;
	
	
	private final boolean subsequentBugs;
	
	public PearsonCorrelationRankerPruningStep(final MiningStep<NodeType, EdgeType> next,
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
		//System.out.println(node.toFragment().getRanking());
		ArrayList<Integer> classes = new ArrayList<Integer>();
		ArrayList<Integer> weights = new ArrayList<Integer>();
		
		double maxPpmcc = 0.0;
		int maxRankedEdgeId = 0;
		
		boolean oldNode = false;
		
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
					node.setEdgeRanking(i, PearsonProductMomentCorrelationCoefficient(weights, classes));
				}
			}
			// determine maximum ranking
			maxPpmcc = 0.0;
			
			ArrayList<Integer> maxRankedEdgeIds = node.getMaxRankedEdgeIds();
			
			for (i = 0; i < graph.getEdgeCount(); i++) {
				if (maxRankedEdgeIds.indexOf(i) != -1) {
					if (node.getEdgeRanking(i) >= maxPpmcc) {
						maxPpmcc = node.getEdgeRanking(i);
						maxRankedEdgeId = i;
						oldNode = true;
					}
				}
			}
			for (i = 0; i < graph.getEdgeCount(); i++) {
				if (maxRankedEdgeIds.indexOf(i) == -1) {
					if (node.getEdgeRanking(i) >= maxPpmcc) {
						maxPpmcc = node.getEdgeRanking(i);
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
					if ( maxPpmcc == node.getRanking() && !oldNode) {
						node.addMaxRankedEdgeId(maxRankedEdgeId);
						node.setRanking(maxPpmcc);
						node.setLastNumberOfEmbeddings(embeddings.size());
						callNext(node, extensions);
					// no further subsequent bugs found -> save fragment ant prune search branch
					} else {
						node.store(true);
					}
				// node has not been marked for pruning
				} else {
					// fragment ranking is in between lower and upper bound -> continue 
					if (lowerBound <=  maxPpmcc &&  maxPpmcc <= upperBound) {
						node.setRanking( maxPpmcc);
						node.setLastNumberOfEmbeddings(embeddings.size());
						if (!oldNode) {
							node.addMaxRankedEdgeId(maxRankedEdgeId);
						}
						callNext(node, extensions);
					// fragment ranking is higher than upper bound -> save fragment and prune search branch
					} else if ( maxPpmcc > upperBound) {
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
				if (lowerBound <=  maxPpmcc &&  maxPpmcc <= upperBound) {
					node.setRanking( maxPpmcc);
					node.setLastNumberOfEmbeddings(embeddings.size());
					callNext(node, extensions);
				// fragment ranking is higher than upper bound -> save fragment and prune search branch
				} else if ( maxPpmcc > upperBound) {
					node.store(true);
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
	
	public double PearsonProductMomentCorrelationCoefficient(ArrayList<Integer> attribute1, ArrayList<Integer> attribute2) {
		int n = attribute1.size();
		int i;
		
		double mean1 = mean(attribute1);
		double mean2 = mean(attribute2);
		double deviation1 = standardDeviation(attribute1);
		double deviation2 = standardDeviation(attribute2);
		
		double ppmcc = 0;
		for (i = 0; i < n; i++) {
			ppmcc += attribute1.get(i) * attribute2.get(i);
		}
		ppmcc = ppmcc - (n * mean1 * mean2);
		ppmcc = ppmcc / (n * deviation1 * deviation2);
		
		if (n * deviation1 * deviation2 == 0) {
			ppmcc = 0;
		}
		
		return Math.abs(ppmcc);
	}
	
	public double standardDeviation(ArrayList<Integer> data) {
		int n = data.size();
		int i;
		
		double m = mean(data);
		
		double deviation = 0;
		for (i = 0; i < n; i++) {
			deviation += (data.get(i) - m) * (data.get(i) - m);
		}
		deviation = deviation / (n -1);
		deviation = Math.sqrt(deviation);
		
		return deviation;
	}
	
	public double mean(ArrayList<Integer> data) {
		int n = data.size();
		int i;
		
		double mean = 0;
		for (i = 0; i < n; i++) {
			mean += data.get(i);
		}
		mean = mean / n;
		
		return mean;
	}
} 
