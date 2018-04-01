package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping;


import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;

import java.util.Comparator;

public class NLPMapping {
    public NLPToken belong;
    public double score = 0;
    public double rank = -1; // -1 代表是之后添加的结点
    public NLPMapping(){

    }
    public NLPMapping(NLPToken token, double similar){
        this.belong = token;
        this.score = similar;
    }
    public static class ComparatorUser implements Comparator {

        public int compare(Object o1, Object o2) {
            NLPMapping user0 = (NLPMapping) o1;
            NLPMapping user1 = (NLPMapping) o2;
            if (Math.abs(user0.score - user1.score) < 0.01){
                if (user0 instanceof NLPVertexSchemaMapping && user1 instanceof NLPVertexSchemaMapping){
                    return ((NLPVertexSchemaMapping) user0).vertexType.name.compareTo(((NLPVertexSchemaMapping) user1).vertexType.name);
                }
            }
            return ((Double)(user0.score*-1)).compareTo(((Double)(user1.score*-1)));
        }
    }
}
