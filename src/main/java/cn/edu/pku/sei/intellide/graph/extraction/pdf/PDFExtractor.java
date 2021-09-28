package cn.edu.pku.sei.intellide.graph.extraction.pdf;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.PDFTextStripper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PDFExtractor extends KnowledgeExtractor {

    public static final Label GuideSection = Label.label("GuideSection");
    public static final RelationshipType SUB_DOCX_ELEMENT = RelationshipType.withName("subDocxElement");
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String LEVEL = "level";
    public static final String SERIAL = "serial";

    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {

        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[]{"pdf"}, true)) {
            String fileName = file.getAbsolutePath().substring(new File(this.getDataDir()).getAbsolutePath().length()).replaceAll("^[/\\\\]+", "");
            try {
                GuideSection root = new GuideSection();
                root.title = fileName;
                root.level = 0;
                root.serial = 1;
                PDDocument pdf = PDDocument.load(file);
                //获取PDDocumentCatalog文档目录对象
                PDDocumentCatalog catalog = pdf.getDocumentCatalog();
                //获取PDDocumentOutline文档纲要对象
                PDDocumentOutline outline=catalog.getDocumentOutline();
                //获取第一个纲要条目（标题1）
                PDOutlineItem item=outline.getFirstChild();
                if(outline!=null) {
                    while (item != null) {
                        root.children.add(getOutline(item, pdf, 1));
                        item = item.getNextSibling();
                    }
                }
                root.toNeo4j(this.getInserter());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 递归，打印所有子item
    public GuideSection getOutline(PDOutlineItem item, PDDocument pdf, int depth) throws IOException {
        //System.out.println(item.getTitle());
        // 提取文本
        PDFTextStripper textStripper = new PDFTextStripper("GBK");
        PDOutlineItem endBookmark = getEndBookmark(item);

        String start, end = "";
        textStripper.setStartBookmark(item);
        start = item.getTitle();
        start = start.substring(0, start.indexOf(" ")+1);
        if (endBookmark != null) {
            textStripper.setEndBookmark(endBookmark);
            end = endBookmark.getTitle();
            end = end.substring(0, end.indexOf(" ")+1);
        }

        String content = textStripper.getText(pdf);
        int s = content.indexOf(start);
        int e = content.lastIndexOf(end);
        if (s >= 0) {
            if (e > s)  content = content.substring(s, e);
            else content = content.substring(s);
        }


        // 创建对象
        GuideSection guideSection = new GuideSection();
        guideSection.title = item.getTitle();
        guideSection.content = content;
        guideSection.level = depth;


        PDOutlineItem child = item.getFirstChild();
        int serial = 1;
        while (child != null) {
            GuideSection childSection = getOutline(child, pdf, depth+1);
            childSection.serial = serial++;
            guideSection.children.add(childSection);
            child = child.getNextSibling();
        }
        return guideSection;
    }


    public PDOutlineItem getEndBookmark (PDOutlineItem item) {
        PDOutlineItem result = null;
        result = item.getFirstChild();
        if (result != null)     return result;
        result = item.getNextSibling();
        if (result != null)     return result;
        result = ((PDOutlineItem) item.getParent()).getNextSibling();
        return result;
    }


    public static void main(String[] args) {
        PDFExtractor test = new PDFExtractor();
        test.setDataDir("D:/Data/TestData/PDF");
        test.extraction();
    }


    class GuideSection {
        String title = "";
        int level = -1;
        int serial = 0;
        String content = "";
        List<GuideSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            Map<String, Object> map = new HashMap<>();
            map.put(PDFExtractor.TITLE, title);
            map.put(PDFExtractor.LEVEL, level);
            map.put(PDFExtractor.SERIAL, serial);
            map.put(PDFExtractor.CONTENT, content);
            long node = inserter.createNode(map, new Label[] { PDFExtractor.GuideSection });
            for (int i = 0; i < children.size(); i++) {
                GuideSection child = children.get(i);
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(PDFExtractor.SERIAL, i);
                inserter.createRelationship(node, childId, PDFExtractor.SUB_DOCX_ELEMENT, rMap);
            }
            return node;
        }
    }
}
