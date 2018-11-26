package cn.edu.pku.sei.intellide.graph.extraction.task.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    public static String getPrecedingContext(String phrase, String context) {
        // System.out.println(context + "\t" + phrase);
        if (phrase == null || context == null)
            return null;
        int i = context.indexOf(phrase);
        // System.out.println(i);
        if (i == -1)
            return null;
        String preceding = context.substring(0, i);
        // System.out.println(preceding);
        return preceding.trim();
    }

    public static void main(String[] args) {
        System.out.println(ignorePunctuation("address change in neo4j codebase as Neo4jTemplate.beginTx)  ，db.index.fornodes (user"));
        System.out.println(ignorePunctuation("Joe's calculation，serve formatted data to d3.js visualisation library"));
        List<String> li = getWordList("I have  .. 990.s a good");
        for (String s : li){
            System.out.println(s);
        }

    }

    public static String ignorePunctuation(String str) {
        Matcher mat;
        mat = Pattern.compile("-[lrLR][rcsRCS][Bb]-").matcher(str);
        str = mat.replaceAll("");
        mat = Pattern.compile("( [^0-9a-zA-Z ]+ )|,|，").matcher(str);
        str = mat.replaceAll(" ");
        mat = Pattern.compile("([^0-9a-zA-Z ]+ )|( [^0-9a-zA-Z ]+)").matcher(str);
        str = mat.replaceAll(" ");
        mat = Pattern.compile("[ ]+").matcher(str);
        str = mat.replaceAll(" ");
        return str;
    }

    public static List<String> getWordList(String str){
        String[] s_arr = str.split("[ ]+");
        List<String> ret = new ArrayList<String>();
        for (String s : s_arr){
            if (s.toLowerCase().startsWith("http://") && s.length() > 14)
                continue;
            ret.add(s.trim());
        }
        return ret;
    }

}

