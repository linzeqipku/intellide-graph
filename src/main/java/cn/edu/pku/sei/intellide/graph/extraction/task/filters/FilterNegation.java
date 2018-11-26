package cn.edu.pku.sei.intellide.graph.extraction.task.filters;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Rules;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class FilterNegation {
	public static boolean filterInRootOnly(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		// __ < (RB < not|n't|never) | < (ADVP < (RB < not|n't|never))
		String filterPattern = "__ < (RB=neg < " + Rules.ruleWordsConjuctionForTregex(Rules.NEGATIVE_WORDS)
				+ ") | < (ADVP < (RB=neg-nested < " + Rules.ruleWordsConjuctionForTregex(Rules.NEGATIVE_WORDS)
				+ "))";
		// System.out.println(filterPattern);

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果短语中包含否定词，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_NEGATION_ROOT);
			Tree evdTree = matcher.getNode("neg");
			if (evdTree == null)
				evdTree = matcher.getNode("neg-nested");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}

	public static boolean filterThoroughly(PhraseInfo phrase) {
		if (phrase == null)
			return false;

		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());

		// __ << not|n't|never
		String filterPattern = "__ << ( __=neg <" + Rules.ruleWordsConjuctionForTregex(Rules.NEGATIVE_WORDS)
				+ " )";

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern.toString());
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果短语中包含否定词，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_NEGATION_THOROUGHLY);
			Tree evdTree = matcher.getNode("neg");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}
}
