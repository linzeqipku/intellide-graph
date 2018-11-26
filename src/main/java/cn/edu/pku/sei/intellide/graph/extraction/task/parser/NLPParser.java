package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import edu.stanford.nlp.trees.Tree;

public class NLPParser {

    public static void main(String[] args) {
//        NLPParser.initializeConnection();
//        parseAllSentences();
//        NLPParser.finalizeConnection();
    }

    public static Tree parseGrammaticalTree(String sentence) {
//		if (DocumentParser.hasTooManyIllegalSymbols(sentence))
//			return null;

        // Add a period to the end of sentence, if there is none.
        int i;
        for (i = sentence.length() - 1; i >= 0; i--) {
            char ch = sentence.charAt(i);
            if (Character.isLetter(ch) || Character.isDigit(ch))
                break;
        }
        sentence = sentence.substring(0, i + 1) + ".";

        Tree tree = StanfordParser.parseTree(sentence);

        return tree;
    }

}

