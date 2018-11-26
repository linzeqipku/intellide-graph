package cn.edu.pku.sei.intellide.graph.extraction.task.filters;

import java.util.Arrays;
import java.util.HashSet;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Rules;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.TextUtils;

public class FilterContext {
	private PhraseInfo				phrase;
	private String					sentence;
	private String					precedingContext;
	private static HashSet<String>	qaContextList;

	static {
		qaContextList = new HashSet<String>();

		qaContextList.addAll(Arrays.asList(Rules.qa_phrases));
		qaContextList.addAll(Arrays.asList(Rules.qa_verbs));
		qaContextList.addAll(Arrays.asList(Rules.qa_nouns));
	}

	public FilterContext(PhraseInfo phrase, String sentence) {
		this.phrase = phrase;
		this.setSentence(sentence);
		this.precedingContext = TextUtils.getPrecedingContext(phrase.getText(), sentence);
		// System.out.println(precedingContext);
	}

	public boolean filter() {
		if (precedingContext == null || "".equals(precedingContext.trim()))
			return false;
		for (String qa : qaContextList) {
			if (precedingContext.endsWith(qa)) {
				Proof proof = new Proof(ProofType.CONTEXT_IMMEDIATE);
				proof.setEvidence(qa);
				phrase.addProof(proof);
			}
		}
		if (phrase.hasProof(ProofType.CONTEXT_IMMEDIATE))
			return true;

		for (String qa : qaContextList) {
			qa = qa + " ";// qa关键词后面加一个空格的原因是，避免出现了done这样的词，既识别出done也识别出do
			int idx = precedingContext.indexOf(qa);
			if (idx < 0)
				continue;

			int nearbyThreshold = 12;
			if (idx + qa.length() + nearbyThreshold >= precedingContext.length()) {
				Proof proof = new Proof(ProofType.CONTEXT_NEARBY);
				proof.setEvidence(qa);
				phrase.addProof(proof);
			}
		}
		if (phrase.hasProof(ProofType.CONTEXT_NEARBY))
			return true;

		for (String qa : qaContextList) {
			int precedingThreshold = 50;
			int idx = precedingContext.indexOf(qa);
			if (idx < 0)
				continue;
			if (idx + qa.length() + precedingThreshold >= precedingContext.length()) {
				Proof proof = new Proof(ProofType.CONTEXT_PRECEDING);
				proof.setEvidence(qa);
				phrase.addProof(proof);
			}
		}
		if (phrase.hasProof(ProofType.CONTEXT_PRECEDING))
			return true;
		else
			return false;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

}
