package cn.edu.pku.sei.intellide.graph.extraction.docx;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
<<<<<<< HEAD
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
=======
import edu.stanford.nlp.pipeline.Requirement;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.TableWidthType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
import org.apache.poi.xwpf.usermodel.IBodyElement;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
<<<<<<< HEAD
import scala.collection.immutable.List;
=======
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
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
<<<<<<< HEAD
    private int docType;
    private int currentLevel;
    private boolean flag;               // doc title flag
    private int[] levels = new int[4];  // title serial number
    private int[] nums = new int[5];    // entity content key-id
    private String tmpKey, tmpVal;      // level-4 title tmp var

    ArrayList<RequirementSection> titles0 = new ArrayList<RequirementSection>(5);
    ArrayList<FeatureSection> titles1 = new ArrayList<FeatureSection>(5);
    Map<String, RequirementSection> map0;
    Map<String, FeatureSection> map1;
=======
    int currentLevel;
    int level1, level2, level3; // title serial number
    int num0, num1, num2;       // entity content key-id
    boolean flag;               // doc title flag
    String tmpKey, tmpVal;      // level-4 title tmp var
    RequirementSection title0, title1, title2, title3;
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2

    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {
        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[] { "docx" }, true)) {
            String fileName = file.getAbsolutePath().substring(new File(this.getDataDir()).getAbsolutePath().length())
                    .replaceAll("^[/\\\\]+", "");
<<<<<<< HEAD
            XWPFDocument xd = null;
            try {
                xd = new XWPFDocument(new FileInputStream(file));
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            init();
            if (fileName.contains("需求分析")) {
                docType = 0;
                try {
                    parseRequirement(xd, map0);
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            } else if (fileName.contains("特性设计")) {
                docType = 1;
                try {
                    parseFeature(xd, map1);
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
            for (RequirementSection requirementSection : map0.values()) {
                if(requirementSection.level != -1) requirementSection.toNeo4j(this.getInserter());
            }
            for (FeatureSection featureSection : map1.values()) {
                if(featureSection.level != -1) featureSection.toNeo4j(this.getInserter());
=======
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
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
            }
        }
    }

    public void init() {
        currentLevel = 0;
<<<<<<< HEAD
        flag = false;
        tmpKey = ""; tmpVal = "";
        map0 = new HashMap<>();
        map1 = new HashMap<>();
        titles0.clear();
        titles1.clear();
        for(int i = 1;i <= 3;i++) levels[i] = 0;
        for(int i = 0;i <= 3;i++) {
            nums[i] = 0;
            titles0.add(i, new RequirementSection());
            titles1.add(i, new FeatureSection());
        }
    }
    
    public void infoFill_r(int styleID, XWPFParagraph para, Map<String, RequirementSection> map) {
        for(int i = 1;i <= 3;i++) {
            if(titles0.get(i) != null && !map.containsKey(titles0.get(i).title)) {
                map.put(titles0.get(i).title, titles0.get(i));
                titles0.get(i-1).children.add(titles0.get(i));
            }
        }
        titles0.set(styleID, new RequirementSection());
        levels[styleID]++; nums[styleID] = 0;
        titles0.get(styleID).title = para.getText();
        titles0.get(styleID).level = styleID;
        titles0.get(styleID).serial = levels[styleID];
        if(styleID < 3) levels[styleID+1] = 0;

    }

    public void infoFill_f(int styleID, XWPFParagraph para, Map<String, FeatureSection> map) {
        for(int i = 1;i <= 3;i++) {
            if(titles1.get(i) != null && !map.containsKey(titles1.get(i).title)) {
                map.put(titles1.get(i).title, titles1.get(i));
                titles1.get(i-1).children.add(titles1.get(i));
            }
        }
        titles1.set(styleID, new FeatureSection());
        levels[styleID]++; nums[styleID] = 0;
        titles1.get(styleID).title = para.getText();
        titles1.get(styleID).level = styleID;
        titles1.get(styleID).serial = levels[styleID];
        if(styleID < 3) levels[styleID+1] = 0;

    }

    public void handleParagraph_r(Iterator<IBodyElement> bodyElementsIterator, XWPFParagraph para, Map<String, RequirementSection> map) throws JSONException {
        if (para.getText() == null) return;
        // title of document
        if (!flag) {
            titles0.get(0).title = para.getText();
            titles0.get(0).level = 0;
            titles0.get(0).serial = 0;
=======
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
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
            flag = true;
            return;
        }
        int titleLevel;
<<<<<<< HEAD
        if (para.getStyleID() == null || para.getStyleID().length() != 1)
=======
        if (para.getStyleID().length() != 1)
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
            titleLevel = -1;
        else
            titleLevel = Integer.parseInt(para.getStyleID());
        switch (titleLevel) {
            case 1: {
<<<<<<< HEAD
                infoFill_r(1, para, map);
=======
                if (title1 != null)
                    map.put(title1.title, title1);
                title1 = new RequirementSection();
                level1++; level2 = 0; num1 = 0;
                title1.title = para.getText();
                title1.level = 1;
                title1.serial = level1;
                title0.children.add(title1);
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
                currentLevel = 1;
                break;
            }
            case 2: {
<<<<<<< HEAD
                infoFill_r(2, para, map);
=======
                if (title2 != null)
                    map.put(title2.title, title2);
                title2 = new RequirementSection();
                level2++; level3 = 0; num2 = 0;
                title2.title = para.getText();
                title2.level = 2;
                title2.serial = level2;
                title1.children.add(title2);
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
                currentLevel = 2;
                break;
            }
            case 3: {
<<<<<<< HEAD
                infoFill_r(3, para, map);
=======
                if (title3 != null)
                    map.put(title3.title, title3);
                title3 = new RequirementSection();
                level3++;
                title3.level = 3;
                title3.title = para.getText();
                title3.serial = level3;
                title2.children.add(title3);
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
                currentLevel = 3;
                break;
            }
            default: {
<<<<<<< HEAD
                // non-title: content attribute
                if (currentLevel == 0) {
                    // content between titles0.get(0) and titles0.get(1)
                    titles0.get(0).content.put(String.valueOf(++nums[0]), para.getText());
                } else if (currentLevel == 1) {
                    titles0.get(1).content.put(String.valueOf(++nums[1]), para.getText());
                } else if (currentLevel == 2) {
                    titles0.get(2).content.put(String.valueOf(++nums[2]), para.getText());
                } else if (currentLevel == 3) {
                    if (titleLevel == 4) {
                        // level-4 title content
                        tmpKey = para.getText();
                        tmpVal = "";
                        IBodyElement tmpElement = null;
                        while(bodyElementsIterator.hasNext()) {
                            tmpElement = bodyElementsIterator.next();
                            if(tmpElement instanceof XWPFParagraph) {
                                String styleID = ((XWPFParagraph)(tmpElement)).getStyleID();
                                if(styleID != null && (styleID.equals("1") || styleID.equals("2") || styleID.equals("3") || styleID.equals("4"))) {
                                    titles0.get(3).content.put(tmpKey, tmpVal);
                                    tmpKey = "";
                                    handleParagraph_r(bodyElementsIterator, ((XWPFParagraph)(tmpElement)), map);
                                    break;
                                }
                                else {
                                    tmpVal += (((XWPFParagraph)(tmpElement)).getText() + '\n');
                                }
                            }
                            else if(tmpElement instanceof XWPFTable) {
                                titles0.get(3).content.put(tmpKey, tmpVal);
                                tmpKey = "";
                                handleTable(((XWPFTable)tmpElement));
                                break;
                            }
                        }
                    } 
                    else {
                        // normal text content
                        if(!para.getText().equals("")) titles0.get(3).content.put(String.valueOf(++nums[3]), para.getText());
                    }
                }
            }
        }
    }

    public void handleParagraph_f(Iterator<IBodyElement> bodyElementsIterator, XWPFParagraph para, Map<String, FeatureSection> map) throws JSONException {
        if (para.getText() == null) return;
        // title of document
        if (!flag) {
            titles1.get(0).title = para.getText();
            titles1.get(0).level = 0;
            titles1.get(0).serial = 0;
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
                infoFill_f(1, para, map);
                currentLevel = 1;
                break;
            }
            case 2: {
                infoFill_f(2, para, map);
                currentLevel = 2;
                break;
            }
            case 3: {
                infoFill_f(3, para, map);
                currentLevel = 3;
                break;
            }
            default: {
                // non-title: content attribute
                if (currentLevel == 0) {
                    // content between titles0.get(0) and titles0.get(1)
                    titles1.get(0).content.put(String.valueOf(++nums[0]), para.getText());
                } else if (currentLevel == 1) {
                    titles1.get(1).content.put(String.valueOf(++nums[1]), para.getText());
                } else if (currentLevel == 2) {
                    titles1.get(2).content.put(String.valueOf(++nums[2]), para.getText());
                } else if (currentLevel == 3) {
                    if (titleLevel == 4) {
                        // level-4 title content
                        tmpKey = para.getText();
                        tmpVal = "";
                        IBodyElement tmpElement = null;
                        while(bodyElementsIterator.hasNext()) {
                            tmpElement = bodyElementsIterator.next();
                            if(tmpElement instanceof XWPFParagraph) {
                                String styleID = ((XWPFParagraph)(tmpElement)).getStyleID();
                                if(styleID != null && (styleID.equals("1") || styleID.equals("2") || styleID.equals("3") || styleID.equals("4"))) {
                                    titles1.get(3).content.put(tmpKey, tmpVal);
                                    tmpKey = "";
                                    handleParagraph_f(bodyElementsIterator, ((XWPFParagraph)(tmpElement)), map);
                                    break;
                                }
                                else {
                                    tmpVal += (((XWPFParagraph)(tmpElement)).getText() + '\n');
                                }
                            }
                            else if(tmpElement instanceof XWPFTable) {
                                titles1.get(3).content.put(tmpKey, tmpVal);
                                tmpKey = "";
                                handleTable(((XWPFTable)tmpElement));
                                break;
                            }
                        }
                    }
                    else {
                        // normal text content
                        if(!para.getText().equals("")) titles1.get(3).content.put(String.valueOf(++nums[3]), para.getText());
                    }
=======
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
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
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
<<<<<<< HEAD
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
=======
        if(currentLevel == 0) title0.table.add(ja);
        else if(currentLevel == 1) title1.table.add(ja);
        else if(currentLevel == 2) title2.table.add(ja);
        else if(currentLevel == 3) title3.table.add(ja);
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
    }

    public void parseRequirement(XWPFDocument xd, Map<String, RequirementSection> map) throws JSONException {

        Iterator<IBodyElement> bodyElementsIterator = xd.getBodyElementsIterator();
        while (bodyElementsIterator.hasNext()) {
            IBodyElement bodyElement = bodyElementsIterator.next();
            if(bodyElement instanceof XWPFTable) {
                handleTable(((XWPFTable) (bodyElement)));
            }
            else if(bodyElement instanceof XWPFParagraph) {
<<<<<<< HEAD
                handleParagraph_r(bodyElementsIterator, ((XWPFParagraph) (bodyElement)), map);
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
                handleParagraph_f(bodyElementsIterator, ((XWPFParagraph) (bodyElement)), map);
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

    class RequirementSection {
        long node = -1;
=======
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
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
        String title = "";
        int level = -1;
        int serial = 0;
        JSONObject content = new JSONObject();
<<<<<<< HEAD
        ArrayList<JSONArray> table = new ArrayList<>();
        ArrayList<RequirementSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            if(node != -1) return node;
=======
        ArrayList<JSONArray> table = new ArrayList<JSONArray>();
        List<RequirementSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
            Map<String, Object> map = new HashMap<>();
            map.put(DocxExtractor.TITLE, title);
            map.put(DocxExtractor.LEVEL, level);
            map.put(DocxExtractor.SERIAL, serial);
<<<<<<< HEAD
            map.put(DocxExtractor.CONTENT, content.toString());
            map.put(DocxExtractor.TABLE, table.toString());
            node = inserter.createNode(map, new Label[] { DocxExtractor.RequirementSection });
            for (int i = 0; i < children.size(); i++) {
                RequirementSection child = children.get(i);
                if(child.level == -1) continue;
=======
            map.put(DocxExtractor.CONTENT, content);
            map.put(DocxExtractor.TABLE, table);
            long node = inserter.createNode(map, new Label[] { DocxExtractor.RequirementSection });
            for (int i = 0; i < children.size(); i++) {
                RequirementSection child = children.get(i);
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(DocxExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, DocxExtractor.PARENT, rMap);
            }
            return node;
        }
<<<<<<< HEAD
    }

    class FeatureSection {
        long node = -1;
=======

    }

    class FeatureSection {
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
        String title = "";
        int level = -1;
        int serial = 0;
        JSONObject content = new JSONObject();
<<<<<<< HEAD
        ArrayList<JSONArray> table = new ArrayList<>();
        ArrayList<FeatureSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            if(node != -1) return node;
=======
        ArrayList<JSONArray> table = new ArrayList<JSONArray>();
        List<FeatureSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
            Map<String, Object> map = new HashMap<>();
            map.put(DocxExtractor.TITLE, title);
            map.put(DocxExtractor.LEVEL, level);
            map.put(DocxExtractor.SERIAL, serial);
<<<<<<< HEAD
            map.put(DocxExtractor.CONTENT, content.toString());
            map.put(DocxExtractor.TABLE, table.toString());
            node = inserter.createNode(map, new Label[] { DocxExtractor.FeatureSection });
            for (int i = 0; i < children.size(); i++) {
                FeatureSection child = children.get(i);
                if(child.level == -1) continue;
=======
            map.put(DocxExtractor.CONTENT, content);
            map.put(DocxExtractor.TABLE, table);
            long node = inserter.createNode(map, new Label[] { DocxExtractor.FeatureSection });
            for (int i = 0; i < children.size(); i++) {
                FeatureSection child = children.get(i);
>>>>>>> 1e32a80cc6775e6c82519c0c263d47fb9ec94ff2
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(DocxExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, DocxExtractor.PARENT, rMap);
            }
            return node;
        }
    }
}