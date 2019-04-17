package cn.edu.pku.sei.intellide.graph.qa.doc_search;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class DocSearchEvalInfo {

    @Getter @Setter private long nodeId;
    @Getter @Setter private String query;
    @Getter @Setter private int irRank, snowRank;
    @Getter @Setter private List<String> irResult=new ArrayList<>(), snowResult=new ArrayList<>();

}
