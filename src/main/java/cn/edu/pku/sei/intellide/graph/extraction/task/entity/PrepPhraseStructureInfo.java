package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.ConjunctionInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.DictionaryInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.WordInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.TreeUtils;
import edu.stanford.nlp.trees.Tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrepPhraseStructureInfo implements Serializable {
	private static final long		serialVersionUID	= 316814757213205466L;

	// private String uuid;
	private ConjunctionInfo			conjunction			= null;
	private NounPhraseStructureInfo	subNP				= null;
	private PrepPhraseStructureInfo	subPP				= null;					// 一般不会有吧？

	public PrepPhraseStructureInfo() {
		super();
		// this.uuid = UUID.randomUUID().toString();
	}

	// public PrepPhraseStructureInfo(PhraseInfo phrase) {
	// this(phrase.getStemmedTree());
	// }

	// stemmed tree
	public PrepPhraseStructureInfo(Tree tree) {
		this();
		if (!TreeUtils.isPP(tree))
			return;

		Tree[] children = tree.children();
		for (int i = 0; i < children.length; i++) {
			Tree child = children[i];
			if (TreeUtils.isPreposition(child)) {
				// 保留介词和to
				// 只保留第一个遇到的？
				if (conjunction == null) {
					String prep = TreeUtils.getLeafString(child);
					conjunction = DictionaryInfo.addConjunction(prep);
				}
			}
			else if (TreeUtils.isNP(child)) {
				subNP = new NounPhraseStructureInfo(child);
			}
			else if (TreeUtils.isPP(child)) {
				subPP = new PrepPhraseStructureInfo(child);
			}
			else if (TreeUtils.isCC(child)) {
				// 不保留连词，如and等
			}
		}
	}

	// @Override
	// public String getUuid() {
	// return uuid;
	// }

	public ConjunctionInfo getConjunction() {
		return conjunction;
	}

	public void setConjunction(ConjunctionInfo conjunction) {
		this.conjunction = conjunction;
	}

	public NounPhraseStructureInfo getSubNP() {
		return subNP;
	}

	public void setSubNP(NounPhraseStructureInfo directNP) {
		this.subNP = directNP;
	}

	public PrepPhraseStructureInfo getSubPP() {
		return subPP;
	}

	public void setSubPP(PrepPhraseStructureInfo pp) {
		this.subPP = pp;
	}

	public String toNLText() {
		StringBuilder str = new StringBuilder();
		if (conjunction != null)
			str.append(conjunction.toString());
		if (subNP != null)
			str.append(" " + subNP.toNLText());
		if (subPP != null)
			str.append(" " + subPP.toNLText());
		return str.toString();
	}

	public String toTaskText() {
		StringBuilder str = new StringBuilder();
		if (conjunction != null)
			str.append(conjunction.toString() + TaskInfo.POS_SEPARATOR + TaskInfo.POS_CONJ);
		if (subNP != null)
			str.append(" " + subNP.toTaskText());
		if (subPP != null)
			str.append(" " + subPP.toTaskText());
		return str.toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("<PP ");
		if (conjunction != null)
			str.append("<CONJ " + conjunction.toString() + ">");
		if (subNP != null)
			str.append(subNP.toString());
		if (subPP != null)
			str.append(subPP.toString());
		str.append("/>");
		return str.toString();
	}

	public List<WordInfo> toWordList() {
		List<WordInfo> result = new ArrayList<>();
		if (conjunction != null)
			result.add(conjunction);
		if (subNP != null)
			result.addAll(subNP.toWordList());
		if (subPP != null)
			result.addAll(subPP.toWordList());
		return result;
	}

	public Set<String> toWordBag() {
		Set<String> wordBag = new HashSet<>();
		List<WordInfo> words = toWordList();
		for (WordInfo wordInfo : words) {
			wordBag.add(wordInfo.getName());
		}
		return wordBag;
	}
}
