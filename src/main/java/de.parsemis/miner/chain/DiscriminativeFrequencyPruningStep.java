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



/**
 * This class implements the pruning of fragments according minSup in the Classes of 
 * their Supergraphs.
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
public class DiscriminativeFrequencyPruningStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {
	
	final ArrayList<Integer> classMinSup;
	
	public DiscriminativeFrequencyPruningStep(final MiningStep<NodeType, EdgeType> next,
			final ArrayList<Integer> classMinSup) {
		
		super(next);
		this.classMinSup = classMinSup;
	}
	
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		
		final Fragment<NodeType, EdgeType> fragment = node.toFragment();
		final Collection<Embedding<NodeType, EdgeType>> embeddings = fragment.getEmbeddings();
		final Graph<NodeType, EdgeType> graph = fragment.toGraph();
		
		int[] sup = new int[this.classMinSup.size()];
		for (final Embedding<NodeType, EdgeType> embedding : embeddings) {
			int classNumber = embedding.toHPEmbedding().getSuperGraph().getClassNumber();
			if (classNumber < sup.length) {
				sup[classNumber]++;
			}
		}
		
		boolean store = true;
		for (int i = 0; i < sup.length; i++) {
			if (sup[i] < this.classMinSup.get(i)) {
				store = false;
			}
		}
		
		if (store) {
			node.store(true);
			callNext(node, extensions);
		} else {
			node.store(false);
		}
		
	}
}