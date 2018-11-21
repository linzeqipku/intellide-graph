package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.PhraseInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.TreeUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class PhraseExtractor {
    //public static final Logger logger = Logger.getLogger(PhraseExtractor.class);

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

    // public static List<VerbalPhraseInfo> extractVerbPhrases(SentenceInfo
    // sentence) {
    // if (sentence == null || sentence.getGrammaticalTree() == null)
    // return null;
    //
    // // 拿到句子的句法树
    // Tree tree = sentence.getGrammaticalTree();
    //
    // List<VerbalPhraseInfo> phraseList = new ArrayList<>();
    //
    // // 提取VP短语最关键的一句，定义提取的正则式
    // String vpPattern = "VP < /VB.*/";
    //
    // TregexPattern tregexPattern = TregexPattern.compile(vpPattern);
    // TregexMatcher matcher = tregexPattern.matcher(tree);
    //
    // HashSet<Tree> treeSet = new HashSet<Tree>();
    // // 获取下一个不同的match节点
    // while (matcher.findNextMatchingNode()) {
    // // match到的新的子树
    // Tree matchedTree = matcher.getMatch();
    // if (treeSet.contains(matchedTree)) {
    // // System.err.println("Repeated tree！！");
    // // System.err.println(sentence.getContent());
    // // System.err.println(matchedTree);
    // continue;
    // }
    // treeSet.add(matchedTree);
    //
    // // 新建一个phrase对象
    // VerbalPhraseInfo phrase = new VerbalPhraseInfo();
    // phrase.setTree(matchedTree);
    // phrase.setContent(TreeUtils.interpretTreeToString(matchedTree));
    //
    // Proof proof = new Proof(ProofType.INIT_EXTRACTION_VP);
    // phrase.getProofs().add(proof);
    //
    // phraseList.add(phrase);
    // }
    //
    // sentence.getPhrases().addAll(phraseList);
    // return phraseList;
    // }

    public static PhraseInfo[] extractNounPhrases(Tree sentenceTree) {
        if (sentenceTree == null)
            return null;

        List<PhraseInfo> phraseList = new ArrayList<PhraseInfo>();

        // 提取NP短语最关键的一句，定义提取的正则式
        String npPattern = "NP";

        TregexPattern tregexPattern = TregexPattern.compile(npPattern);
        TregexMatcher matcher = tregexPattern.matcher(sentenceTree);

        HashSet<Tree> treeSet = new HashSet<Tree>();
        // 获取下一个不同的match节点
        while (matcher.findNextMatchingNode()) {
            // match到的新的子树
            Tree matchedTree = matcher.getMatch();
            if (treeSet.contains(matchedTree)) {
                // System.err.println("Repeated tree！！！！");
                // System.err.println(sentence.getContent());
                // matchedTree.pennPrint();
                continue;
            }
            treeSet.add(matchedTree);

            // 新建一个phrase对象
            PhraseInfo phrase = new PhraseInfo();
            phrase.setSyntaxTree(matchedTree.toString());
            phrase.setPhraseType(PhraseInfo.PHRASE_TYPE_NP);
            phrase.setText(TreeUtils.interpretTreeToString(matchedTree));

            Proof proof = new Proof(ProofType.INIT_EXTRACTION_NP);
            phrase.getProofs().add(proof);

            phraseList.add(phrase);
        }

        return phraseList.toArray(new PhraseInfo[phraseList.size()]);
    }

    // public static List<PhraseInfo> extractNounPhrases(SentenceInfo sentence)
    // {
    // if (sentence == null || sentence.getGrammaticalTree() == null)
    // return null;
    //
    // // 拿到句子的句法树
    // Tree tree = sentence.getGrammaticalTree();
    //
    // List<PhraseInfo> phraseList = new ArrayList<PhraseInfo>();
    //
    // // 提取NP短语最关键的一句，定义提取的正则式
    // String npPattern = "NP";
    //
    // TregexPattern tregexPattern = TregexPattern.compile(npPattern);
    // TregexMatcher matcher = tregexPattern.matcher(tree);
    //
    // HashSet<Tree> treeSet = new HashSet<Tree>();
    // // 获取下一个不同的match节点
    // while (matcher.findNextMatchingNode()) {
    // // match到的新的子树
    // Tree matchedTree = matcher.getMatch();
    // if (treeSet.contains(matchedTree)) {
    // // System.err.println("Repeated tree！！！！");
    // // System.err.println(sentence.getContent());
    // // matchedTree.pennPrint();
    // continue;
    // }
    // treeSet.add(matchedTree);
    //
    // // 新建一个phrase对象
    // PhraseInfo phrase = new PhraseInfo();
    // phrase.setTree(matchedTree);
    // phrase.setContent(TreeUtils.interpretTreeToString(matchedTree));
    //
    // Proof proof = new Proof(ProofType.INIT_EXTRACTION_NP);
    // phrase.getProofs().add(proof);
    //
    // phraseList.add(phrase);
    // }
    //
    // sentence.getPhrases().addAll(phraseList);
    // return phraseList;
    // }

    // public static List<PhraseInfo> extractNounPhrasesFromVP(PhraseInfo
    // verbalPhrase) {
    // if (verbalPhrase == null || verbalPhrase.getStemmedTree() == null)
    // return null;
    //
    // // 拿到句子的句法树
    // Tree tree = verbalPhrase.getStemmedTree();
    //
    // List<PhraseInfo> phraseList = new ArrayList<PhraseInfo>();
    //
    // // 提取NP短语最关键的一句，定义提取的正则式
    // String npPattern = "NP";
    //
    // TregexPattern tregexPattern = TregexPattern.compile(npPattern);
    // TregexMatcher matcher = tregexPattern.matcher(tree);
    //
    // HashSet<Tree> treeSet = new HashSet<Tree>();
    // // 获取下一个不同的match节点
    // while (matcher.findNextMatchingNode()) {
    // // match到的新的子树
    // Tree matchedTree = matcher.getMatch();
    // if (treeSet.contains(matchedTree)) {
    // // System.err.println("Repeated tree！！！！");
    // // System.err.println(sentence.getContent());
    // // matchedTree.pennPrint();
    // continue;
    // }
    // treeSet.add(matchedTree);
    //
    // // 新建一个phrase对象
    // PhraseInfo phrase = new PhraseInfo();
    // phrase.setTree(matchedTree);
    // phrase.setContent(TreeUtils.interpretTreeToString(matchedTree));
    //
    // Proof proof = new Proof(ProofType.INIT_EXTRACTION_NP);
    // phrase.getProofs().add(proof);
    //
    // phraseList.add(phrase);
    // }
    //
    // for (PhraseInfo phraseInfo : phraseList) {
    // System.out.println("NP\t" + phraseInfo.toString().replaceAll(" ", "\t") +
    // "\nNPTree\t"
    // + phraseInfo.getTree());
    // }
    // return phraseList;
    // }

}

