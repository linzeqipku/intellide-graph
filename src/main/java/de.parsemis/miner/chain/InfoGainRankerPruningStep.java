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

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.HPEmbedding;


/**
 * This class implements the pruning of fragments according to the information gain of 
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
public class InfoGainRankerPruningStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {
	
	private final int minSize; // minimal size of fragments before they are checked by this class
	
	private final double lowerBound;
	
	private final double upperBound;
	
	private final boolean closeGraph;
	
	private final boolean subsequentBugs;
	
	
	public InfoGainRankerPruningStep(final MiningStep<NodeType, EdgeType> next,
			final int minSize, final double lowerBound, final double upperBound, final boolean subsequentBugs, final boolean closeGraph) {
		super(next);
		this.minSize = minSize;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.subsequentBugs = subsequentBugs;
		
		this.closeGraph = closeGraph;
	}
	
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		
		final Fragment<NodeType, EdgeType> fragment = node.toFragment();
		final Collection<Embedding<NodeType, EdgeType>> embeddings = fragment.getEmbeddings();
		final Graph<NodeType, EdgeType> graph = fragment.toGraph();		
				
		int i, e;
		
		boolean oldNode = false;
		 
		ArrayList<Integer> classes = new ArrayList<Integer>();
		ArrayList<Integer> weights = new ArrayList<Integer>();
		
		double maxInfoGain = 0.0;
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
					node.setEdgeRanking(i, infoGain(weights, classes, optimalSplitValue(weights, classes)));
				}
			}
			// determine maximum ranking
			maxInfoGain = 0.0;
			
			ArrayList<Integer> maxRankedEdgeIds = node.getMaxRankedEdgeIds();
			
			for (i = 0; i < graph.getEdgeCount(); i++) {
				if (maxRankedEdgeIds.indexOf(i) != -1) {
					if (node.getEdgeRanking(i) >= maxInfoGain) {
						maxInfoGain = node.getEdgeRanking(i);
						maxRankedEdgeId = i;
						oldNode = true;
					}
				}
			}
			for (i = 0; i < graph.getEdgeCount(); i++) {
				if (maxRankedEdgeIds.indexOf(i) == -1) {
					if (node.getEdgeRanking(i) >= maxInfoGain) {
						maxInfoGain = node.getEdgeRanking(i);
						maxRankedEdgeId = i;
						oldNode = false;
					}
				}
			}
			
			
			// look for subsequent bugs
			if (this.subsequentBugs) {
				if (node.getPruningMark()) {
					// subsequent bug found -> continue
					if (maxInfoGain == node.getRanking() && !oldNode) {
						node.addMaxRankedEdgeId(maxRankedEdgeId); //war nicht da -> bug?
						node.setRanking(maxInfoGain);
						node.setLastNumberOfEmbeddings(embeddings.size());
						callNext(node, extensions);
					// no further subsequent bugs found -> save fragment and prune search branch
					} else {
						node.store(true);
					}
				// node has not been marked for pruning
				} else {
					// fragment ranking is in between lower and upper bound -> continue 
					if (lowerBound <= maxInfoGain && maxInfoGain <= upperBound) {
						node.setRanking(maxInfoGain);
						node.setLastNumberOfEmbeddings(embeddings.size());
						if (!oldNode) {
							node.addMaxRankedEdgeId(maxRankedEdgeId);
						}
						callNext(node, extensions);
					// fragment ranking is higher than upper bound -> save fragment and prune search branch
					} else if (maxInfoGain > upperBound) {
						node.setPruningMark(true);
						node.setRanking(maxInfoGain);
						node.setLastNumberOfEmbeddings(embeddings.size());
						if (!oldNode) {
							node.addMaxRankedEdgeId(maxRankedEdgeId);
						}
						callNext(node, extensions);
					//fragment ranking is lower than lower bound -> prune search branch
					} else {
						node.store(false);
					}
				}
			// do not look for subsequent bugs
			} else {
				// fragment ranking is in between lower and upper bound -> continue 
				if (lowerBound <= maxInfoGain && maxInfoGain <= upperBound) {
					node.setRanking(maxInfoGain);
					node.setLastNumberOfEmbeddings(embeddings.size());
					callNext(node, extensions);
				// fragment ranking is higher than upper bound -> save fragment and prune search branch
				} else if (maxInfoGain > upperBound) {
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

	
	// determines the split_value maximizing InfoGain
	private double optimalSplitValue(ArrayList<Integer> data, ArrayList<Integer> classes) {
		int[] data2 = new int[data.size()];
		int i;
		double maxVal = 0.0;
		double curVal = 0.0;
		double splitVal = 0.0;
		double optimalSplitVal = 0.0;
		double temp1, temp2;
		
		// copy data
		for (i = 0; i < data.size(); i++) {
			data2[i] = data.get(i).intValue();
		}
		// sort
		java.util.Arrays.sort(data2);
		
		for (i = 0; i < data2.length - 1; i++) {
			temp1 = data2[i];
			temp2 = data2[i+1];
			splitVal = (temp1+temp2) / 2;
			curVal = infoGain(data, classes, splitVal);
			if (curVal >= maxVal) {
				maxVal = curVal;
				optimalSplitVal = splitVal;
			}
		}
		return optimalSplitVal;
	}
	
	// calculates InfoGain in respect to split_value
	private double infoGain(ArrayList<Integer> data, ArrayList<Integer> classes, double split_value) {
		return  entropy(classes) - entropyX(data, classes, split_value); // korrekt
	}
		
	// calculates entropy in respect to the buckets created by split_value // korrekt
	private double entropyX(ArrayList<Integer> data, ArrayList<Integer> classes, double split_value) {
		double ret = 0.0;
		double p1 = 0.0;
		double p2 = 0.0;
		int i, c, val;
		ArrayList<Integer> classes1 = new ArrayList<Integer>();
		ArrayList<Integer> classes2 = new ArrayList<Integer>();
		
		
		// seperate data, count bags
		for (i = 0; i < classes.size(); i++) {
			c = classes.get(i).intValue();
			val = data.get(i).intValue();
			if (val < split_value) {
				classes1.add(c);
				p1++;
			} else {
				classes2.add(c);
				p2++;
			}
		}
		p1 = p1/classes.size();
		p2 = p2/classes.size();
		ret = p1 * entropy(classes1) + p2 * entropy(classes2); 
		return ret;
	}
	
	// Shannon Entropy (log_2)
	private double entropy(ArrayList<Integer> classes) {
		double ret = 0.0;
		double p;
		double temp1, temp2;
		int[] count = new int[classes.size() * 10];
		int i;
		// count occurrence of each class  
		for (i = 0; i < classes.size(); i++) {
			count[classes.get(i).intValue()]++;
		}
		// calculate entropy
		for (i = 0; i < classes.size(); i++) {
			if (count[i] == 0) {
			} else {
				temp1 = count[i];
				//temp1 = count.get(i);
				temp2 = classes.size();
				p = temp1 / temp2;
				ret = ret - p * ld(p);
			}
		}
		return ret;
	}
	
	// log_2
	public static double ld(double x)	{
		return Math.log(x) / Math.log(2);
	}

	public String serialize(final Graph<NodeType, EdgeType> graph) {
		// serialize graph
		String text = "";
		text += "t # " + graph.getName() + "\n";
		for (int i = 0; i < graph.getNodeCount(); i++) {
			text += "v " + graph.getNode(i).getIndex() + " "
					+ graph.getNode(i).getLabel() + "\n";
		}
		for (int i = 0; i < graph.getEdgeCount(); i++) {
			if (graph.getEdge(i).getDirection() == Edge.INCOMING) {
				text += "e " + graph.getNodeB(graph.getEdge(i)).getIndex()
						+ " " + graph.getNodeA(graph.getEdge(i)).getIndex()
						+ " " + graph.getEdge(i).getLabel() + "\n";
			} else {
				text += "e " + graph.getNodeA(graph.getEdge(i)).getIndex()
						+ " " + graph.getNodeB(graph.getEdge(i)).getIndex()
						+ " " + graph.getEdge(i).getLabel() + "\n";
			}
		}
		return text;
	}

}