package cn.edu.pku.sei.intellide.graph.extraction.task.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class TreeUtils {
    private static Tree testTree;

    public static void main(String[] args) {
        String string = // "He got a score of 3.7 in the exams.";
                "He wants to give us an idea of how things are scored.";
        // "I could never be playing around with natural language parse trees, he wants to handle
        // empty cells in excel sheets.";
        // "He wants to put that into this box, I hope I can move these from there to his room.";
        // ", he'll be a dancer, and he wouldn't said that his father might be a farmer, his family
        // will never be Chinese.";

//        SentenceInfo sentence = new SentenceInfo(string);
//        SentenceParser.parseGrammaticalTree(sentence);
//        sentence.getGrammaticalTree().pennPrint();
//        PhraseExtractor.extractVerbPhrases(sentence);
//
//        Tree copyTree = sentence.getGrammaticalTree().deepCopy();
//        System.out.println(copyTree.equals(sentence.getGrammaticalTree()));
//        System.out.println(copyTree);

        // List<PhraseInfo> phrases = sentence.getPhrases();
        // for (int i = phrases.size() - 1; i >= 0; i--) {
        // System.out.println("**********************");
        // PhraseInfo phrase = phrases.get(i);
        // System.out.println(phrase.getContent());
        // Tree tree = phrase.getTree();
        // depthFirstTraversal(tree);
        // // trimEmptyLeaf(tree);
        // }

        testTree = Tree.valueOf(
                "(ROOT (S (NP (PRP He)) (VP (VBZ wants) (S (VP (TO to) (VP (VB give) (NP (PRP us)) NP)))) (. .)))");
        depthFirstTraversal(testTree);
        System.out.println("==============");
        testTree.pennPrint();
        depthFirstTraversal(testTree);

    }

    public static String getLeafString(Tree tree) {
        if (tree.isPreTerminal()) {
            // 树的下一层节点是终结符(leaf)，也就是说，树是pos tag，下一层是一个单词
            Tree leaf = tree.getChild(0);
            if (leaf != null && leaf.isLeaf()) {
                // 叶子结点
                return leaf.value();
            }
        }
        return null;
    }

    public static void depthFirstTraversal(Tree tree) {
        if (tree == null)
            return;
        System.out.println();
        tree.pennPrint();
        System.out.println("isEmpty:" + tree.isEmpty());
        System.out.println("isLeaf:" + tree.isLeaf());
        System.out.println("isPhrasal:" + tree.isPhrasal());
        System.out.println("isPrePreTerminal:" + tree.isPrePreTerminal());
        System.out.println("isPreTerminal:" + tree.isPreTerminal());
        System.out.println("isUnaryRewrite:" + tree.isUnaryRewrite());

        if (tree.isLeaf())
            return;
        Tree[] children = tree.children();
        for (int i = 0; i < children.length; i++) {
            if (children[i].label().toString().equals("PRP")) {
                removeSubTree(children[i], tree);
            }
            else
                depthFirstTraversal(children[i]);
        }
        if (tree.isLeaf()) {
            System.out.println("LEAF！" + tree);
            removeSubTree(tree, testTree);
        }
    }

    public static void removeSubTree(Tree child, Tree ancestor) {
        if (child == null || ancestor == null)
            return;
        try {
            Tree parent = child.parent(ancestor);
            if (parent != null) {
                int index = parent.objectIndexOf(child);
                if (index >= 0)
                    parent.removeChild(index);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 助词短语
    public static boolean isParticle(Tree tree) {
        try {
            return tree.label().toString().equals("PRT");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    // 助词单词
    public static boolean isParticleWord(Tree tree) {
        try {
            return tree.label().toString().equals("RP");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    // 形容词
    public static boolean isAdjective(Tree tree) {
        try {
            return tree.label().toString().startsWith("JJ");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    // 连词
    public static boolean isCC(Tree tree) {
        try {
            return tree.label().toString().equals("CC");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    // 介词或者 to
    public static boolean isPreposition(Tree tree) {
        try {
            return tree.label().toString().equals("IN") || tree.label().toString().equals("TO");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isVB(Tree tree) {
        try {
            return tree.label().toString().startsWith("VB");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isVP(Tree tree) {
        try {
            return tree.label().toString().equals("VP");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isNP(Tree tree) {
        try {
            return tree.label().toString().equals("NP");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isPP(Tree tree) {
        try {
            return tree.label().toString().equals("PP");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isNN(Tree tree) {
        try {
            return tree.label().toString().startsWith("NN");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    // 冠词 determiner
    public static boolean isDT(Tree tree) {
        try {
            return tree.label().toString().equals("DT") || tree.label().toString().equals("PDT");
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    @Deprecated
    public static Tree getParent(Tree child, Tree ancestor) {
        Tree officialResult = child.parent(ancestor);
        if (officialResult != null)
            return officialResult;

        if (child == null || ancestor == null)
            return null;

        Tree[] children = ancestor.children();
        if (children == null)
            return null;
        for (int i = 0; i < children.length; i++) {
            if (children[i].equals(child))
                return ancestor;
            else if (children[i].isLeaf())
                return null;
            else {
                Tree returnedTree = getParent(child, children[i]);
                if (returnedTree != null)
                    return returnedTree;
            }
        }
        return null;
    }

    @Deprecated
    public static int indexOf(Tree child, Tree parent) {
        int officialResult = parent.objectIndexOf(child);
        if (officialResult >= 0)
            return officialResult;

        if (child == null || parent == null)
            return -1;

        Tree[] children = parent.children();
        for (int i = 0; i < children.length; i++) {
            if (children[i].equals(child))
                return i;
        }
        return -1;
    }

    public static String interpretTreeToString(Tree tree) {
        // String str = Sentence.listToString(tree.yield());
        List<Tree> words = tree.getLeaves();
        String str = "";
        int tot = 0;
        int tot1 = 0;
        boolean flag = false;
        for (Tree word : words) {
            String s = word.toString();
            if (s.equals("''") || s.equals("``")) {
                tot ^= 1;
                if (tot == 1) {
                    flag = true;
                    str = str + " ";
                    str = str + "\"";
                    continue;
                }
                else {
                    str = str + "\"";
                }
            }
            else if (s.equals("'") || s.equals("`")) {
                tot1 ^= 1;
                if (tot1 == 1) {
                    flag = true;
                    str = str + " ";
                    str = str + "'";
                    continue;
                }
                else {
                    str = str + "'";
                }
            }
            else if (s.toUpperCase().equals("-LRB-")) {
                flag = true;
                str = str + " ";
                str = str + word;
                continue;
            }
            else if (s.toUpperCase().equals("-RRB-")) {
                str = str + word;
            }
            else if (s.toUpperCase().equals("-LCB-")) {
                flag = true;
                str = str + " ";
                str = str + word;
                continue;
            }
            else if (s.toUpperCase().equals("-RCB-")) {
                str = str + word;
            }
            else if (s.toUpperCase().equals("-LSB-")) {
                flag = true;
                str = str + " ";
                str = str + word;
                continue;
            }
            else if (s.toUpperCase().equals("-RSB-")) {
                str = str + word;
            }
            else if (s.startsWith(".")) {
                str = str + word;
            }
            else if (s.equals(",")) {
                str = str + word;
            }
            else if (s.equals(":")) {
                str = str + word;
            }
            else if (s.equals(";")) {
                str = str + word;
            }
            else if (s.equals("?")) {
                str = str + word;
            }
            else if (s.equals("!")) {
                str = str + word;
            }
            else if (s.equals("n't")) {
                str = str + word;
            }
            else if (s.equals("'m")) {
                str = str + word;
            }
            else if (s.equals("'s")) {
                str = str + word;
            }
            else if (s.equals("'re")) {
                str = str + word;
            }
            else if (s.equals("'ve")) {
                str = str + word;
            }
            else if (flag) {
                str = str + word;
            }
            else {
                str = str + " ";
                str = str + word;
            }
            flag = false;
        }
        str = str.replace("-LRB-", "(");
        str = str.replace("-RRB-", ")");
        str = str.replace("-LCB-", "{");
        str = str.replace("-RCB-", "}");
        str = str.replace("-LSB-", "[");
        str = str.replace("-RSB-", "]");
        str = str.replace("-lrb-", "(");
        str = str.replace("-rrb-", ")");
        str = str.replace("-lcb-", "{");
        str = str.replace("-rcb-", "}");
        str = str.replace("-lsb-", "[");
        str = str.replace("-rsb-", "]");
        return str.trim();
    }

}
