package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping;


import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;

import java.util.Comparator;

public class NLPMapping {
    public NLPToken belong;
    public double score = 0;
    public double rank = -1; // -1 代表是之后添加的结点

    public NLPMapping() {

    }

    public NLPMapping(NLPToken token, double similar) {
        this.belong = token;
        this.score = similar;
    }

}
