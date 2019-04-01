package cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.Atom;

public class SemTree {

    public String treeType;

    public Atom root;

    public SemTree leftChild = null;
    public SemTree rightChild = null;

    public SemTree(SemTree l, SemTree r){
        leftChild = l;
        rightChild = r;
    }
}
