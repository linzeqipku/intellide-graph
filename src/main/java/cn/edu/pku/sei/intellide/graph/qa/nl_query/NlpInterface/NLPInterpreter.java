package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper.*;

import java.util.*;

public class NLPInterpreter {

    public static Map<String, NLPInterpreter> instances = new HashMap<>();

    private String languageIdentifier = "english";
    private String cypherStr = "";
    private List<Query> queries = new ArrayList<>();
    private int offsetMax;

    public synchronized static NLPInterpreter getInstance(String languageIdentifier){
        NLPInterpreter instance = instances.get(languageIdentifier);
        if (instance != null){
            return instance;
        }
        instance = new NLPInterpreter(languageIdentifier);
        instances.put(languageIdentifier, instance);
        return instance;
    }

    private NLPInterpreter(String languageIdentifier){
        this.languageIdentifier = languageIdentifier;
    }

    public synchronized List<String> pipeline(String plainText){
        try {
            queries.clear();
            Query query = generatorTokens(plainText);
            mapTokensToNodeAndRelation(query);
            offsetMax = query.tokens.size();
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < offsetMax; i++) list.add(0);
            cypherStr = "";

            DFS(query, 0, list, 0);
            int tot = 0;
            List<Query> answers = new ArrayList<>();
            for (Query query1 : queries) {
                if (query1.nodes.size() == 0) continue;
                List<Query> listq = new ArrayList<>();
                listq.addAll(LinkAllNodes.getInstance(languageIdentifier).process(query1));
                for (Query q : listq) {
                    Evaluator.evaluate(q);
                    if (q.score < -0.1) continue;
                    generatorInferenceLinks(q);
                    String s = generatorCyphers(q);
                    q.cypher = s;
                    if (!s.equals("")) {
                        cypherStr += s + "</br>";
                        q.rank = tot;
                        tot++;
                        answers.add(q);
                    }
                }
            }
            answers.sort(Comparator.comparing(p -> p.score));
            Set<Query> anstmp = new HashSet<>();
            for (Query q : answers) {
                boolean flag = true;
                for (Query qq : anstmp) {
                    if (q.cypher.equals(qq.cypher)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) anstmp.add(q);
            }
            answers.clear();
            answers.addAll(anstmp);

            answers.sort(Comparator.comparing(p -> p.score));
            if (answers.size() > 20) answers = answers.subList(0, 20);
            List<String> ans = new ArrayList<>();
            for (Query q : answers) {
                ans.add(q.cypher);
            }
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void DFS(Query query, int offset, List<Integer> list, int no){
        if (no > 1) return;
        if (offset == offsetMax){
            for (NLPToken token : query.tokens){
                if (list.get((int)token.offset) < 0) token.mapping = null; else
                token.mapping = token.mappingList.get(list.get((int)token.offset));
            }
            Query newquery = query.copyOut();
            mapToSchema(newquery);
            List<Query> querys = EdgeMappingSchema.process(newquery);
            queries.addAll(querys);
            return;
        }
        boolean flag = false;
        for (NLPToken token : query.tokens){
            if (token.offset == offset){
                flag = true;
                if (!(token.mapping instanceof NLPVertexSchemaMapping) ||
                        !((NLPVertexSchemaMapping)token.mapping).must) {
                    list.set(offset, -1);
                    if (token.nomapping)DFS(query, offset + 1, list, no);else
                    DFS(query, offset + 1, list, no + 1);
                }
                for (int i = 0; i < token.mappingList.size(); i++){
                    list.set(offset,i);
                    DFS(query,offset+1, list, no);
                }
            }
        }

        if (!flag) DFS(query,offset+1, list, no);
    }

    private Query generatorTokens(String plainText){
        return TokensGenerator.generator(plainText, languageIdentifier);
    }

    private void mapTokensToNodeAndRelation(Query query){
        TokenMapping.process(query, languageIdentifier);
    }

    private void generatorInferenceLinks(Query query){
        InferenceLinksGenerator.generate(query);
    }

    private void mapToSchema(Query query){
        SchemaMapping.mapping(query);
    }

    private String generatorCyphers(Query query){
        return CyphersGenerator.generate(query);
    }

}

