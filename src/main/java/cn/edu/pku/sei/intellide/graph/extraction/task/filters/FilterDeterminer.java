package cn.edu.pku.sei.intellide.graph.extraction.task.filters;

import org.apache.commons.lang3.ArrayUtils;

import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Rules;

public class FilterDeterminer {
	public static boolean isValuable(String word) {
		return ArrayUtils.contains(Rules.valuable_determiners, word);
	}

}
