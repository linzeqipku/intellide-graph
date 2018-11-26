package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.TreeUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class PhraseExtractor {

    public static void main(String[] args) {
//		String str = "Pass the stemmed tree to clusterer, finding out centralized tasks.";
//		String str = "How to convert a huge .csv file to excel using POI";
        String str = "I'm trying to develop a complex report, and I need to set up the print areas for the excel file.";
        Tree tree = NLPParser.parseGrammaticalTree(str);
        PhraseInfo[] verbPhrases = extractVerbPhrases(tree);
        for (PhraseInfo phraseInfo : verbPhrases) {
            System.out.println();
            System.out.println(phraseInfo.getText());
            System.out.println(phraseInfo.getSyntaxTree());
            System.out.println(Tree.valueOf(phraseInfo.getSyntaxTree()));
            System.out.println(phraseInfo.getProofs());
        }
    }

    public static PhraseInfo[] extractVerbPhrases(Tree sentenceTree) {
        if (sentenceTree == null)
            return null;

        List<PhraseInfo> phraseList = new ArrayList<>();

        // 提取VP短语最关键的一句，定义提取的正则式
        String vpPattern = "VP < /VB.*/";

        TregexPattern tregexPattern = TregexPattern.compile(vpPattern);
        TregexMatcher matcher = tregexPattern.matcher(sentenceTree);

        HashSet<Tree> treeSet = new HashSet<Tree>();
        // 获取下一个不同的match节点
        while (matcher.findNextMatchingNode()) {
            // match到的新的子树
            Tree matchedTree = matcher.getMatch();
            if (treeSet.contains(matchedTree)) {
                // System.err.println("Repeated tree！！");
                // System.err.println(sentence.getContent());
                // System.err.println(matchedTree);
                continue;
            }
            treeSet.add(matchedTree);

            // 新建一个phrase对象
            PhraseInfo phrase = new PhraseInfo();
            phrase.setPhraseType(PhraseInfo.PHRASE_TYPE_VP);
            phrase.setText(TreeUtils.interpretTreeToString(matchedTree));
            phrase.setSyntaxTree(matchedTree.toString());

            phrase.addProof(new Proof(ProofType.INIT_EXTRACTION_VP));

            phraseList.add(phrase);
        }

        return phraseList.toArray(new PhraseInfo[phraseList.size()]);
    }

}

