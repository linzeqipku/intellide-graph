package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CnToEnDirectory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class StopWords {

    public static Map<String, StopWords> instances = new HashMap<>();
    public Set<String> stopWords = new HashSet<>();

    private StopWords(String languageIdentifier) {
        List<String> lines = new ArrayList<>();
        try {
            InputStream in = CnToEnDirectory.class.getResourceAsStream("/nli/stopwords/" + languageIdentifier + ".txt");
            lines = IOUtils.readLines(in, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopWords.addAll(lines);
    }

    public synchronized static StopWords getInstance(String languageIdentifier) {

        StopWords instance = instances.get(languageIdentifier);
        if (instance != null) {
            return instance;
        }
        instance = new StopWords(languageIdentifier);
        instances.put(languageIdentifier, instance);
        return instance;
    }

    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

}
