package cn.edu.pku.sei.intellide.graph.extraction.docx;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.IBodyElement;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import com.alibaba.fastjson.JSONObject;
//import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DocxExtractor extends KnowledgeExtractor {

    public static final Label RequirementSection = Label.label("RequirementSection");
    public static final Label FeatureSection = Label.label("FeatureSection");
    public static final Label ArchitectureSection = Label.label("ArchitectureSection");
    public static final RelationshipType SUB_DOCX_ELEMENT = RelationshipType.withName("subDocxElement");
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String TABLE = "table";
    public static final String LEVEL = "level";
    public static final String SERIAL = "serial";

    /* Auxiliary Data Structures */
    private int docType;
    private int currentLevel;
    private boolean flag;               // doc title flag
    private int[] levels = new int[4];  // title serial number
    private int[] nums = new int[5];    // entity content key-id
    private String tmpKey, tmpVal;      // level-4 title tmp var

    ArrayList<RequirementSection> titles0 = new ArrayList<RequirementSection>(5);
    ArrayList<FeatureSection> titles1 = new ArrayList<FeatureSection>(5);
    ArrayList<ArchitectureSection> titles2 = new ArrayList<ArchitectureSection>(5);


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
                xd = new XWPFDocument(new FileInputStream(file));
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            Map<String, RequirementSection> map0 = new HashMap<>();
            Map<String, FeatureSection> map1 = new HashMap<>();
            Map<String, ArchitectureSection> map2 = new HashMap<>();
            init();
            if (fileName.contains("需求分析")) {
                docType = 0;
                try {
                    parseRequirement(xd, map0);
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (fileName.contains("特性设计")) {
                docType = 1;
                try {
                    parseFeature(xd, map1);
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
            else if(fileName.contains("架构")) {
                docType = 2;
                try {
                    parseArchitecture(xd, map2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            for (RequirementSection requirementSection : map0.values()) {
                if(requirementSection.level != -1) requirementSection.toNeo4j(this.getInserter());
            }
            for (FeatureSection featureSection : map1.values()) {
                if(featureSection.level != -1) featureSection.toNeo4j(this.getInserter());
            }
            for (ArchitectureSection architectureSection : map2.values()) {
                if(architectureSection.level != -1) architectureSection.toNeo4j(this.getInserter());
            }
        }
    }

    public void init() {
        currentLevel = 0;
        flag = false;
        tmpKey = ""; tmpVal = "";
        titles0.clear();
        titles1.clear();
        for(int i = 1;i <= 3;i++) levels[i] = 0;
        for(int i = 0;i <= 3;i++) {
            nums[i] = 0;
            titles0.add(i, new RequirementSection());
            titles1.add(i, new FeatureSection());
            titles2.add(i, new ArchitectureSection());
        }
    }

    public <T>void infoFill(int styleID, XWPFParagraph para, Map<String, T> map) {
        levels[styleID]++; nums[styleID] = 0;
        if(docType == 0) {
            for(int i = 1;i <= 3;i++) {
                if(titles0.get(i) != null && !map.containsKey(titles0.get(i).title)) {
                    map.put(titles0.get(i).title, (T) titles0.get(i));
                    titles0.get(i-1).children.add(titles0.get(i));
                }
            }
            titles0.set(styleID, new RequirementSection());
            titles0.get(styleID).title = para.getText();
            titles0.get(styleID).level = styleID;
            titles0.get(styleID).serial = levels[styleID];
        }
        else if(docType == 1) {
            for(int i = 1;i <= 3;i++) {
                if(titles1.get(i) != null && !map.containsKey(titles1.get(i).title)) {
                    map.put(titles1.get(i).title, (T) titles1.get(i));
                    titles1.get(i-1).children.add(titles1.get(i));
                }
            }
            titles1.set(styleID, new FeatureSection());
            titles1.get(styleID).title = para.getText();
            titles1.get(styleID).level = styleID;
            titles1.get(styleID).serial = levels[styleID];
        }
        else if(docType == 2) {
            for(int i = 1;i <= 3;i++) {
                if(titles2.get(i) != null && !map.containsKey(titles2.get(i).title)) {
                    map.put(titles2.get(i).title, (T) titles2.get(i));
                    titles2.get(i-1).children.add(titles2.get(i));
                }
            }
            titles2.set(styleID, new ArchitectureSection());
            titles2.get(styleID).title = para.getText();
            titles2.get(styleID).level = styleID;
            titles2.get(styleID).serial = levels[styleID];
        }
        if(styleID < 3) levels[styleID+1] = 0;
    }

    public boolean validText(String text) {
        if(text == null) return false;
        text = text.replaceAll(" ", "");
        if(text.length() == 0) return false;
        return !text.equals("\t") && !text.equals("\r\n");
    }

    public <T>void handleTitle(Iterator<IBodyElement> bodyElementsIterator, XWPFParagraph para, Map<String, T> map) throws JSONException {
        tmpKey = para.getText();
        if(!validText(tmpKey)) return;
        tmpVal = "";
        IBodyElement tmpElement = null;
        while(bodyElementsIterator.hasNext()) {
            tmpElement = bodyElementsIterator.next();
            if(tmpElement instanceof XWPFParagraph) {
                String styleID = ((XWPFParagraph)(tmpElement)).getStyleID();
                if(styleID != null && (styleID.equals("1") || styleID.equals("2") || styleID.equals("3") || styleID.equals("4"))) {
                    if(docType == 0) titles0.get(3).content.put(tmpKey, tmpVal);
                    else if(docType == 1) titles1.get(3).content.put(tmpKey, tmpVal);
                    else if(docType == 2) titles2.get(3).content.put(tmpKey, tmpVal);
                    tmpKey = "";
                    handleParagraph(bodyElementsIterator, ((XWPFParagraph)(tmpElement)), map);
                    break;
                }
                else {
                    tmpVal += (((XWPFParagraph)(tmpElement)).getText() + '\n');
                }
            }
            else if(tmpElement instanceof XWPFTable) {
                if(docType == 0) titles0.get(3).content.put(tmpKey, tmpVal);
                else if(docType == 1) titles1.get(3).content.put(tmpKey, tmpVal);
                else if(docType == 2) titles2.get(3).content.put(tmpKey, tmpVal);
                tmpKey = "";
                handleTable(((XWPFTable)tmpElement));
                break;
            }
        }
    }

    public <T>void handleParagraph(Iterator<IBodyElement> bodyElementsIterator, XWPFParagraph para, Map<String, T> map) throws JSONException {
        if (!validText(para.getText())) return;
        // title of document
        if (!flag) {
            if(docType == 0) {
                titles0.get(0).title = para.getText();
                titles0.get(0).level = 0;
                titles0.get(0).serial = 0;
            }
            else if(docType == 1) {
                titles1.get(0).title = para.getText();
                titles1.get(0).level = 0;
                titles1.get(0).serial = 0;
            }
            else if(docType == 2) {
                titles2.get(0).title = para.getText();
                titles2.get(0).level = 0;
                titles2.get(0).serial = 0;
            }
            flag = true;
            return;
        }
        int titleLevel;
        if (para.getStyleID() == null || para.getStyleID().length() != 1)
            titleLevel = -1;
        else
            titleLevel = Integer.parseInt(para.getStyleID());
        switch (titleLevel) {
            case 1: {
                infoFill(1, para, map);
                currentLevel = 1;
                break;
            }
            case 2: {
                infoFill(2, para, map);
                currentLevel = 2;
                break;
            }
            case 3: {
                infoFill(3, para, map);
                currentLevel = 3;
                break;
            }
            default: {
                // non-title: content attribute
                if (currentLevel == 0) {
                    // content between titles0.get(0) and titles0.get(1)
                    if(docType == 0) titles0.get(0).content.put(String.valueOf(++nums[0]), para.getText());
                    else if(docType == 1) titles1.get(0).content.put(String.valueOf(++nums[0]), para.getText());
                    else if(docType == 2) titles2.get(0).content.put(String.valueOf(++nums[0]), para.getText());
                }
                else if (currentLevel == 1) {
                    if(docType == 0) titles0.get(1).content.put(String.valueOf(++nums[1]), para.getText());
                    else if(docType == 1) titles1.get(1).content.put(String.valueOf(++nums[1]), para.getText());
                    else if(docType == 2) titles2.get(1).content.put(String.valueOf(++nums[1]), para.getText());
                }
                else if (currentLevel == 2) {
                    if(docType == 0) titles0.get(2).content.put(String.valueOf(++nums[2]), para.getText());
                    else if(docType == 1) titles1.get(2).content.put(String.valueOf(++nums[2]), para.getText());
                    else if(docType == 2) titles2.get(2).content.put(String.valueOf(++nums[2]), para.getText());
                }
                else if (currentLevel == 3) {
                    if (titleLevel == 4) {
                        // level-4 title content
                        handleTitle(bodyElementsIterator, para, map);
                    }
                    else {
                        // normal text content
                        if(docType == 0) titles0.get(3).content.put(String.valueOf(++nums[3]), para.getText());
                        else if(docType == 1) titles1.get(3).content.put(String.valueOf(++nums[3]), para.getText());
                        else if(docType == 2) titles2.get(3).content.put(String.valueOf(++nums[3]), para.getText());
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
        if(docType == 0) {
            if(currentLevel == 0) titles0.get(0).table.add(ja);
            else if(currentLevel == 1) titles0.get(1).table.add(ja);
            else if(currentLevel == 2) titles0.get(2).table.add(ja);
            else if(currentLevel == 3) titles0.get(3).table.add(ja);
        }
        else if(docType == 1) {
            if(currentLevel == 0) titles1.get(0).table.add(ja);
            else if(currentLevel == 1) titles1.get(1).table.add(ja);
            else if(currentLevel == 2) titles1.get(2).table.add(ja);
            else if(currentLevel == 3) titles1.get(3).table.add(ja);
        }
        else if(docType == 2) {
            if(currentLevel == 0) titles2.get(0).table.add(ja);
            else if(currentLevel == 1) titles2.get(1).table.add(ja);
            else if(currentLevel == 2) titles2.get(2).table.add(ja);
            else if(currentLevel == 3) titles2.get(3).table.add(ja);
        }
    }

    public void parseRequirement(XWPFDocument xd, Map<String, RequirementSection> map) throws JSONException {

        Iterator<IBodyElement> bodyElementsIterator = xd.getBodyElementsIterator();
        while (bodyElementsIterator.hasNext()) {
            IBodyElement bodyElement = bodyElementsIterator.next();
            if(bodyElement instanceof XWPFTable) {
                handleTable(((XWPFTable) (bodyElement)));
            }
            else if(bodyElement instanceof XWPFParagraph) {
                handleParagraph(bodyElementsIterator, ((XWPFParagraph) (bodyElement)), map);
            }
        }
        for(int i = 0;i <= 3;i++) {
            if(titles0.get(i) != null && !map.containsKey(titles0.get(i).title)) {
                map.put(titles0.get(i).title, titles0.get(i));
                if(i > 0) titles0.get(i-1).children.add(titles0.get(i));
            }
        }
        if(!tmpKey.equals("")) titles0.get(3).content.put(tmpKey, tmpVal);
    }

    public void parseFeature(XWPFDocument xd, Map<String, FeatureSection> map) throws JSONException {
        Iterator<IBodyElement> bodyElementsIterator = xd.getBodyElementsIterator();
        while (bodyElementsIterator.hasNext()) {
            IBodyElement bodyElement = bodyElementsIterator.next();
            if(bodyElement instanceof XWPFTable) {
                handleTable(((XWPFTable) (bodyElement)));
            }
            else if(bodyElement instanceof XWPFParagraph) {
                handleParagraph(bodyElementsIterator, ((XWPFParagraph) (bodyElement)), map);
            }
        }
        for(int i = 0;i <= 3;i++) {
            if(titles1.get(i) != null && !map.containsKey(titles1.get(i).title)) {
                map.put(titles1.get(i).title, titles1.get(i));
                if(i > 0) titles1.get(i-1).children.add(titles1.get(i));
            }
        }
        if(!tmpKey.equals("")) titles1.get(3).content.put(tmpKey, tmpVal);
    }

    public void parseArchitecture(XWPFDocument xd, Map<String, ArchitectureSection> map) throws JSONException {
        Iterator<IBodyElement> bodyElementsIterator = xd.getBodyElementsIterator();
        while (bodyElementsIterator.hasNext()) {
            IBodyElement bodyElement = bodyElementsIterator.next();
            if(bodyElement instanceof XWPFTable) {
                handleTable(((XWPFTable) (bodyElement)));
            }
            else if(bodyElement instanceof XWPFParagraph) {
                handleParagraph(bodyElementsIterator, ((XWPFParagraph) (bodyElement)), map);
            }
        }
        for(int i = 0;i <= 3;i++) {
            if(titles2.get(i) != null && !map.containsKey(titles2.get(i).title)) {
                map.put(titles2.get(i).title, titles2.get(i));
                if(i > 0) titles2.get(i-1).children.add(titles2.get(i));
            }
        }
        if(!tmpKey.equals("")) titles2.get(3).content.put(tmpKey, tmpVal);
    }

    class RequirementSection {
        long node = -1;
        String title = "";
        int level = -1;
        int serial = 0;
        JSONObject content = new JSONObject(new LinkedHashMap<>());
        ArrayList<JSONArray> table = new ArrayList<>();
        ArrayList<RequirementSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            if(node != -1) return node;
            Map<String, Object> map = new HashMap<>();
            map.put(DocxExtractor.TITLE, title);
            map.put(DocxExtractor.LEVEL, level);
            map.put(DocxExtractor.SERIAL, serial);
            map.put(DocxExtractor.CONTENT, content.toString());
            map.put(DocxExtractor.TABLE, table.toString());
            node = inserter.createNode(map, new Label[] { DocxExtractor.RequirementSection });
            for (int i = 0; i < children.size(); i++) {
                RequirementSection child = children.get(i);
                if(child.level == -1) continue;
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(DocxExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, DocxExtractor.SUB_DOCX_ELEMENT, rMap);
            }
            return node;
        }
    }

    class FeatureSection {
        long node = -1;
        String title = "";
        int level = -1;
        int serial = 0;
        JSONObject content = new JSONObject(new LinkedHashMap<>());
        ArrayList<JSONArray> table = new ArrayList<>();
        ArrayList<FeatureSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            if(node != -1) return node;
            Map<String, Object> map = new HashMap<>();
            map.put(DocxExtractor.TITLE, title);
            map.put(DocxExtractor.LEVEL, level);
            map.put(DocxExtractor.SERIAL, serial);
            map.put(DocxExtractor.CONTENT, content.toString());
            map.put(DocxExtractor.TABLE, table.toString());
            node = inserter.createNode(map, new Label[] { DocxExtractor.FeatureSection });
            for (int i = 0; i < children.size(); i++) {
                FeatureSection child = children.get(i);
                if(child.level == -1) continue;
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(DocxExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, DocxExtractor.SUB_DOCX_ELEMENT, rMap);
            }
            return node;
        }
    }

    class ArchitectureSection {
        long node = -1;
        String title = "";
        int level = -1;
        int serial = 0;
        JSONObject content = new JSONObject(new LinkedHashMap<>());
        ArrayList<JSONArray> table = new ArrayList<>();
        ArrayList<ArchitectureSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            if(node != -1) return node;
            Map<String, Object> map = new HashMap<>();
            map.put(DocxExtractor.TITLE, title);
            map.put(DocxExtractor.LEVEL, level);
            map.put(DocxExtractor.SERIAL, serial);
            map.put(DocxExtractor.CONTENT, content.toString());
            map.put(DocxExtractor.TABLE, table.toString());
            node = inserter.createNode(map, new Label[] { DocxExtractor.ArchitectureSection });
            for (int i = 0; i < children.size(); i++) {
                ArchitectureSection child = children.get(i);
                if(child.level == -1) continue;
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(DocxExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, DocxExtractor.SUB_DOCX_ELEMENT, rMap);
            }
            return node;
        }
    }
}
