package cn.edu.pku.sei.intellide.graph.extraction.docx_to_neo4j;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocxGraphBuilder {

    public static final Label DOCX = Label.label("Docx");
    public static final RelationshipType SUB_DOCX_ELEMENT = RelationshipType.withName("subDocxElement");
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String HTML = "html";
    public static final String SERIAL_NUMBER = "serialNumber";

    private String dataDir;
    private BatchInserter inserter= null;

    public static void process(String graphDirPath,String dataDir) throws IOException{
        DocxGraphBuilder graphBuilder=new DocxGraphBuilder(graphDirPath,dataDir);
        graphBuilder.extractDocxTrees();
        graphBuilder.inserter.shutdown();
        System.out.println("docx ok!");
    }

    private DocxGraphBuilder(String graphDirPath, String dataDir) throws IOException {
        this.dataDir=dataDir;
        inserter = BatchInserters.inserter(new File(graphDirPath));
    }

    private void extractDocxTrees() throws IOException{
        Map<File,DocSection> map=new HashMap<>();
        for (File file:FileUtils.listFiles(new File(dataDir),new String[]{"html"},true)){
            String fileName=file.getAbsolutePath().substring(new File(dataDir).getAbsolutePath().length()).replaceAll("^[/\\\\]+","");
            Document doc = Jsoup.parse(FileUtils.readFileToString(file,"utf-8"));
            DocSection root=new DocSection();
            root.title=fileName;
            DocSection currentDocSection=root;
            for (Element element:doc.getElementsByTag("body").first().children()){
                if (!element.tagName().matches("^h\\d+$")){
                    currentDocSection.html+=element.outerHtml();
                }
                else {
                    int depth=Integer.parseInt(element.tagName().substring(1));
                    while (depth<=currentDocSection.depth)
                        currentDocSection=currentDocSection.parent;
                    DocSection newDocSection=new DocSection();
                    newDocSection.depth=depth;
                    newDocSection.title=element.text();
                    newDocSection.html=element.outerHtml();
                    newDocSection.parent=currentDocSection;
                    currentDocSection.children.add(newDocSection);
                    currentDocSection=newDocSection;
                }
            }
            map.put(file,root);
        }
        for (DocSection docSection:map.values())
            docSection.toNeo4j(inserter);
    }

    class DocSection {

        String title="";
        String html="";
        int depth=0;
        DocSection parent=null;
        List<DocSection> children=new ArrayList<>();

        public long toNeo4j(BatchInserter inserter){
            Map<String,Object> map=new HashMap<>();
            map.put(DocxGraphBuilder.TITLE, title);
            map.put(DocxGraphBuilder.HTML, html);
            map.put(DocxGraphBuilder.CONTENT, Jsoup.parse(html).text());
            long node=inserter.createNode(map, new Label[]{DocxGraphBuilder.DOCX});
            for (int i=0;i<children.size();i++) {
                DocSection child=children.get(i);
                long childId=child.toNeo4j(inserter);
                Map<String,Object> rMap=new HashMap<>();
                rMap.put(DocxGraphBuilder.SERIAL_NUMBER,i);
                inserter.createRelationship(node,childId, DocxGraphBuilder.SUB_DOCX_ELEMENT,rMap);
            }
            return node;
        }

    }

    public static void main(String[] args){
        try{
            process("F://graph-tsr4","F://TSR2/htmlSet2");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        //System.out.println("OK");
    }

}