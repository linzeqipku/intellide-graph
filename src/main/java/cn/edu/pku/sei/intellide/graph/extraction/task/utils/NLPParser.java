package cn.edu.pku.sei.intellide.graph.extraction.task.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.print.Doc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.StanfordParser;
import edu.stanford.nlp.trees.Tree;

public class NLPParser {
    public static final Logger	logger				= Logger.getLogger(NLPParser.class);
    public static final String	NLP_PARSER_LOG_FILE	= "nlp_parser_data_log.txt";
    public static Connection	conn;


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

        // logger.info(sentence);
        // long t1 = System.currentTimeMillis();
        Tree tree = StanfordParser.parseTree(sentence);
        // long t2 = System.currentTimeMillis();
        // System.err.println("parseTree "+(t2 - t1) + "ms");

        return tree;
    }

}

