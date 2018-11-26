package cn.edu.pku.sei.intellide.graph.extraction.pptx;


import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PptxExtractor extends KnowledgeExtractor {

    public static final Label PPTX = Label.label("Pptx");
    public static final RelationshipType SUB_PPTX_ELEMENT = RelationshipType.withName("subPptxElement");
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String SERIAL_NUMBER = "serialNumber";

    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {
        Map<File, PptSection> map = new HashMap<>();
        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[]{"pptx"}, true)) {
            XMLSlideShow pptx = null;
            try {
                pptx = new XMLSlideShow(new FileInputStream(file.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            PptSection root = new PptSection();
            root.title = file.getName();
            for (XSLFSlide slide : pptx.getSlides()) {
                PptSection newPptSection = new PptSection();
                for (XSLFShape shape : slide) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape txShape = (XSLFTextShape) shape;
                        String txType = txShape.getShapeName();
                        if (txType.contains("标题") | txType.contains("TITLE")) {
                            newPptSection.title += txShape.getText();
                        } else if (txType.contains("内容") | txType.contains("CONTENT")) {
                            newPptSection.content += txShape.getText();
                        } else {
                            continue;
                        }
                    }
                }
                root.children.add(newPptSection);
            }
            map.put(file, root);
        }
        for (PptSection pptSection : map.values()) {
            pptSection.toNeo4j(this.getInserter());
        }
    }

    class PptSection {

        String title = "";
        String content = "";
        List<PptSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            Map<String, Object> map = new HashMap<>();
            map.put(PptxExtractor.TITLE, title);
            map.put(PptxExtractor.CONTENT, content);
            long node = inserter.createNode(map, new Label[]{PptxExtractor.PPTX});
            for (int i = 0; i < children.size(); i++) {
                PptSection child = children.get(i);
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(PptxExtractor.SERIAL_NUMBER, i);
                inserter.createRelationship(node, childId, PptxExtractor.SUB_PPTX_ELEMENT, rMap);
            }
            return node;
        }

    }

}
