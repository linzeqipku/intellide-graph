package cn.edu.pku.sei.intellide.graph.extraction.task.entity.dictionary;

import java.io.Serializable;

public class WordInfo implements Serializable {
	private static final long	serialVersionUID	= -1402796693691596422L;
	private String				name;
	// private String uuid;

	public WordInfo(String name) {
		// this.uuid = UUID.randomUUID().toString();
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WordInfo))
			return false;
		return name.equals(((WordInfo) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// @Override
	// public String getUuid() {
	// return uuid;
	// }

	@Override
	public String toString() {
		return name;
	}
}
