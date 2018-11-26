package cn.edu.pku.sei.intellide.graph.extraction.task.filters;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.NLPParser;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.PhraseExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class FilterModalVerb {

	public static boolean filterInRootOnly(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		StringBuilder filterPattern = new StringBuilder("__ < MD=md");

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern.toString());
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果按照过滤情态动词的pattern无法匹配，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_MODAL_VERB_ROOT);
			Tree evdTree = matcher.getNode("md");
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

		StringBuilder filterPattern = new StringBuilder("__ << MD=md");

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern.toString());
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果按照过滤情态动词的pattern无法匹配，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_MODAL_VERB_THOROUGHLY);
			Tree evdTree = matcher.getNode("md");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}


	public static void main(String args[]) {
		String string = "I could not be a doctor, he'll be a dancer, and he wouldn't said that his father might be a farmer, his family will never be Chinese.";

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
