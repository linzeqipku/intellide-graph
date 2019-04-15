package cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.Atom;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.AtomFactory;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.OperationAtom;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.Schema;
import org.apache.commons.lang3.tuple.Pair;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.List;

public class SemanticParser {

    public SemanticParser(){

    }

    public List<TreeNode> parse(List<Atom> sentence){
        int n = sentence.size();
        List<TreeNode>[][] table = new List[n][n];
        for (int i = 0; i < n; ++i){
            table[i][i] = new ArrayList<>(1);
            TreeNode node = new TreeNode(sentence.get(i));
            node.spanStart = i;
            node.spanEnd = i;
            table[i][i].add(node);
        }
        for (int len = 2; len <= n; ++len){
            for (int i = 0; i <= n - len; ++i){
                int j = i + len - 1;
                table[i][j] = new ArrayList<>(1);  // avoid null table entry
                for (int k = i; k < j; ++k){
                    List<TreeNode> span1 = table[i][k];
                    List<TreeNode> span2 = table[k+1][j];
                    if (span1.size() == 0 || span2.size() == 0)  // if p1 or p2 is empty, they cannot form new node
                        continue;
                    for (TreeNode subtree1: span1){
                        for (TreeNode subtree2: span2){
                            List<TreeNode> newTree = join(subtree1, subtree2);
                            if (newTree != null && newTree.size() > 0){
                                for (TreeNode node : newTree){
                                    node.spanStart = i;
                                    node.spanEnd = j;
                                }
                                table[i][j].addAll(newTree);
                            }
                        }
                    }
                }
            }
        }
        for (TreeNode root: table[0][n-1]){
            System.out.println(root);
        }
        return table[0][n-1];
    }

    public List<TreeNode> join(TreeNode tree1, TreeNode tree2){
        if (tree1.treeOrder == TreeNode.ENTITY){
            if (tree2.treeOrder == TreeNode.ENTITY)
                return join2Entities(tree1, tree2);
            else if (tree2.treeOrder == TreeNode.RELATION)
                return joinEntityRelation(tree1, tree2);
            else if (tree2.treeOrder == TreeNode.OPERATION)
                return joinOpEntity(tree2, tree1);
            else if (tree2.treeOrder == TreeNode.HALF_OP)
                return joinHalfOpEntity(tree2, tree1);
        }
        else if (tree1.treeOrder == TreeNode.RELATION){
            if (tree2.treeOrder == TreeNode.ENTITY)
                return joinEntityRelation(tree2, tree1);

        }
        else if (tree1.treeOrder == TreeNode.OPERATION){
            if (tree2.treeOrder == TreeNode.ENTITY)
                return joinOpEntity(tree1, tree2);
        }
        else if (tree1.treeOrder == TreeNode.HALF_OP){
            if (tree2.treeOrder == TreeNode.ENTITY)
                return joinHalfOpEntity(tree1, tree2);
        }

        return null;
    }

    public List<TreeNode> joinOpEntity(TreeNode optree, TreeNode entree){
        List<TreeNode> res = new ArrayList<>(1);
        if (entree.spanEnd <= optree.spanStart) {  // join with leftside first to avoid redundant tree
            TreeNode root = new TreeNode(optree.atom, entree, null);  // add to left child
            root.treeOrder = TreeNode.HALF_OP;
            root.treeType = entree.treeType;
            res.add(root);
        }
        return res;
    }

    public List<TreeNode> joinHalfOpEntity(TreeNode optree, TreeNode entree){
        List<TreeNode> res = new ArrayList<>(1);
        if (optree.treeType.equals(entree.treeType)) { // must and/or the same type node
            TreeNode root = new TreeNode(optree.atom, optree.leftChild, entree);
            root.treeOrder = 1;
            root.treeType = optree.treeType;
            res.add(root);
        }
        return res;
    }

    public List<TreeNode> join2Entities(TreeNode leftTree, TreeNode rightTree){
        List<TreeNode> res = new ArrayList<>(1);
        String leftType = leftTree.treeType;
        if (rightTree.treeType.equals(leftType)) {  // must have the same tree type (e.g. CLASS)
            Atom atom = AtomFactory.createOp(Atom.AND);
            TreeNode root = new TreeNode(atom, leftTree, rightTree);
            root.treeOrder = TreeNode.ENTITY;
            root.treeType = leftType;
            res.add(root);
        }
        else {
            String rel = oneHopConnectable(leftType, rightTree.treeType);
            if (rel != null) {
                Atom atom = AtomFactory.createOp(Atom.JOIN);
                TreeNode bridge = new TreeNode(atom);
                int index = rel.indexOf("-");
                if (index != -1){  // it is a inverse relation
                    rel = rel.substring(0, index);
                    Atom biAtom = AtomFactory.createBinary(rel);
                    bridge.leftChild = rightTree;
                    bridge.rightChild = new TreeNode(biAtom);
                }
                else {
                    Atom biAtom = AtomFactory.createBinary(rel);
                    bridge.leftChild = new TreeNode(biAtom);
                    bridge.rightChild = rightTree;
                }
                TreeNode root = new TreeNode(AtomFactory.createOp(Atom.AND), leftTree, bridge);
                res.add(root);
            }
        }
        return res;
    }

    public List<TreeNode> joinEntityRelation(TreeNode entTree, TreeNode relTree){
        List<TreeNode> res = new ArrayList<>(2);
        String enType = entTree.atom.getType();
        String relType = relTree.atom.getType();
        if (entTree.atom.isAbstractEntity()) // abstract entity cannot join with a relation
            return res;

        Pair<String, String> p = Schema.relations.get(relType);
        if (enType.equals(p.getLeft())){
            Atom atom = AtomFactory.createOp(Atom.JOIN);
            // entity is the left child
            TreeNode root = new TreeNode(atom, entTree, relTree);
            root.treeOrder = TreeNode.ENTITY;
            root.treeType = p.getRight();
            res.add(root);
        }
        if (enType.equals(p.getRight())){
            Atom atom = AtomFactory.createOp(Atom.JOIN);
            // entity is the right child
            TreeNode root = new TreeNode(atom, relTree, entTree);
            root.treeOrder = TreeNode.ENTITY;
            root.treeType = p.getLeft();
            res.add(root);
        }
        return res;
    }

    public String oneHopConnectable(String leftType, String rightType){
        for (String rel: Schema.relations.keySet()){
            Pair<String, String> p = Schema.relations.get(rel);
            if (p.getLeft().equals(leftType) && p.getRight().equals(rightType)){
                return rel;
            }
            else if (p.getRight().equals(leftType) && p.getLeft().equals(rightType)){
                return rel + "-1";
            }
        }
        return null;
    }
}
