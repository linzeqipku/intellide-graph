package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.ir.LuceneIndex;
import org.apache.commons.io.FileUtils;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopWords {
    private static EnglishStemmer stemmer = new EnglishStemmer();
    public static Set<String> englishStopWords = new HashSet<>();
    static {
        List<String> lines=new ArrayList<>();
        try {
            lines= FileUtils.readLines(new File(LuceneIndex.dataDirPath+"\\stopwords_chinese.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        englishStopWords.addAll(lines);
    }
    public static boolean isStopWord(String word){
        return englishStopWords.contains(word);
    }
}
