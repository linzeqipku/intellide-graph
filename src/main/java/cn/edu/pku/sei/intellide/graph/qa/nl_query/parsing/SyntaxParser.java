package cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SyntaxParser {

    public List<Pair<Integer, Integer>> parse(String text){
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,parse");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        if (!(text.endsWith(".") || text.endsWith("?")))
            text = text + ".";
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);

        // annnotate the document
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        // get the first sentecne's tree
        Tree tree = sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
        System.out.println(tree);
        Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());

        List<Pair<Integer, Integer>> seq = new ArrayList<>();
        for (Constituent constituent : treeConstituents) {
            if (constituent.label() != null
                    && (!constituent.label().toString().equals("ROOT"))) {
                System.out.println("found constituent: "+constituent.toString());
                System.out.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
                seq.add(Pair.of(constituent.start(), constituent.end()));
            }
        }
        return seq;
    }
}
