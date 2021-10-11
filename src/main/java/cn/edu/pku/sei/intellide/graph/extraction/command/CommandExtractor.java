package cn.edu.pku.sei.intellide.graph.extraction.command;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.docx.DocxExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.IBodyElement;

import org.neo4j.graphdb.Label;
import org.neo4j.unsafe.batchinsert.BatchInserter;

//import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CommandExtractor extends KnowledgeExtractor {

    public static final Label Command = Label.label("Command");

    public static final String NAME = "name";
    public static final String FORMAT = "format";
    public static final String FUNCTION = "function";
    public static final String PARAMETERS = "parameters";
    public static final String VIEW = "view";
    public static final String DLEVEL = "default_level";
    public static final String USAGE = "usage";
    public static final String EXAMPLE = "example";
    public static final String BELONG = "belong_to";



    /* Auxiliary Data Structures */
    private int currentLevel;
    private boolean flag, if_table;               // doc title flag
    private String tmpKey, tmpVal, now_title, nowcommand;      // level-4 title tmp var
    ArrayList<Command> titles1 = new ArrayList<Command>(3);


    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {
        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[] { "docx" }, true)) {
            String fileName = file.getAbsolutePath().substring(new File(this.getDataDir()).getAbsolutePath().length())
                    .replaceAll("^[/\\\\]+", "");
            XWPFDocument xd = null;
            try {
                FileInputStream fs = new FileInputStream(file);
                xd = new XWPFDocument(fs);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            init();
            try {
                parseCommand(xd);
            }
            catch(JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void init() {
        currentLevel = 0;
        flag = false;
        if_table = false;
        tmpKey = ""; tmpVal = "";
        titles1.clear();
        for(int i = 0;i <= 3;i++) {
            titles1.add(i, new CommandExtractor.Command());
        }
    }

    public boolean validText(String text) {
        if(text == null) return false;
        text = text.replaceAll(" ", "");
        if(text.length() == 0) return false;
        return !text.equals("\t") && !text.equals("\r\n");
    }

    public void handleTitle(Iterator<IBodyElement> bodyElementsIterator, XWPFParagraph para) throws JSONException {
        tmpKey = para.getText();

        if(!validText(tmpKey)) return;
        tmpVal = "";
        IBodyElement tmpElement = null;
        while(bodyElementsIterator.hasNext()) {
            tmpElement = bodyElementsIterator.next();
            if(tmpElement instanceof XWPFParagraph) {
                String styleID = ((XWPFParagraph)(tmpElement)).getStyleID();
                String str = ((XWPFParagraph)(tmpElement)).getText();
                
                if(styleID != null && (styleID.equals("1") || styleID.equals("2"))) {
                    titles1.get(0).example = tmpVal;
                    System.out.println(titles1.get(0).name);
                    titles1.get(0).toNeo4j(this.getInserter());
                    titles1.set(0, new CommandExtractor.Command());
                    tmpVal = "";
                    handleParagraph(bodyElementsIterator, ((XWPFParagraph)(tmpElement)));
                    break;
                }
                else {
                    if (str != null && str.contains("命令功能")){
                        titles1.get(0).name = nowcommand;
                        titles1.get(0).belong_to = now_title;
                        tmpVal = "";
                    }
                    if (str != null && str.contains("命令格式")){
                        titles1.get(0).function = tmpVal;
                        tmpVal = "";
                    }
                    if (str != null && str.contains("参数说明")){
                        if_table = true;
                        titles1.get(0).format = tmpVal;
                        tmpVal = "";
                    }
                    if (str != null && str.contains("视图")){
                        if_table = false;
                        tmpVal = "";
                    }
                    if (str != null && str.contains("缺省级别")){
                        titles1.get(0).view = tmpVal;
                        tmpVal = "";
                    }
                    if (str != null && str.contains("使用指南")){
                        titles1.get(0).default_level = tmpVal;
                        tmpVal = "";
                    }
                    if (str != null && str.contains("使用实例")){
                        titles1.get(0).usage = tmpVal;
                        tmpVal = "";
                    }
                    else {
                        tmpVal = tmpVal + (((XWPFParagraph) (tmpElement)).getText() + '\n');
                    }
                }
                
            }
            else if(tmpElement instanceof XWPFTable) {
                if(if_table)
                    handleTable(((XWPFTable)tmpElement));
            }
        }
    }

    public void handleParagraph(Iterator<IBodyElement> bodyElementsIterator, XWPFParagraph para) throws JSONException {
        if (!validText(para.getText())) return;
        int titleLevel;
        if (para.getStyleID() == null || para.getStyleID().length() != 1)
            titleLevel = -1;
        else
            titleLevel = Integer.parseInt(para.getStyleID());
        switch (titleLevel) {
            case 1: {
                now_title = para.getText();
                currentLevel = 1;
                break;
            }
            default: {
                if (currentLevel == 1) {
                    if (titleLevel == 2){
                        if(para.getText()!= null && para.getText().contains("命令支持情况")) return;
                        nowcommand = para.getText();
                        handleTitle(bodyElementsIterator, para);
                    }
                }
            }
        }
    }

    public void handleTable(XWPFTable table) {
        JSONArray ja = new JSONArray();
        String[] lines = table.getText().split("\\r?\\n");
        for(String line : lines) {
            ja.put(line);
        }
        titles1.get(0).parameters = ja;
    }

    public void parseCommand(XWPFDocument xd) throws JSONException {

        Iterator<IBodyElement> bodyElementsIterator = xd.getBodyElementsIterator();
        while (bodyElementsIterator.hasNext()) {
            IBodyElement bodyElement = bodyElementsIterator.next();
            if(bodyElement instanceof XWPFTable) {
                handleTable(((XWPFTable) (bodyElement)));
            }
            else if(bodyElement instanceof XWPFParagraph) {
                handleParagraph(bodyElementsIterator, ((XWPFParagraph) (bodyElement)));
            }
        }


        if(titles1.get(0) != null) {
            titles1.get(0).example = tmpVal;
            titles1.get(0).toNeo4j(this.getInserter());
        }
    }

    class Command {
        long node = -1;
        String title = "";
        String name;
        String format;
        String function;
        JSONArray parameters = new JSONArray();
        String view;
        String default_level;
        String usage;
        String example;
        String belong_to;

        public long toNeo4j(BatchInserter inserter) {
            if(node != -1) return node;
            Map<String, Object> map = new HashMap<>();
            map.put(CommandExtractor.NAME, name);
            map.put(CommandExtractor.FORMAT, format);
            map.put(CommandExtractor.FUNCTION, function);
            map.put(CommandExtractor.PARAMETERS, parameters.toString());
            map.put(CommandExtractor.DLEVEL, default_level);
            map.put(CommandExtractor.USAGE, usage);
            map.put(CommandExtractor.EXAMPLE, example);
            map.put(CommandExtractor.BELONG, belong_to);
            node = inserter.createNode(map, new Label[] { CommandExtractor.Command });
            return node;
        }
    }
}