package cn.edu.pku.sei.intellide.graph.extraction;

import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.extraction.docx_to_neo4j.DocxGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector.CodeMentionDetector;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;

import java.io.IOException;

public class SnowGraphTest {

    private static final String GRAPH_DIR_PATH="E:/dc/graph.db";
    private static final String SRC_DIR_PATH="E:/dc/data/";
    private static final String GIT_DIR_PATH="D:/test/lucene-solr/.git";
    private static final String DOCX_DIR_PATH="E:/dc/data/html";

    @Test
    public void testCodeGraphBuilder() throws IOException {
        JavaCodeGraphBuilder.process(GRAPH_DIR_PATH, SRC_DIR_PATH);
    }

    @Test
    public void testDocxGraphBuilder() throws IOException {
        DocxGraphBuilder.process(GRAPH_DIR_PATH,DOCX_DIR_PATH);
    }

    @Test
    public void testCodeMentionDetector() throws IOException, ParseException {
        CodeMentionDetector.process(GRAPH_DIR_PATH);
    }

    @Test
    public void testCodeTokenizer(){
        CodeTokenizer.process(GRAPH_DIR_PATH);
    }

}
