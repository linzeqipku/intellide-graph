package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.*;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.TreeUtils;
import edu.stanford.nlp.trees.Tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NounPhraseStructureInfo implements Serializable {
	private static final long		serialVersionUID	= 5909005832811390899L;

	// private String uuid;
	private List<WordInfo>			wordChain			= new ArrayList<>();
	private PrepPhraseStructureInfo	subPP				= null;					// 可能有

	public NounPhraseStructureInfo() {
		super();
		// this.uuid = UUID.randomUUID().toString();
	}

	// public NounPhraseStructureInfo(PhraseInfo phrase) {
	// this(phrase.getStemmedTree());
	// }

	// stemmed tree
	public NounPhraseStructureInfo(Tree tree) {
		this();
		if (!TreeUtils.isNP(tree))
			return;

		Tree[] children = tree.children();
		for (int i = 0; i < children.length; i++) {
			Tree child = children[i];
			if (TreeUtils.isDT(child)) {
				String dt = TreeUtils.getLeafString(child);

				// 保留all、each、both、as、or这类冠词
				// if (FilterDeterminer.isValuable(dt)) {
				// WordInfo dtWord = DictionaryInfo.addOtherWord(dt);
				// wordChain.add(dtWord);
				// }

				// new policy: reserve all the determiners
				WordInfo dtWord = DictionaryInfo.addOtherWord(dt);
				wordChain.add(dtWord);
			}
			else if (TreeUtils.isAdjective(child)) {
				// 保留形容词
				String adj = TreeUtils.getLeafString(child);
				AdjectiveInfo adjInfo = DictionaryInfo.addAdjective(adj);
				wordChain.add(adjInfo);
			}
			else if (TreeUtils.isNN(child)) {
				String noun = TreeUtils.getLeafString(child);
				NounInfo nounInfo = DictionaryInfo.addNoun(noun);
				wordChain.add(nounInfo);
			}
			else if (TreeUtils.isNP(child)) {
				// 子np中的所有名词提上一级到当前名词链，pp则舍去
				NounPhraseStructureInfo subNP = new NounPhraseStructureInfo(child);
				List<WordInfo> subNPWordChain = subNP.getWordChain();
				wordChain.addAll(subNPWordChain);
			}
			else if (TreeUtils.isCC(child)) {
				// 保留连词
				String cc = TreeUtils.getLeafString(child);
				ConjunctionInfo conjInfo = DictionaryInfo.addConjunction(cc);
				wordChain.add(conjInfo);
			}
			else if (TreeUtils.isPP(child)) {
				// 如果是np下辖pp的形式, 遇到pp时的处理
				// 如vp=vb+np（np+pp）时, 且当前节点为根节点的儿子, 则保留;
				subPP = new PrepPhraseStructureInfo(child);
			}
		}

		adjust();
	}

	public void adjust() {
		if (wordChain.size() <= 0) {
			// 如果名词结构中不包含名词，说明该名词可能是个代词，用sth.代替
			NounInfo sth = DictionaryInfo.addNoun("SOMETHING");
			wordChain.add(sth);
		}
	}

	// @Override
	// public String getUuid() {
	// return uuid;
	// }

	@Deprecated
	public void addWordToChain(WordInfo word) {
		if (wordChain == null)
			wordChain = new ArrayList<>();
		wordChain.add(word);
	}

	public NounInfo getKeyNoun() {
		for (int i = wordChain.size() - 1; i >= 0; i--) {
			WordInfo word = wordChain.get(i);
			if (word instanceof NounInfo)
				return (NounInfo) word;
		}
		return null;
	}

	public PrepPhraseStructureInfo getSubPP() {
		return subPP;
	}

	public void setSubPP(PrepPhraseStructureInfo directPP) {
		this.subPP = directPP;
	}

	public List<WordInfo> getWordChain() {
		return wordChain;
	}

	public void setWordChain(List<WordInfo> wordChain) {
		this.wordChain = wordChain;
	}

	public String toNLText() {
		StringBuilder str = new StringBuilder();
		if (wordChain != null) {
			for (int i = 0; i < wordChain.size(); i++) {
				if (i > 0)
					str.append(" ");
				str.append(wordChain.get(i).toString());
			}
		}
		if (subPP != null)
			str.append(" " + subPP.toNLText());
		return str.toString();
	}

	public String toTaskText() {
		StringBuilder str = new StringBuilder();
		if (wordChain != null) {
			for (int i = 0; i < wordChain.size(); i++) {
				if (i > 0)
					str.append(" ");
				WordInfo word = wordChain.get(i);
				str.append(word.toString() + TaskInfo.POS_SEPARATOR);
				if (word instanceof NounInfo)
					str.append(TaskInfo.POS_NOUN);
				else if (word instanceof AdjectiveInfo)
					str.append(TaskInfo.POS_ADJ);
				else if (word instanceof VerbInfo)
					str.append(TaskInfo.POS_VERB);
				else if (word instanceof ConjunctionInfo)
					str.append(TaskInfo.POS_CONJ);
				else
					str.append(TaskInfo.POS_OTHER);
			}
			// replace kernel noun pos tag
			int lastIndexOfNN = str.lastIndexOf(TaskInfo.POS_SEPARATOR + TaskInfo.POS_NOUN);
			if (lastIndexOfNN > 0)
				str.replace(lastIndexOfNN,
						lastIndexOfNN + TaskInfo.POS_SEPARATOR.length() + TaskInfo.POS_NOUN.length(),
						TaskInfo.POS_SEPARATOR + TaskInfo.POS_KERNEL_NOUN);
		}
		if (subPP != null)
			str.append(" " + subPP.toTaskText());
		return str.toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("<NP ");
		if (wordChain != null) {
			for (int i = 0; i < wordChain.size(); i++) {
				str.append(wordChain.get(i).toString());
				if (i < wordChain.size() - 1)
					str.append(" ");
			}
		}
		if (subPP != null)
			str.append(subPP.toString());
		str.append("/>");
		return str.toString();
	}

	public List<WordInfo> toWordList() {
		List<WordInfo> result = new ArrayList<>();
		result.addAll(wordChain);
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
