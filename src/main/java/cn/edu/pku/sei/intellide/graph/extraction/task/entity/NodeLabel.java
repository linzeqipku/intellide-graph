package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

public enum NodeLabel {
	DEFAULT(0), // 默认
	VP(1), // 动词短语
	NP(2), // 名词短语
	PP(3), // 介词短语
	verb(4), // 动词
	noun(5), // 名词
	adj(6), // 形容词
	conj(7), // 连词/介词/助词(particle)
	other(8)// 其他，如冠词等
	;

	private int number; // 定义自定义的变量

	private NodeLabel(int typeNum) {
		this.number = typeNum;
	}

	public int getNumber() {
		return number;
	}

	public static NodeLabel getLabel(String str) {
		switch (str) {
		case "VP":
			return VP;
		case "NP":
			return NP;
		case "PP":
			return PP;
		case "verb":
			return verb;
		case "noun":
			return noun;
		case "adj":
			return adj;
		case "conj":
			return conj;
		case "other":
			return other;
		default:
			return DEFAULT;
		}
	}

	public static void main(String[] args) {
		for (NodeLabel v : values()) {
			System.out.println(v);
		}
	}
}
