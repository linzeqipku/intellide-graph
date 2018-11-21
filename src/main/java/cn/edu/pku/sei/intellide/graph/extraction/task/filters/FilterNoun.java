package cn.edu.pku.sei.intellide.graph.extraction.task.filters;

import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Rules;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class FilterNoun {
	private PhraseInfo		phrase;
	private HashSet<String>	nouns	= new HashSet<String>();

	public FilterNoun(PhraseInfo phrase) {
		this.phrase = phrase;
		extractNouns(phrase);
	}

	public boolean filter() {
		boolean qaNouns = filterQANouns();
		boolean stopNouns = filterStopNouns();
		boolean unlikeNouns = filterUnlikeNouns();
		boolean codeElement = filterCODEElement();

		// 不能直接在if中调用方法，避免短路跳出
		if (qaNouns && stopNouns && unlikeNouns && codeElement)
			return true;
		else
			return false;
	}

	private boolean extractNouns(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		if (phrase.getPhraseType() == PhraseInfo.PHRASE_TYPE_VP) {
			if (phrase.hasProof(ProofType.FORM_VP_NP) || phrase.hasProof(ProofType.FORM_VP_NP_PP)) {
				String filterPattern = "VP < " + Rules.NP_NN_PATTERN;

				TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
				TregexMatcher matcher = tregexPattern.matcher(phraseTree);
				while (matcher.find()) {
					Tree t1 = matcher.getNode(Rules.NAME_NP_NN);
					Tree t2 = matcher.getNode(Rules.NAME_NP_NP_NN);
					if (t1 != null) {
						String word = t1.getLeaves().get(0).toString().trim();
						nouns.add(word);
					}
					if (t2 != null) {
						String word = t2.getLeaves().get(0).toString().trim();
						nouns.add(word);
					}
				}
			}
			if (phrase.hasProof(ProofType.FORM_VP_PP) || phrase.hasProof(ProofType.FORM_VP_NP_PP)) {
				String filterPattern = "VP < " + Rules.PP_PATTERN;

				TregexPattern tregexPattern = TregexPattern.compile(filterPattern);
				TregexMatcher matcher = tregexPattern.matcher(phraseTree);
				while (matcher.find()) {
					Tree t1 = matcher.getNode(Rules.NAME_NP_NN);
					Tree t2 = matcher.getNode(Rules.NAME_NP_NP_NN);
					Tree t3 = matcher.getNode(Rules.NAME_PP_NN);

					if (t1 != null) {
						String word = t1.getLeaves().get(0).toString().trim();
						nouns.add(word);
					}
					if (t2 != null) {
						String word = t2.getLeaves().get(0).toString().trim();
						nouns.add(word);
					}
					if (t3 != null) {
						String word = t3.getLeaves().get(0).toString().trim();
						nouns.add(word);
					}
				}
			}
		}

		// System.out.println("===noun filter===");
		//
		// tree.pennPrint();
		// System.out.println(phrase.getProofString());
		// System.out.println(nouns);
		// System.out.println("========");
		return false;
	}

	public boolean filterCODEElement() {
		boolean flag = true;
		if (nouns != null) {
			for (String noun : nouns) {
				if (noun.startsWith("CODE")) {
					if (noun.length() > 4 && CharUtils.isAsciiAlphanumeric(noun.charAt(4))) {
						Proof proof = new Proof(ProofType.FAIL_QA_NOUN);
						proof.setEvidence(noun);
						phrase.addProof(proof);
						flag = false;
					}
				}
			}
		}
		return flag;
	}

	public boolean filterQANouns() {
		boolean flag = true;
		if (nouns != null) {
			for (String noun : nouns) {
				if (ArrayUtils.contains(Rules.qa_nouns, noun)) {
					Proof proof = new Proof(ProofType.FAIL_QA_NOUN);
					proof.setEvidence(noun);
					phrase.addProof(proof);
					flag = false;
				}
			}
		}
		return flag;
	}

	public boolean filterStopNouns() {
		boolean flag = true;
		if (nouns != null) {
			for (String noun : nouns) {
				if (ArrayUtils.contains(Rules.stop_nouns, noun)) {
					Proof proof = new Proof(ProofType.FAIL_STOP_NOUN);
					proof.setEvidence(noun);
					phrase.addProof(proof);
					flag = false;
				}
			}
		}
		return flag;
	}

	public boolean filterUnlikeNouns() {
		boolean flag = true;
		if (nouns != null) {
			for (String noun : nouns) {
				if (ArrayUtils.contains(Rules.unlike_nouns, noun)) {
					Proof proof = new Proof(ProofType.FAIL_UNLIKE_NOUN);
					proof.setEvidence(noun);
					phrase.addProof(proof);
					flag = false;
				}
			}
		}
		return flag;
	}

}
