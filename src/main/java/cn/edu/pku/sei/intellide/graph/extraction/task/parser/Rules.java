package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.text.WordUtils;

public class Rules {
    public static final int			THRESHOLD				= 0;

    private static final String		QA_PHRASES_FILE			= "qa_phrases.dat";
    private static final String		QA_NOUNS_FILE			= "qa_nouns.dat";
    private static final String		QA_VERBS_FILE			= "qa_verbs.dat";
    private static final String		STOP_NOUNS_FILE			= "stop_nouns.dat";
    private static final String		STOP_PHRASES_FILE		= "stop_phrases.dat";
    private static final String		STOP_VERBS_FILE			= "stop_verbs.dat";
    private static final String		UNLIKE_NOUNS_FILE		= "unlike_nouns.dat";
    private static final String		UNLIKE_VERBS_FILE		= "unlike_verbs.dat";
    private static final String		DETERMINERS_FILE		= "valuable_determiners.dat";

    public static String[]			qa_phrases;
    public static String[]			qa_nouns;
    public static String[]			qa_verbs;
    public static String[]			stop_nouns;
    public static String[]			stop_phrases;
    public static String[]			stop_verbs;
    public static String[]			unlike_nouns;
    public static String[]			unlike_verbs;
    public static String[]			valuable_determiners;

    public static final String[]	BE_VERBS				= { "be", "am", "is", "are", "was", "were",
            "being", "been", "'m", "'s", "'re" };
    public static final String[]	MODAL_VERBS				= { "can", "could", "dare", "may", "might",
            "must", "ought", "shall", "should", "will", "would", "'d", "'ll" };
    public static String[]			HAVE_VERBS				= { "have", "has", "had", "having", "'ve", "'s",
            "'d" };
    public static final String[]	NEGATIVE_WORDS			= { "not", "n't", "never" };
    public static final String[]	DETERMINERS				= { "this", "that", "these", "those" };
    public static final String[]	VALID_PRONOUNS			= { "it", "them" };
    public static final String[]	VALID_TWOLETTER_WORDS	= { "an", "as", "at", "by", "db", "in", "ip",
            "of", "on", "or", "to" };

    public static final String		NAME_NP_NN				= "np-nn";
    public static final String		NAME_NP_NP_NN			= "np-np-nn";
    public static final String		NAME_PP_NN				= "pp-nn";

    public static final String		NP_NN_PATTERN			= " ( NP < /NN.*/" + " = " + NAME_NP_NN
            + " | !< /NN.*/ < (NP < /NN.*/" + " = " + NAME_NP_NP_NN + ") ) ";								// NP最多下扩两层
    public static final String		PP_PATTERN				= " ( PP < /NN.*/" + " = " + NAME_PP_NN
            + " | !< /NN.*/ < " + NP_NN_PATTERN + " | !< /NN.*/ !< NP < ( PP < /NN.*/" + " = " + NAME_PP_NN
            + " | !< /NN.*/ < " + NP_NN_PATTERN + ") ) ";
    public static final String		NP_NN_PP_PATTERN		= " ( NP < (PP < NP) ) ";

    private static final String		DT_PATTERN				= " ( DT <"
            + Rules.ruleWordsConjuctionForTregex(Rules.DETERMINERS) + " ) ";
    private static final String		PRP_PATTERN				= " ( PRP < "
            + Rules.ruleWordsConjuctionForTregex(Rules.VALID_PRONOUNS) + " ) ";
    public static final String		NP_PRP_PATTERN			= " ( NP <: " + PRP_PATTERN
            + " | !</NN.*/ < ( NP <: " + PRP_PATTERN + " ) )";
    public static final String		NP_DT_PATTERN			= " ( NP <: " + DT_PATTERN
            + " | !</NN.*/ < ( NP <: " + DT_PATTERN + " ) )";

    static {
        loadWordList();
    }

