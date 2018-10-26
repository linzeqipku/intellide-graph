package cn.edu.pku.sei.intellide.graph.extraction.graph_xml_parser;

import cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector.CodeMentionDetector;
import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.extraction.docx_linker.DocLinker;
import cn.edu.pku.sei.intellide.graph.extraction.docx_to_neo4j.DocxGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.git_to_neo4j.GitGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j.JiraGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.mail_to_neo4j.MailGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.StackOverflowGraphBuilder;
import cn.edu.pku.sei.intellide.graph.webapp.entity.SnowGraphProject;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GraphXmlParser {

    private static DocumentBuilderFactory dbFactory = null;
    private static DocumentBuilder db =null;
    private static Document document = null;
    private static List<ProgramXml> programs = null;
    static {
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            db = dbFactory.newDocumentBuilder();
        }catch (ParserConfigurationException e){
            e.printStackTrace();
        }
    }

    public static List<ProgramXml> getPrograms(String fileName) throws Exception{
        document = db.parse(fileName);
        NodeList programList = document.getElementsByTagName("program");
        programs = new ArrayList<ProgramXml>();

        for(int i=0;i<programList.getLength();i++){

            ProgramXml program = new ProgramXml();
            org.w3c.dom.Node node = programList.item(i);
            NamedNodeMap namedNodeMap = node.getAttributes();
            String id = namedNodeMap.getNamedItem("id").getTextContent();
            program.setId(Integer.parseInt(id));
            NodeList cList = node.getChildNodes();
            ArrayList<String> contents = new ArrayList<>();
            for(int j=1;j<cList.getLength();j+=2){
                org.w3c.dom.Node cNode = cList.item(j);
                String content = cNode.getFirstChild().getTextContent();
                contents.add(content);
            }
            program.setName(contents.get(0));
            program.setCodePath(contents.get(1));
            program.setEmailPath(contents.get(2));
            program.setIssuePath(contents.get(3));
            program.setCommitPath(contents.get(4));
            program.setStackoverflowPath(contents.get(5));
            program.setDocxPath(contents.get(6));
            program.setGraphDataBasePath(contents.get(7));
            program.setDescription(contents.get(8));
            program.setProjectJson(contents.get(9));
            programs.add(program);

        }

        return programs;

    }

    public static void process(String fileName){
        try{
            List<ProgramXml> programs = getPrograms(fileName);
            List<List<String>> projectInfo = new ArrayList<>();
            String jsonFilePath = null;

            for(int i = 0;i<programs.size();i++){
                List<String> project = new ArrayList<>();
                ProgramXml program = programs.get(i);
                String GraphDataBasePath = program.getGraphDataBasePath();
                GraphDataBasePath = GraphDataBasePath + "Graph-" + program.getName();

                String CodePath = program.getCodePath();
                JavaCodeGraphBuilder.process(GraphDataBasePath,CodePath);
                String EmailPath = program.getEmailPath();
                if(!EmailPath.equals(" ")){
                    MailGraphBuilder.process(GraphDataBasePath,EmailPath);
                }
                String IssuePath = program.getIssuePath();
                if(!IssuePath.equals(" ")){
                    JiraGraphBuilder.process(GraphDataBasePath,IssuePath);
                }
                String CommitPath = program.getCommitPath();
                if(!CommitPath.equals(" ")){
                    GitGraphBuilder.process(GraphDataBasePath,CommitPath);
                }
                String StackOverflowPath = program.getStackoverflowPath();
                if(!StackOverflowPath.equals(" ")){
                    StackOverflowGraphBuilder.process(GraphDataBasePath,StackOverflowPath);
                }
                String DocxPath = program.getDocxPath();
                if(!DocxPath.equals(" ")){
                    DocxGraphBuilder.process(GraphDataBasePath,DocxPath);
                    DocLinker.process(GraphDataBasePath);
                }
                CodeTokenizer.process(GraphDataBasePath);
                CodeMentionDetector.process(GraphDataBasePath);
                System.out.println("Graph Done!");
                project.add(program.getName());
                project.add(program.getDescription());
                projectInfo.add(project);
                jsonFilePath = program.getProjectJson();
            }

            SnowGraphProject.wirte2json(projectInfo,jsonFilePath);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args){
        String fileName = args[0];
        GraphXmlParser.process(fileName);

        System.out.println("数据库已完成!");



    }


}
