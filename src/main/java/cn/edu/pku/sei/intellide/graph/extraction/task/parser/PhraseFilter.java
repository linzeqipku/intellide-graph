package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import org.apache.log4j.Logger;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.CheckerPhraseForm;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterBeVerb;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterContext;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterModalVerb;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterNegation;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterNoun;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterPhrase;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterPronoun;
import cn.edu.pku.sei.intellide.graph.extraction.task.filters.FilterVerb;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.util.*;

public class PhraseFilter {
    public static final Logger logger = Logger.getLogger(PhraseFilter.class);

    public static void filter(PhraseInfo phrase, String sentence) {
        // logger.info("[FilterPhrase] filtering...");

        if (phrase.getPhraseType() == PhraseInfo.PHRASE_TYPE_VP)
            CheckerPhraseForm.checkVP(phrase);
        else if (phrase.getPhraseType() == PhraseInfo.PHRASE_TYPE_NP)
            CheckerPhraseForm.checkNP(phrase);

        // if (phrase.hasProof(ProofType.ILLEGAL_VP_PHRASE) ||
        // phrase.hasProof(ProofType.ILLEGAL_NP_PHRASE))
        // return;

        // VP_NP_FormChecker.check(phrase);
        // VP_NP_PP_FormChecker.check(phrase);
        // VP_PP_FormChecker.check(phrase);
        // VP_PRP_PP_FormChecker.check(phrase);
        // VP_DT_PP_FormChecker.check(phrase);

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

        // System.err.println("[FilterPhrase] filtering finished, time: " + (t2
        // - t1) + "ms");
    }

    // public static void filter(PhraseInfo phrase, SentenceInfo sentence) {
    // // logger.info("[FilterPhrase] filtering...");
    // long t1 = System.currentTimeMillis();
    //
    // if (phrase.isVP())
    // CheckerPhraseForm.checkVP(phrase);
    // else if (phrase.isNP())
    // CheckerPhraseForm.checkNP(phrase);
    //
    // // if (phrase.hasProof(ProofType.ILLEGAL_VP_PHRASE) ||
    // // phrase.hasProof(ProofType.ILLEGAL_NP_PHRASE))
    // // return;
    //
    // // VP_NP_FormChecker.check(phrase);
    // // VP_NP_PP_FormChecker.check(phrase);
    // // VP_PP_FormChecker.check(phrase);
    // // VP_PRP_PP_FormChecker.check(phrase);
    // // VP_DT_PP_FormChecker.check(phrase);
    //
    // FilterBeVerb.filterRoot(phrase);
    // FilterModalVerb.filterRoot(phrase);
    //
    // FilterNegation.filterRoot(phrase);
    // FilterNegation.filterAll(phrase);
    // FilterPronoun.filter(phrase);
    //
    // FilterVerb.filter(phrase);
    //
    // FilterNoun filterNoun = new FilterNoun(phrase);
    // filterNoun.filter();
    //
    // FilterPhrase.filter(phrase);
    //
    // FilterContext filterContext = new FilterContext(phrase, sentence);
    // filterContext.filter();
    //
    // long t2 = System.currentTimeMillis();
    // // System.err.println("[FilterPhrase] filtering finished, time: " + (t2
    // // - t1) + "ms");
    // }

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


//		Tree t = NLPParser.parseGrammaticalTree(string);
//		t.pennPrint();
//		PhraseInfo[] ps = PhraseExtractor.extractVerbPhrases(t);
//
//		for (PhraseInfo phrase : ps) {
//			System.out.println("==================");
//			phrase.getSyntaxTree();
//			filter(phrase, string);
//			if (phrase.getProofScore() <= 0)
//				continue;
//			System.out.println(phrase.getText());
//			System.out.println("proofScore: " + phrase.getProofScore());
////			System.out.println(phrase.getSyntaxTree());
////			System.out.println(phrase.getProofString());
////			System.out.println();
////			for (Proof prf : phrase.getProofs()) {
////				String str = prf.toString();
////				Proof p2 = Proof.of(str);
////				System.out.println(p2.toString());
////			}
//		}

        String str = "I have Java code that converts CSV to xlsx. It works fine with a small file size. Now I have a CSV file " +
                "with 2 lakh records(200,000) and on conversion I am getting an out of memory error. I tried changing the " +
                "workbook to 3X33FWorkbook and increasing the heap size and Java memory size to -Xms8G -Xmx10G. Even this " +
                "did not work. I tried it on a UNIX box. On searching, I got some code about using BigGridDemo. Can anyone " +
                "help me in customizing that to reading a .csv file and then using its logic to write to xlsx or any other solution";
        String[] sentences = {"I have Java code that converts CSV to xlsx.", "It works fine with a small file size.", "Now I have a CSV file " +
                "with 2 lakh records(200,000) and on conversion I am getting an out of memory error.", "I tried changing the " +
                "workbook to 3X33FWorkbook and increasing the heap size and Java memory size to -Xms8G -Xmx10G.", "Even this " +
                "did not work.", "I tried it on a UNIX box.", "On searching, I got some code about using BigGridDemo.", "Can anyone " +
                "help me in customizing that to reading a .csv file and then using its logic to write to xlsx or any other solution"};
//		Document doc = new Document(string);
//		List<Sentence> sentences = doc.sentences();
//        for (String sentence: sentences) {
            Tree t = NLPParser.parseGrammaticalTree(string);
//			t.pennPrint();
            PhraseInfo[] ps = PhraseExtractor.extractVerbPhrases(t);

            for (PhraseInfo phrase : ps) {
                System.out.println("==================");
                phrase.getSyntaxTree();
                filter(phrase, string);
//				if (phrase.getProofScore() < -5)
//					continue;
                System.out.println(phrase.getText());
                System.out.println("proofScore: " + phrase.getProofScore());
                System.out.println(phrase.getSyntaxTree());
                System.out.println(phrase.getProofString());
                System.out.println();
//				for (Proof prf : phrase.getProofs()) {
//					String str = prf.toString();
//					Proof p2 = Proof.of(str);
//					System.out.println(p2.toString());
//				}
            }
//        }
    }

}
