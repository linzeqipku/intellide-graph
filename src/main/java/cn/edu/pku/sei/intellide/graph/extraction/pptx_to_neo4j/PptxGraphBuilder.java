package cn.edu.pku.sei.intellide.graph.extraction.pptx_to_neo4j;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xslf.usermodel.*;

import org.neo4j.graphdb.*;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;


public class PptxGraphBuilder {

    public static final Label PPTX = Label.label("Pptx");
    public static final RelationshipType SUB_PPTX_ELEMENT = RelationshipType.withName("subPptxElement");
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String SERIAL_NUMBER = "serialNumber";

    private String dataDir;
    private BatchInserter inserter= null;

    public static void process(String graphDirPath,String dataDir) throws IOException {
        PptxGraphBuilder graphBuilder=new PptxGraphBuilder(graphDirPath,dataDir);
        graphBuilder.extractPptxTrees();
        graphBuilder.inserter.shutdown();
    }

    private PptxGraphBuilder(String graphDirPath, String dataDir) throws IOException {
        this.dataDir=dataDir;
        inserter = BatchInserters.inserter(new File(graphDirPath));
    }

    private void extractPptxTrees() throws IOException {
        Map<File, PptSection> map = new HashMap<>();
        for (File file : FileUtils.listFiles(new File(dataDir), new String[]{"pptx"}, true)) {
            XMLSlideShow pptx = new XMLSlideShow(new FileInputStream(file.getAbsolutePath()));
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
            pptSection.toNeo4j(inserter);
        }
    }

    class PptSection {

        String title = "";
        String content = "";
        List<PptSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            Map<String,Object> map=new HashMap<>();
            map.put(PptxGraphBuilder.TITLE, title);
            map.put(PptxGraphBuilder.CONTENT, content);
            long node = inserter.createNode(map, new Label[]{PptxGraphBuilder.PPTX});
            for (int i=0;i<children.size();i++) {
                PptSection child = children.get(i);
                long childId = child.toNeo4j(inserter);
                Map<String,Object> rMap = new HashMap<>();
                rMap.put(PptxGraphBuilder.SERIAL_NUMBER, i);
                inserter.createRelationship(node, childId, PptxGraphBuilder.SUB_PPTX_ELEMENT, rMap);
            }
            return node;
        }

    }

}
