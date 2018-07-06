package cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer;

import cn.edu.pku.sei.intellide.graph.extraction.git_to_neo4j.GitGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j.JiraGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.mail_to_neo4j.MailGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.StackOverflowGraphBuilder;
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

/**
 * Precondition: 全部的数据解析工作已经做完了。
 * 对于开源软件数据，识别出需要当成文本来处理的结点，给它们添加属性isText=true，用"_title"来统一表示它们的标题，用"_text"来统一表示它们的文本内容；
 * 此外，对于代码结点，对它们的名字进行驼峰切词，把切词结果放到codeTokens属性里面去。
 */

public class CodeTokenizer {

    public static final String IS_TEXT="isText";
    public static final String TITLE="_title";
    public static final String TEXT="_text";
    public static final String CODE_TOKENS ="codeTokens";

    public static void process(String graphDir) {
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.findNodes(JavaCodeGraphBuilder.CLASS);
            nodes.stream().forEach(node->codeTokenExtraction(node));
            nodes = db.findNodes(JavaCodeGraphBuilder.METHOD);
            nodes.stream().forEach(node->codeTokenExtraction(node));
            nodes = db.findNodes(JavaCodeGraphBuilder.FIELD);
            nodes.stream().forEach(node->codeTokenExtraction(node));
            tx.success();
        }

        List<List<Node>> nodeSegs = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.getAllNodes().iterator();
            List<Node> list=new ArrayList<>();
            while (nodes.hasNext()){
                Node node=nodes.next();
                if (list.size()<1000)
                    list.add(node);
                else {
                    nodeSegs.add(list);
                    list=new ArrayList<>();
                }
            }
            if (list.size()>0)
                nodeSegs.add(list);
            tx.success();
        }
        for (List<Node> list:nodeSegs)
            try (Transaction tx = db.beginTx()) {
                for (Node node:list)
                    flossTextExtraction(node);
                tx.success();
            }

        db.shutdown();
    }

    private static void flossTextExtraction(Node node) {
        if (node.hasProperty(TITLE))
            node.removeProperty(TITLE);
        if (node.hasProperty(TEXT))
            node.removeProperty(TEXT);
        if (node.hasProperty(IS_TEXT))
            node.removeProperty(IS_TEXT);

        if (node.hasLabel(JavaCodeGraphBuilder.CLASS)||node.hasLabel(JavaCodeGraphBuilder.METHOD)) {
            node.setProperty(TITLE, node.getProperty(JavaCodeGraphBuilder.FULLNAME));
            node.setProperty(TEXT, node.getProperty(JavaCodeGraphBuilder.CONTENT));
            node.setProperty(IS_TEXT, true);
        }

        if (node.hasLabel(GitGraphBuilder.COMMIT)){
            node.setProperty(TITLE, node.getProperty(GitGraphBuilder.NAME));
            node.setProperty(TEXT, node.getProperty(GitGraphBuilder.MESSAGE));
            node.setProperty(IS_TEXT, true);
        }

        //TODO: ISSUE, EMAIL,STACKOVERFLOW

        //增加了ISSUE数据的ISSUE和ISSUECOMMENT的文本信息
        if (node.hasLabel(JiraGraphBuilder.ISSUE)){
            node.setProperty(TITLE, node.getProperty(JiraGraphBuilder.ISSUE_NAME));
            node.setProperty(TEXT, node.getProperty(JiraGraphBuilder.ISSUE_DESCRIPTION));
            node.setProperty(IS_TEXT, true);
        }
        if (node.hasLabel(JiraGraphBuilder.ISSUECOMMENT)){
            node.setProperty(TITLE, node.getProperty(JiraGraphBuilder.ISSUECOMMENT_CREATOR_NAME));
            node.setProperty(TEXT, node.getProperty(JiraGraphBuilder.ISSUECOMMENT_BODY));
            node.setProperty(IS_TEXT, true);
        }
        //增加了MAIL数据的文本信息
        if (node.hasLabel(MailGraphBuilder.MAIL)){
            node.setProperty(TITLE, node.getProperty(MailGraphBuilder.MAIL_SUBJECT));
            node.setProperty(TEXT, node.getProperty(MailGraphBuilder.MAIL_BODY));
            node.setProperty(IS_TEXT, true);
        }
        //增加SO数据的文本信息
        if (node.hasLabel(StackOverflowGraphBuilder.QUESTION)){
            node.setProperty(TITLE, node.getProperty(StackOverflowGraphBuilder.QUESTION_TITLE));
            node.setProperty(TEXT, node.getProperty(StackOverflowGraphBuilder.QUESTION_BODY));
            node.setProperty(IS_TEXT, true);
        }
        if (node.hasLabel(StackOverflowGraphBuilder.ANSWER)){
            node.setProperty(TITLE, node.getProperty(StackOverflowGraphBuilder.ANSWER_BODY));
            node.setProperty(TEXT, node.getProperty(StackOverflowGraphBuilder.ANSWER_BODY));
            node.setProperty(IS_TEXT, true);
        }

    }

    private static void codeTokenExtraction(Node node) {
        String content = "";
        if (node.hasProperty(JavaCodeGraphBuilder.FULLNAME))
            content += (String) node.getProperty(JavaCodeGraphBuilder.NAME);
        Set<String> tokens=tokenization(content);
        node.setProperty(CODE_TOKENS, StringUtils.join(tokens," "));
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
    public static void main(String[] args){
        CodeTokenizer.process("F:\\Apache\\GraphDataBase\\Graph-TSR-chinese");
    }

}
