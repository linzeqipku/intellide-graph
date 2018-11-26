package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper.*;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.IOException;
import java.util.*;

@Slf4j
public class NLPInterpreter {

    private static Map<GraphDatabaseService, NLPInterpreter> instances = new HashMap<>();

    private GraphDatabaseService db;
    private String languageIdentifier = "english";
    private List<Query> queries = new ArrayList<>();
    private int offsetMax;

    private NLPInterpreter(GraphDatabaseService db, String languageIdentifier) {
        this.db = db;
        this.languageIdentifier = languageIdentifier;
    }

    public synchronized static NLPInterpreter getInstance(GraphDatabaseService db) throws IOException {
        NLPInterpreter instance = instances.get(db);
        if (instance == null) {
            throw new IOException("NLPInterpreter not found");
        }
        return instance;
    }

    public synchronized static NLPInterpreter createInstance(GraphDatabaseService db, String languageIdentifier){
        NLPInterpreter instance = instances.get(db);
        if (instance != null){
            return instance;
        }
        instance = new NLPInterpreter(db, languageIdentifier);
        instances.put(db, instance);
        return instance;
    }

    /**
     * 1. 使用TokensGenerator，对输入的句子进行切词、词性标注、命名实体识别等预处理；
     * 2. 使用TokenMapping，给每个token识别出相应的graph schema元素；
     * 3. 使用SchemaMapping和EdgeMappingSchema，给每个token识别出相应的graph entity元素；
     * 4. 使用LinkAllNodes，把识别出的元素关联形成一个推理子图；
     * @param plainText
     * @return 评分排名前20的推理子图
     */
    public synchronized List<String> pipeline(String plainText) {
        log.debug("开始解析问句的语义.");
        try {
            queries.clear();
            Query query = new TokensGenerator().generator(plainText, languageIdentifier, db);
            new TokenMapping().process(query, languageIdentifier, db);
            offsetMax = query.tokens.size();
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < offsetMax; i++) list.add(0);

            dfs(query, 0, list, 0);

            int tot = 0;
            List<Query> answers = new ArrayList<>();
            for (Query query1 : queries) {
                if (query1.nodes.size() == 0) continue;
                List<Query> listq = new ArrayList<>();
                listq.addAll(new LinkAllNodes(languageIdentifier).process(query1));
                for (Query q : listq) {
                    new Evaluator().evaluate(q);
                    if (q.score < -0.1) continue;
                    new InferenceLinksGenerator().generate(q);
                    String s = new CyphersGenerator().generate(q);
                    q.cypher = s;
                    if (!s.equals("")) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void dfs(Query query, int offset, List<Integer> list, int no) {
        if (no > 1) return;
        if (offset == offsetMax) {
            for (NLPToken token : query.tokens) {
                if (list.get((int) token.offset) < 0)
                    token.mapping = null;
                else
                    token.mapping = token.mappingList.get(list.get((int) token.offset));
            }
            Query newquery = query.copyOut();
            new SchemaMapping().mapping(newquery);
            List<Query> queries = new EdgeMappingSchema().process(newquery, db);
            this.queries.addAll(queries);
            return;
        }
        boolean flag = false;
        for (NLPToken token : query.tokens) {
            if (token.offset == offset) {
                flag = true;
                if (!(token.mapping instanceof NLPVertexSchemaMapping) ||
                        !((NLPVertexSchemaMapping) token.mapping).must) {
                    list.set(offset, -1);
                    if (token.nomapping) dfs(query, offset + 1, list, no);
                    else
                        dfs(query, offset + 1, list, no + 1);
                }
                for (int i = 0; i < token.mappingList.size(); i++) {
                    list.set(offset, i);
                    dfs(query, offset + 1, list, no);
                }
            }
        }

        if (!flag) dfs(query, offset + 1, list, no);
    }

}

