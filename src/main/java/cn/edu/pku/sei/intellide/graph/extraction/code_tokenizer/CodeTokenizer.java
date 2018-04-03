package cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer;

import cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector.CodeMentionDetector;
import cn.edu.pku.sei.intellide.graph.extraction.docx_to_neo4j.DocxGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeTokenizer {

    public static final String TOKENS="tokens";

    public static void process(String graphDir) {
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.findNodes(JavaCodeGraphBuilder.CLASS);
            nodes.stream().forEach(node->wordExtraction(node));
            nodes = db.findNodes(JavaCodeGraphBuilder.METHOD);
            nodes.stream().forEach(node->wordExtraction(node));
            nodes = db.findNodes(JavaCodeGraphBuilder.FIELD);
            nodes.stream().forEach(node->wordExtraction(node));
            tx.success();
        }
        db.shutdown();
    }

    private static void wordExtraction(Node node) {
        String content = "";
        if (node.hasProperty(JavaCodeGraphBuilder.COMMENT))
            content += (String) node.getProperty(JavaCodeGraphBuilder.COMMENT);
        if (node.hasProperty(JavaCodeGraphBuilder.FULLNAME))
            content += (String) node.getProperty(JavaCodeGraphBuilder.FULLNAME);
        Iterable<Relationship> rels = node.getRelationships(CodeMentionDetector.CODE_MENTION, Direction.OUTGOING);
        for (Relationship rel : rels) {
            Node docxNode = rel.getEndNode();
            int c=0;
            Iterable<Relationship> reverseRels = docxNode.getRelationships(CodeMentionDetector.CODE_MENTION, Direction.INCOMING);
            for (Relationship reverseRel:reverseRels)
                c++;
            if (c>1)
                continue;
            if (docxNode.hasProperty(DocxGraphBuilder.CONTENT))
                content += " " + docxNode.getProperty(DocxGraphBuilder.CONTENT);
        }
        Set<String> tokens=tokenization(content);
        node.setProperty(TOKENS, StringUtils.join(tokens," "));
    }

    public static Set<String> tokenization(String content) {
        Set<String> r = new HashSet<>();
        content = content.replaceAll("[^\\u4e00-\\u9fa5\\w]+", " ");
        content.trim();
        if (content.length()==0)
            return r;
        List<Term> terms = HanLP.segment(content);
        for (Term term : terms) {
            String word = term.word;
            if (word.matches("\\w+")) {
                List<String> tokens=englishTokenization(word);
                r.add(word.toLowerCase());
                r.addAll(tokens);
            } else if (word.matches("[\\u4e00-\\u9fa5]+"))
                r.add(word);
        }
        return r;
    }

    /**
     * IndexLDAException_conf --> indexldaexception + index + lda + exception + conf
     */
    private static List<String> englishTokenization(String word){
        List<String> tokens=new ArrayList<>();
        String[] eles=word.trim().split("[^A-Za-z]+");
        for (String e:eles){
            List<String> humps=camelSplit(e);
            tokens.add(e.toLowerCase());
            if (humps.size()>0)
                tokens.addAll(humps);
        }
        return tokens;
    }

    /**
     *  IndexLDAException --> index + lda + exception
     */
    private static List<String> camelSplit(String e) {
        List<String> r = new ArrayList<>();
        Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
        if (m.find()) {
            String s = m.group().toLowerCase();
            r.add(s);
            if (s.length() < e.length())
                r.addAll(camelSplit(e.substring(s.length())));
        }
        return r;
    }

}
