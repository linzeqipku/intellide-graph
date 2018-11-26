package cn.edu.pku.sei.intellide.graph.extraction.task.filters;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Rules;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class FilterVerb {
	public static boolean filter(PhraseInfo phrase) {
		boolean have = filterHave(phrase);
		boolean qaVerbs = filterQAVerbs(phrase);
		boolean stopVerbs = filterStopVerbs(phrase);
		boolean unlikeVerbs = filterUnlikeVerbs(phrase);

		// 不能直接在if中调用方法，避免短路跳出
		if (have && qaVerbs && stopVerbs && unlikeVerbs)
			return true;
		else
			return false;
	}

	public static boolean filterHave(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		// __ < ( /VB.*/ < have|has|had|'ve|'s|'d )
		String filterPattern = "__ < ( /VB.*/=vb < " + Rules.ruleWordsConjuctionForTregex(Rules.HAVE_VERBS)
				+ " ) ";
		// System.out.println(filterPattern);

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_HAVE_VERB);
			Tree evdTree = matcher.getNode("vb");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}

	public static boolean filterQAVerbs(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		String filterPattern = "__ < ( /VB.*/=vb < " + Rules.ruleWordsConjuctionForTregex(Rules.qa_verbs)
				+ " ) ";

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_STOP_VERB);
			Tree evdTree = matcher.getNode("vb");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}

	public static boolean filterStopVerbs(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		String filterPattern = "__ < ( /VB.*/=vb < " + Rules.ruleWordsConjuctionForTregex(Rules.stop_verbs)
				+ " ) ";

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_STOP_VERB);
			Tree evdTree = matcher.getNode("vb");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}

	public static boolean filterUnlikeVerbs(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		String filterPattern = "__ < ( /VB.*/=vb < " + Rules.ruleWordsConjuctionForTregex(Rules.unlike_verbs)
				+ " ) ";

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_UNLIKE_VERB);
			Tree evdTree = matcher.getNode("vb");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}

}
