package cn.edu.pku.sei.intellide.graph.extraction.html;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlExtractor extends KnowledgeExtractor {

    public static final Label DOCX = Label.label("Docx");
    public static final RelationshipType SUB_DOCX_ELEMENT = RelationshipType.withName("subDocxElement");
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String HTML = "html";
    public static final String SERIAL_NUMBER = "serialNumber";

    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {
        Map<File, DocSection> map = new HashMap<>();
        for (File file : FileUtils.listFiles(new File(this.getDataDir()), new String[]{"html"}, true)) {
            String fileName = file.getAbsolutePath().substring(new File(this.getDataDir()).getAbsolutePath().length()).replaceAll("^[/\\\\]+", "");
            Document doc = null;
            try {
                doc = Jsoup.parse(FileUtils.readFileToString(file, "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            DocSection root = new DocSection();
            root.title = fileName;
            DocSection currentDocSection = root;
            for (Element element : doc.getElementsByTag("body").first().children()) {
                if (!element.tagName().matches("^h\\d+$")) {
                    currentDocSection.html += element.outerHtml();
                } else {
                    int depth = Integer.parseInt(element.tagName().substring(1));
                    while (depth <= currentDocSection.depth)
                        currentDocSection = currentDocSection.parent;
                    DocSection newDocSection = new DocSection();
                    newDocSection.depth = depth;
                    newDocSection.title = element.text();
                    newDocSection.html = element.outerHtml();
                    newDocSection.parent = currentDocSection;
                    currentDocSection.children.add(newDocSection);
                    currentDocSection = newDocSection;
                }
            }
            map.put(file, root);
        }
        for (DocSection docSection : map.values())
            docSection.toNeo4j(this.getInserter());
    }

    class DocSection {

        String title = "";
        String html = "";
        int depth = 0;
        DocSection parent = null;
        List<DocSection> children = new ArrayList<>();

        public long toNeo4j(BatchInserter inserter) {
            Map<String, Object> map = new HashMap<>();
            map.put(HtmlExtractor.TITLE, title);
            map.put(HtmlExtractor.HTML, html);
            map.put(HtmlExtractor.CONTENT, Jsoup.parse(html).text());
            long node = inserter.createNode(map, new Label[]{HtmlExtractor.DOCX});
            for (int i = 0; i < children.size(); i++) {
                DocSection child = children.get(i);
                long childId = child.toNeo4j(inserter);
                Map<String, Object> rMap = new HashMap<>();
                rMap.put(HtmlExtractor.SERIAL_NUMBER, i);
                inserter.createRelationship(node, childId, HtmlExtractor.SUB_DOCX_ELEMENT, rMap);
            }
            return node;
        }

    }

}