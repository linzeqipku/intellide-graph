package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import org.apache.log4j.Logger;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.*;
import edu.stanford.nlp.trees.Tree;


public class PhraseFilter {

    public static void filter(PhraseInfo phrase, String sentence) {

        if (phrase.getPhraseType() == PhraseInfo.PHRASE_TYPE_VP)
            CheckerPhraseForm.checkVP(phrase);
        else if (phrase.getPhraseType() == PhraseInfo.PHRASE_TYPE_NP)
            CheckerPhraseForm.checkNP(phrase);

        FilterBeVerb.filterInRootOnly(phrase);
        FilterModalVerb.filterInRootOnly(phrase);

        FilterNegation.filterInRootOnly(phrase);
        FilterNegation.filterThoroughly(phrase);
        FilterPronoun.filter(phrase);

        FilterVerb.filter(phrase);

        FilterNoun filterNoun = new FilterNoun(phrase);
        filterNoun.filter();

        FilterPhrase.filter(phrase);

        FilterContext filterContext = new FilterContext(phrase, sentence);
        filterContext.filter();

    }


    public static void main(String[] args) {
        String string = "I'm trying to develop a complex report, and I need to set up the print areas for the excel file.";
        // "I am working with a Swing program and having a little trouble.";
        // "How can I get it into the giant box?" ;
        // "He got a score of 3.7 in the exams.";
        // " support for some of the `` Java Look and Feel Graphics Repository
        // '' actions .";
        // "He wants to immediately give us an idea of how things are scored.";
        // "I could never be playing around with natural language parse trees,
        // he wants to handle empty cells in excel sheets.";
        // "He wants to put that into this box, I hope I can move these from
        // there to his room.";
        // ", he'll be a dancer, and he wouldn't said that his father might be a
        // farmer, his family
        // will never be Chinese.";

		Tree t = NLPParser.parseGrammaticalTree(string);
		t.pennPrint();
		PhraseInfo[] ps = PhraseExtractor.extractVerbPhrases(t);

		for (PhraseInfo phrase : ps) {
			System.out.println("==================");
			phrase.getSyntaxTree();
			filter(phrase, string);
			if (phrase.getProofScore() <= 0)
				continue;
			System.out.println(phrase.getText());
			System.out.println("proofScore: " + phrase.getProofScore());
			System.out.println(phrase.getSyntaxTree());
			System.out.println(phrase.getProofString());
			System.out.println();
			for (Proof prf : phrase.getProofs()) {
				String str = prf.toString();
				Proof p2 = Proof.of(str);
				System.out.println(p2.toString());
			}
		}
    }

}
