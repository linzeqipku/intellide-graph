package cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5880763504381129923L;

	private static List<WordInfo>				words		= new ArrayList<>();
	private static Map<String, NounInfo>		nounIndex	= new HashMap<String, NounInfo>();
	private static Map<String, VerbInfo>		verbIndex	= new HashMap<String, VerbInfo>();
	private static Map<String, AdjectiveInfo>	adjIndex	= new HashMap<String, AdjectiveInfo>();
	private static Map<String, ConjunctionInfo>	conjIndex	= new HashMap<String, ConjunctionInfo>();
	private static Map<String, WordInfo>		otherIndex	= new HashMap<String, WordInfo>();

	public static List<WordInfo> getVocabulary() {
		return words;
	}

	public static NounInfo addNoun(String noun) {
		if (!nounIndex.containsKey(noun)) {
			NounInfo newNoun = new NounInfo(noun);
			words.add(newNoun);
			nounIndex.put(noun, newNoun);
		}
		return nounIndex.get(noun);
	}

	public static VerbInfo addVerb(String verb) {
		if (!verbIndex.containsKey(verb)) {
			VerbInfo newVerb = new VerbInfo(verb);
			words.add(newVerb);
			verbIndex.put(verb, newVerb);
		}
		return verbIndex.get(verb);
	}

	public static AdjectiveInfo addAdjective(String adjective) {
		if (!adjIndex.containsKey(adjective)) {
			AdjectiveInfo newAdj = new AdjectiveInfo(adjective);
			words.add(newAdj);
			adjIndex.put(adjective, newAdj);
		}
		return adjIndex.get(adjective);
	}

	public static ConjunctionInfo addConjunction(String conjunction) {
		if (!conjIndex.containsKey(conjunction)) {
			ConjunctionInfo newConj = new ConjunctionInfo(conjunction);
			words.add(newConj);
			conjIndex.put(conjunction, newConj);
		}
		return conjIndex.get(conjunction);
	}

	public static WordInfo addOtherWord(String otherWord) {
		if (!otherIndex.containsKey(otherWord)) {
			WordInfo newWord = new WordInfo(otherWord);
			words.add(newWord);
			otherIndex.put(otherWord, newWord);
		}
		return otherIndex.get(otherWord);
	}

	public static VerbInfo getVerb(String word) {
		return verbIndex.get(word);
	}

	public static NounInfo getNoun(String noun) {
		return nounIndex.get(noun);
	}

	public static AdjectiveInfo getAdjective(String adj) {
		return adjIndex.get(adj);
	}

	public static ConjunctionInfo getConjunction(String conj) {
		return conjIndex.get(conj);
	}

	public static WordInfo getWord(String word) {
		WordInfo wordInfo = otherIndex.get(word);
		if (wordInfo == null)
			for (WordInfo _word : words) {
				if (_word.getName().equals(word))
					wordInfo = _word;
			}
		return wordInfo;
	}
}
