package cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.Atom;

import java.util.ArrayList;
import java.util.List;

public class SemanticParser {
    public SemanticParser(){

    }
    public String parse(List<Atom> sentence){
        String expr = "";
        int n = sentence.size();
        List<TreeNode>[][] table = new List[n][n];
        for (int i = 0; i < n; ++i){
            table[i][i] = new ArrayList<>(1);
            table[i][i].add(new TreeNode(sentence.get(i), null, null));
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
                            TreeNode newTree = join(subtree1, subtree2);
                            if (newTree != null){
                                table[i][j].add(newTree);
                            }
                        }
                    }
                }
            }
        }
        return expr;
    }
    public TreeNode join(TreeNode tree1, TreeNode tree2){
        TreeNode newTree = null;
        Atom atom1 = tree1.val;
        Atom atom2 = tree2.val;
        if (atom1.isBinary() && atom2.isBinary()){

        }
        return newTree;
    }
}
