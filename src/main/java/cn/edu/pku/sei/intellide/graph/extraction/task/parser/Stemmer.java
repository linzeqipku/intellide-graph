package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.Logger;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Stemmer {
	private static final Logger logger = Logger.getLogger(Stemmer.class);

	private static Properties				props;
	private static final StanfordCoreNLP	pipeline;

	private static List<String>	NOT_TO_STEM_WORDS;
	private static List<String>	NOT_TO_STEM_LABELS;

	static {
		NOT_TO_STEM_WORDS = new ArrayList<String>();
		NOT_TO_STEM_WORDS.add("API");
		NOT_TO_STEM_WORDS.add("api");
		NOT_TO_STEM_WORDS.add("data");
		NOT_TO_STEM_WORDS.add("formula");
		NOT_TO_STEM_WORDS.add("POI");
		NOT_TO_STEM_WORDS.add("poi");
		NOT_TO_STEM_WORDS.add("MS");
		NOT_TO_STEM_WORDS.add("ms");
		NOT_TO_STEM_WORDS.add("xls");
		NOT_TO_STEM_WORDS.add("XLS");
		NOT_TO_STEM_WORDS.add("wiki");
		NOT_TO_STEM_WORDS.add("Wiki");

		NOT_TO_STEM_LABELS = new ArrayList<String>();
		NOT_TO_STEM_LABELS.add("JJ");
		NOT_TO_STEM_LABELS.add("JJR");
		NOT_TO_STEM_LABELS.add("JJS");
		NOT_TO_STEM_LABELS.add("NNP");
		NOT_TO_STEM_LABELS.add("DT");
	}

	static {
		long t1 = System.currentTimeMillis();
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
		long t2 = System.currentTimeMillis();
		logger.info("Initiating pipeline... " + (t2 - t1) + "ms");
	}

	// 将树的每个节点上的词都词干化
	public static Tree stemTree(Tree tree) {
		if (tree == null)
			return null;

		if (tree.isLeaf()) {
			// 叶子结点
			String stemmedString = stem(tree.value());
			tree.setValue(stemmedString);
			return tree;
		}

		if (tree.isPreTerminal()) {
			// 树的下一层节点是终结符，也就是说，树是pos tag
			String label = tree.label().toString();
			if (NOT_TO_STEM_LABELS.contains(label)) {
				return tree;
			}
		}

		Tree[] children = tree.children();
		for (int i = 0; i < children.length; i++) {
			stemTree(children[i]);
		}

		return tree;
	}

	public static String stem(String str) {
		Annotation document = new Annotation(str);
		pipeline.annotate(document);
		String sent = "";
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String s = token.toString().trim();
				if (!NOT_TO_STEM_WORDS.contains(s))
					s = token.get(CoreAnnotations.LemmaAnnotation.class);
				sent = sent + " " + s;
			}
		}
		return sent.trim();
	}

	public static String stem_snowball(String str) {
		EnglishStemmer stemmer = new EnglishStemmer();
		stemmer.setCurrent(str);
		stemmer.stem();
		return stemmer.getCurrent();
	}

	public static void main(String args[]) {
		String s = "tried better APIs reading exactly MS apis api ms going went wanted fisher solution dealing with empty sheet cells";
		// String str = "better";
		long t2 = System.currentTimeMillis();

		System.out.println(stem(s));
		System.out.println(stem_snowball("exactly"));
		System.out.println(stem("building"));
		System.out.println(System.currentTimeMillis() - t2);
		t2 = System.currentTimeMillis();

		String testString = "reading ms wiki files by adding below dependencies from data";
		System.out.println(stem(testString));
		Tree tree = StanfordParser.parseTree(testString);
		tree.pennPrint();

	}

}
