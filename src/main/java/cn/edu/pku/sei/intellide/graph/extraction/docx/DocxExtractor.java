package cn.edu.pku.sei.intellide.graph.extraction.docx;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import edu.stanford.nlp.pipeline.Requirement;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.TableWidthType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.IBodyElement;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class DocxExtractor extends KnowledgeExtractor {

    public static final Label RequirementSection = Label.label("RequirementSection");
    public static final Label FeatureSection = Label.label("FeatureSection");
    public static final RelationshipType PARENT = RelationshipType.withName("parent");
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String TABLE = "table";
    public static final String LEVEL = "level";
    public static final String SERIAL = "serial";

    /* Auxiliary Data Structures */
    int currentLevel;
    int level1, level2, level3; // title serial number
    int num0, num1, num2;       // entity content key-id
    boolean flag;               // doc title flag
    String tmpKey, tmpVal;      // level-4 title tmp var
    RequirementSection title0, title1, title2, title3;

    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {
        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[] { "docx" }, true)) {
            String fileName = file.getAbsolutePath().substring(new File(this.getDataDir()).getAbsolutePath().length())
                    .replaceAll("^[/\\\\]+", "");
            XWPFDocument xd = new XWPFDocument(new FileInputStream(file));
            Map<String, RequirementSection> map1 = new HashMap<>();
            Map<String, FeatureSection> map2 = new HashMap<>();
            init();
            if (fileName.contains("需求")) {
                parseRequirement(xd, map1);
            } else if (fileName.contains("特性")) {
                parseFeature(xd, map2);
            }
            for (RequirementSection requirementSection : map1.values()) {
                requirementSection.toNeo4j(this.getInserter());
            }
            for (FeatureSection featureSection : map2.values()) {
                featureSection.toNeo4j(this.getInserter());
            }
        }
    }

    public void init() {
        currentLevel = 0;
        level1 = 0; level2 = 0; level3 = 0;
        num0 = 0; num1 = 0; num2 = 0;
        flag = false;
        tmpKey = ""; tmpVal = "";
        title0 = new RequirementSection();
        title1 = new RequirementSection();
        title2 = new RequirementSection();
        title3 = new RequirementSection();
    }

    public void handleParagraph(Iterator<IBodyElement> bodyElementsIterator, XWPFParagraph para, Map<String, RequirementSection> map) throws JSONException {
        if (para.getText() == null) return;
        // title of document
        if (flag == false) {
            title0.title = para.getText();
            title0.level = 0;
            title0.serial = 0;
            flag = true;
            return;
        }
        int titleLevel;
        if (para.getStyleID().length() != 1)
            titleLevel = -1;
        else
            titleLevel = Integer.parseInt(para.getStyleID());
        switch (titleLevel) {
            case 1: {
                if (title1 != null)
                    map.put(title1.title, title1);
                title1 = new RequirementSection();
                level1++; level2 = 0; num1 = 0;
                title1.title = para.getText();
                title1.level = 1;
                title1.serial = level1;
                title0.children.add(title1);
                currentLevel = 1;
                break;
            }
            case 2: {
                if (title2 != null)
                    map.put(title2.title, title2);
                title2 = new RequirementSection();
                level2++; level3 = 0; num2 = 0;
                title2.title = para.getText();
                title2.level = 2;
                title2.serial = level2;
                title1.children.add(title2);
                currentLevel = 2;
                break;
            }
            case 3: {
                if (title3 != null)
                    map.put(title3.title, title3);
                title3 = new RequirementSection();
                level3++;
                title3.level = 3;
                title3.title = para.getText();
                title3.serial = level3;
                title2.children.add(title3);
                currentLevel = 3;
                break;
            }
            default: {
                // non-title content
                if (currentLevel == 0) {
                    // content between title0 and title1
                    title0.content.put(String.valueOf(++num0), para.getText());
                } else if (currentLevel == 1) {
                    title1.content.put(String.valueOf(++num1), para.getText());
                } else if (currentLevel == 2) {
                    title2.content.put(String.valueOf(++num2), para.getText());
                } else if (currentLevel == 3) {
                    // TODO: level-3 title content: level-4 title content considered only
                    if (titleLevel == 4) {
                        tmpKey = para.getText();
                        tmpVal = "";
                    } 
                    else {
                        while(bodyElementsIterator.hasNext()) {
                            IBodyElement tmpElement = bodyElementsIterator.next();
                            if(tmpElement instanceof XWPFParagraph) {
                                String styleID = ((XWPFParagraph)(tmpElement)).getStyleID();
                                if (styleID != "1" && styleID != "2" && styleID != "3" && styleID != "4") {
                                    tmpVal += ((XWPFParagraph)(tmpElement)).getText();
                                }
                            }
                            else if(tmpElement instanceof XWPFTable) {
                                title3.content.put(tmpKey, tmpVal);
                                handleTable(((XWPFTable)tmpElement));
                            }
                        }
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
        if(currentLevel == 0) title0.table.add(ja);
        else if(currentLevel == 1) title1.table.add(ja);
        else if(currentLevel == 2) title2.table.add(ja);
        else if(currentLevel == 3) title3.table.add(ja);
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
        if(title0 != null) map.put(title0.title, title0);
        if(title1 != null) map.put(title1.title, title1);
        if(title2 != null) map.put(title2.title, title2);
        if(title3 != null) map.put(title3.title, title3);
    }

    public void parseFeature(XWPFDocument xd, Map<String, FeatureSection> map) {

    }

    class RequirementSection {
        String title = "";
        int level = -1;
        int serial = 0;
        JSONObject content = new JSONObject();
        ArrayList<JSONArray> table = new ArrayList<JSONArray>();
        List<RequirementSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            Map<String, Object> map = new HashMap<>();
            map.put(DocxExtractor.TITLE, title);
            map.put(DocxExtractor.LEVEL, level);
            map.put(DocxExtractor.SERIAL, serial);
            map.put(DocxExtractor.CONTENT, content);
            map.put(DocxExtractor.TABLE, table);
            long node = inserter.createNode(map, new Label[] { DocxExtractor.RequirementSection });
            for (int i = 0; i < children.size(); i++) {
                RequirementSection child = children.get(i);
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(DocxExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, DocxExtractor.PARENT, rMap);
            }
            return node;
        }

    }

    class FeatureSection {
        String title = "";
        int level = -1;
        int serial = 0;
        JSONObject content = new JSONObject();
        ArrayList<JSONArray> table = new ArrayList<JSONArray>();
        List<FeatureSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            Map<String, Object> map = new HashMap<>();
            map.put(DocxExtractor.TITLE, title);
            map.put(DocxExtractor.LEVEL, level);
            map.put(DocxExtractor.SERIAL, serial);
            map.put(DocxExtractor.CONTENT, content);
            map.put(DocxExtractor.TABLE, table);
            long node = inserter.createNode(map, new Label[] { DocxExtractor.FeatureSection });
            for (int i = 0; i < children.size(); i++) {
                FeatureSection child = children.get(i);
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(DocxExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, DocxExtractor.PARENT, rMap);
            }
            return node;
        }
    }
}