    public static void loadWordList() {
//		File dir = new File(Config.getKeywordsDictionaryDir() + File.separator);
        File dir = new File("src/main/resources/rules" + File.separator);
        BufferedReader reader;
        List<String> tempList;
        // stop-verbs
        try {
            reader = new BufferedReader(new FileReader(new File(dir, STOP_VERBS_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    // 首字母大写
                    tempList.add(WordUtils.capitalize(line));
                    // 全部大写
                    tempList.add(line.toUpperCase());
                }
            }
            reader.close();
            stop_verbs = tempList.toArray(new String[tempList.size()]);
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // unlike-verbs
        try {
            reader = new BufferedReader(new FileReader(new File(dir, UNLIKE_VERBS_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    // 首字母大写
                    tempList.add(WordUtils.capitalize(line));
                    // 全部大写
                    tempList.add(line.toUpperCase());
                }
            }
            reader.close();
            unlike_verbs = tempList.toArray(new String[tempList.size()]);
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // stop-nouns
        try {
            reader = new BufferedReader(new FileReader(new File(dir, STOP_NOUNS_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    // 首字母大写
                    tempList.add(WordUtils.capitalize(line));
                    // 全部大写
                    tempList.add(line.toUpperCase());
                }
            }
            reader.close();
            stop_nouns = tempList.toArray(new String[tempList.size()]);
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // unlike-nouns
        try {
            reader = new BufferedReader(new FileReader(new File(dir, UNLIKE_NOUNS_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    // 首字母大写
                    tempList.add(WordUtils.capitalize(line));
                    // 全部大写
                    tempList.add(line.toUpperCase());
                }
            }
            unlike_nouns = tempList.toArray(new String[tempList.size()]);
            reader.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // qa-verbs
        try {
            reader = new BufferedReader(new FileReader(new File(dir, QA_VERBS_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    tempList.add(WordUtils.capitalize(line));
                    tempList.add(line.toUpperCase());
                }
            }
            qa_verbs = tempList.toArray(new String[tempList.size()]);
            reader.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // qa-nouns
        try {
            reader = new BufferedReader(new FileReader(new File(dir, QA_NOUNS_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    tempList.add(WordUtils.capitalize(line));
                    tempList.add(line.toUpperCase());
                }
            }
            qa_nouns = tempList.toArray(new String[tempList.size()]);
            reader.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // context-phrases
        try {
            reader = new BufferedReader(new FileReader(new File(dir, QA_PHRASES_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    tempList.add(capitalizePhrase(line));
                    tempList.add(WordUtils.capitalize(line));
                    tempList.add(line.toUpperCase());
                }
            }
            qa_phrases = tempList.toArray(new String[tempList.size()]);
            reader.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // stop-phrases
        try {
            reader = new BufferedReader(new FileReader(new File(dir, STOP_PHRASES_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    tempList.add(capitalizePhrase(line));
                    tempList.add(WordUtils.capitalize(line));
                    tempList.add(line.toUpperCase());
                }
            }
            stop_phrases = tempList.toArray(new String[tempList.size()]);
            reader.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // valuable determiners
        try {
            reader = new BufferedReader(new FileReader(new File(dir, DETERMINERS_FILE)));
            tempList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    tempList.add(line);
                    tempList.add(WordUtils.capitalize(line));
                    tempList.add(line.toUpperCase());
                }
            }
            valuable_determiners = tempList.toArray(new String[tempList.size()]);
            reader.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String capitalizePhrase(String phrase) {
        if (phrase != null && phrase.length() >= 1) {
            char c = phrase.charAt(0);
            if (CharUtils.isAsciiAlphaLower(c))
                c = (char) (c + 'A' - 'a');
            return c + phrase.substring(1);
        }
        return phrase;
    }

    public static void main(String[] args) {
        for (String string : stop_verbs) {
            System.out.println(string);
        }
        System.out.println();
        for (String string : stop_nouns) {
            System.out.println(string);
        }
        System.out.println();
        for (String string : qa_phrases) {
            System.out.println(string);
        }
        System.out.println();
        for (String string : stop_phrases) {
            System.out.println(string);
        }
    }

    public static String ruleWordsConjuctionForTregex(String words[]) {
        StringBuilder conj = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String keyword = words[i];
            if (i > 0)
                conj.append("|");
            conj.append(keyword);
        }
        return conj.toString();
    }

}

