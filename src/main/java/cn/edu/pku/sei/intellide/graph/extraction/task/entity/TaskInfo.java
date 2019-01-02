package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NounPhraseStructureInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.NounInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary.WordInfo;

import java.io.Serializable;

public class TaskInfo implements Serializable {
	private static final long	serialVersionUID	= 3352927770366598581L;
	public static final String	TABLE_NAME			= "tasks";

	public static final String	POS_SEPARATOR		= "/";
	public static final String	POS_VERB			= "vb";
	public static final String	POS_NOUN			= "nn";
	public static final String	POS_KERNEL_NOUN		= "knn";
	public static final String	POS_ADJ				= "adj";
	public static final String	POS_CONJ			= "conj";
	public static final String	POS_OTHER			= "other";

	private int					id;
	private String				text;
	private String				kernelVerb;
	private String				kernelNoun;

	private int[]				phrasesId;

	public TaskInfo() {
		super();
	}

	public TaskInfo(VerbalPhraseStructureInfo vps) {
		this();
		this.text = vps.toTaskText();

		this.kernelVerb = vps.getVerb().toString();
		NounPhraseStructureInfo subNP = vps.getSubNP();
		if (subNP != null) {
			for (int i = subNP.getWordChain().size() - 1; i >= 0; i--) {
				WordInfo word = subNP.getWordChain().get(i);
				if (word instanceof NounInfo) {
					this.kernelNoun = word.toString();
					break;
				}
			}
		}
	}

	public String toPlainText() {
		return parseTaskStringToPlainText(text);
	}

	public static String parseTaskStringToPlainText(String task) {
		task = task.replaceAll(POS_SEPARATOR + POS_VERB, "");
		task = task.replaceAll(POS_SEPARATOR + POS_ADJ, "");
		task = task.replaceAll(POS_SEPARATOR + POS_CONJ, "");
		task = task.replaceAll(POS_SEPARATOR + POS_KERNEL_NOUN, "");
		task = task.replaceAll(POS_SEPARATOR + POS_NOUN, "");
		task = task.replaceAll(POS_SEPARATOR + POS_OTHER, "");
		return task;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getKernelVerb() {
		return kernelVerb;
	}

	public void setKernelVerb(String kernelVerb) {
		this.kernelVerb = kernelVerb;
	}

	public String getKernelNoun() {
		return kernelNoun;
	}

	public void setKernelNoun(String kernelNoun) {
		this.kernelNoun = kernelNoun;
	}

	public int[] getPhrasesId() {
		return phrasesId;
	}

	public void setPhrasesId(int[] phrasesId) {
		this.phrasesId = phrasesId;
	}

}
