package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping;


import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.ir.LuceneSearchResult;

import java.util.ArrayList;
import java.util.List;

public class NLPNoticeMapping extends NLPMapping {
    public List<LuceneSearchResult> list;
    public NLPNoticeMapping(List<LuceneSearchResult>l) {
        list = list = new ArrayList<>();
        list.addAll(l);
    }
}
