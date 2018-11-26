package cn.edu.pku.sei.intellide.graph.extraction.task.filters;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.NLPParser;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.PhraseExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Rules;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class FilterBeVerb {

	public static boolean filterInRootOnly(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		// __ < ( /VB.*/ = vb !< be|am|is|are|was|were|being|been|'m|'s|'re )
		String filterPattern = "__ < ( /VB.*/ = vb < " + Rules.ruleWordsConjuctionForTregex(Rules.BE_VERBS)
				+ " ) ";
		// System.out.println(filterPattern);

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果按照过滤be动词的pattern无法匹配，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_BE_VERB_ROOT);
			Tree evdTree = matcher.getNode("vb");
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
		if (phraseTree == null)
			return false;

		// __ !<< be|am|is|are|was|were|being|been|'m|'s|'re
		String filterPattern = "__ << ( /VB.*/=vb <" + Rules.ruleWordsConjuctionForTregex(Rules.BE_VERBS)
				+ " ) ";

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern.toString());
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果按照过滤be动词的pattern无法匹配，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_BE_VERB_THOROUGHLY);
			Tree evdTree = matcher.getNode("vb");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}


	public static void main(String args[]) {
		String string = "I have been a doctor, he being a dancer, and he said that his father was a farmer, his family are Chinese.";

		Tree t = NLPParser.parseGrammaticalTree(string);
		t.pennPrint();
		PhraseInfo[] ps = PhraseExtractor.extractVerbPhrases(t);

		for (PhraseInfo phrase : ps) {
			System.out.println("==================");
			phrase.getSyntaxTree();
			filterInRootOnly(phrase);
			filterThoroughly(phrase);
			System.out.println(phrase.getText());
			System.out.println(phrase.getSyntaxTree());
			System.out.println(phrase.getProofString());
		}
	}

}
