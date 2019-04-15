package cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing;

import org.apache.commons.lang3.tuple.Pair;
import java.util.Collections;
import java.util.List;

public class Scorer {

    private SyntaxParser parser;
    private List<Pair<Integer, Integer>> synList;
    private int currentScore;

    public Scorer(SyntaxParser parser){
        this.parser = parser;
    }

    public int coverLength(int start, int end){
        for (Pair<Integer, Integer> p : synList){
            if (p.getLeft() <= start && p.getRight() >= end){
                return p.getRight() - p.getLeft();
            }
        }
        return 0;
    }

    private void eval(TreeNode root){
        if (root.spanStart != -1 && root.spanEnd != -1){
            currentScore -= coverLength(root.spanStart, root.spanEnd);
        }
        if (root.leftChild != null)
            eval(root.leftChild);
        if (root.rightChild != null)
            eval(root.rightChild);
    }

    public TreeNode bestTree(String text, List<TreeNode> treeList){
        if (treeList == null || treeList.size() == 0) {
            System.out.println("empty tree list.");
            return null;
        }
        synList = parser.parse(text);
        currentScore = 0;
        Collections.sort(synList, (p1, p2)->Integer.compare(p1.getRight()-p1.getLeft(), p2.getRight()-p2.getLeft()));
        TreeNode node = null;
        int maxScore = Integer.MIN_VALUE;
        for (TreeNode root : treeList){
            currentScore = 0;
            eval(root);
            if (currentScore >= maxScore){
                maxScore = currentScore;
                node = root;
            }
        }
        return node;
    } // Andreas Zeller, homepage presentation
}
