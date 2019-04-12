package cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.Atom;

public class TreeNode {

    public String treeType;
    public int treeOrder;

    public static final int ENTITY = 1;
    public static final int RELATION = 2;
    public static final int OPERATION = 3;
    public static final int HALF_OP = 4;

    public Atom atom;
    public TreeNode leftChild = null;
    public TreeNode rightChild = null;

    // corresponding span of the original text sequence, default -1.
    public int spanStart = -1;
    public int spanEnd = -1;

    public TreeNode(Atom a){
        atom = a;
        treeOrder = a.getOrder();
        treeType = a.getType();
    }

    public TreeNode(Atom a, TreeNode l, TreeNode r){
        atom = a;
        leftChild = l;
        rightChild = r;
        treeOrder = a.getOrder();
        treeType = a.getType();
    }

    @Override
    public String toString(){
        String left = "NULL";
        if (leftChild != null)
            left = leftChild.toString();
        String right = "NULL";
        if (leftChild != null)
            right = rightChild.toString();
        return String.format("(%s: %s %s)", atom.getName(), left, right);
    }
}
