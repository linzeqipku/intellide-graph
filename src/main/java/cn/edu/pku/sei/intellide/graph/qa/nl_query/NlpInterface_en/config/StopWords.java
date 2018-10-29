package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.config;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CnToEnDirectory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
            InputStream in = CnToEnDirectory.class.getResourceAsStream("/stopwords_lcy.txt");
            lines= IOUtils.readLines(in, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        englishStopWords.addAll(lines);
    }
    public static boolean isStopWord(String word){
        return englishStopWords.contains(word);
    }
}
