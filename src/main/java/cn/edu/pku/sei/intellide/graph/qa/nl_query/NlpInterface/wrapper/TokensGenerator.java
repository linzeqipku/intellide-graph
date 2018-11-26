package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config.StopWords;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPNoticeMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.ir.LuceneIndex;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.ir.LuceneSearchResult;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokensGenerator {

    public Query generator(String text, String languageIdentifier, GraphDatabaseService db) {
        /*AST 匹配到*/
        Query query = new Query();
        boolean flag = false;
        String word = "";
        String tmptext = "";
        Map<String, String> key = new HashMap();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '"' && flag) {
                String k = "TOKEN_" + key.size();
                key.put(k, word);
                tmptext += k;
                flag = !flag;
                continue;
            } else if (text.charAt(i) == '"' && !flag) {
                flag = !flag;
                continue;
            }
            if (flag) {
                word += text.charAt(i);
            } else {
                tmptext += text.charAt(i);
            }
        }
        text = tmptext;
        query.text = text;
        long offset = -1;
        List<NLPToken> set = StanfordParser.getInstance(languageIdentifier).runAllAnnotators(text);
        for (NLPToken token : set) {
            if (!StopWords.getInstance(languageIdentifier).isStopWord(token.text)) {
                token.roffset = token.offset;
                offset++;
                token.offset = offset;
                token.offsetVal = offset;
                if (token.text.startsWith("TOKEN_")) {
                    List<LuceneSearchResult> l = null;
                    try {
                        l = LuceneIndex.getInstance(db).query(key.get(token.text));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    token.mapping = new NLPNoticeMapping(l);
                    token.mappingList.add(token.mapping);
                }
                query.tokens.add(token);
            }
        }
        return query;
    }
}
