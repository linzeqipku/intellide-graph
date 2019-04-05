package cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.Atom;

public class TreeNode {

    public String treeType;

    public Atom val;

    public TreeNode leftChild = null;
    public TreeNode rightChild = null;

    public TreeNode(Atom a, TreeNode l, TreeNode r){
        val = a;
        leftChild = l;
        rightChild = r;
    }
}
