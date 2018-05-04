package cn.edu.pku.sei.intellide.graph.extraction;

import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.extraction.docx_to_neo4j.DocxGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.git_to_neo4j.GitGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector.CodeMentionDetector;
import org.apache.lucene.queryparser.classic.ParseException;
import cn.edu.pku.sei.intellide.graph.extraction.pptx_to_neo4j.PptxGraphBuilder;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import java.io.IOException;

public class SnowGraphTest {

    private static final String GRAPH_DIR_PATH="E:/SnowGraphData/tsr/graphdb";
    private static final String SRC_DIR_PATH="E:\\SnowGraphData\\tsr\\data\\src";
    private static final String GIT_DIR_PATH="E:\\SnowGraphData\\tsr\\data\\git\\.git";
    private static final String DOCX_DIR_PATH="E:\\SnowGraphData\\tsr\\data\\html";
    private static final String PPTX_DIR_PATH="E:/test";

    @Test
    public void testCodeGraphBuilder() throws IOException {
        JavaCodeGraphBuilder.process(GRAPH_DIR_PATH, SRC_DIR_PATH);
    }

    @Test
    public void testGitGraphBuilder() throws IOException, GitAPIException {
        GitGraphBuilder.process(GRAPH_DIR_PATH,GIT_DIR_PATH);
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

    @Test
    public void testPptxGraphBuilder() throws IOException {
        PptxGraphBuilder.process(GRAPH_DIR_PATH,PPTX_DIR_PATH);
    }

}
