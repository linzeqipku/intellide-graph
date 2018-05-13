package cn.edu.pku.sei.intellide.graph.extraction.AllData_to_neo4j;

import cn.edu.pku.sei.intellide.graph.extraction.code_mention_detector.CodeMentionDetector;
import cn.edu.pku.sei.intellide.graph.extraction.code_tokenizer.CodeTokenizer;
import cn.edu.pku.sei.intellide.graph.extraction.git_to_neo4j.GitGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.jira_to_neo4j.JiraGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.mail_to_neo4j.MailGraphBuilder;
import cn.edu.pku.sei.intellide.graph.extraction.stackoverflow_to_neo4j.StackOverflowGraphBuilder;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AllDataGraphBuilder {

    public static String GraphDataBaseRoot = "D:\\GraphDataBase\\";
    public static String SourceDirRoot = "E:\\CrawlData\\Apache\\";  //输入服务器数据据的根目录

    @SuppressWarnings({ "resource", "unused" })
    public  ArrayList<ArrayList<String>> xlsx_reader(String excel_url, int ... args) throws IOException {

        //读取xlsx文件
        XSSFWorkbook xssfWorkbook = null;
        //寻找目录读取文件
        File excelFile = new File(excel_url);
        InputStream is = new FileInputStream(excelFile);
        xssfWorkbook = new XSSFWorkbook(is);

        if(xssfWorkbook==null){
            System.out.println("未读取到内容,请检查路径！");
            return null;
        }

        ArrayList<ArrayList<String>> ans=new ArrayList<ArrayList<String>>();
        //遍历xlsx中的sheet
        for (int numSheet = 0; numSheet < xssfWorkbook.getNumberOfSheets(); numSheet++) {
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(numSheet);
            if (xssfSheet == null) {
                continue;
            }
            // 对于每个sheet，读取其中的每一行
            for (int rowNum = 0; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
                XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                if (xssfRow == null) continue;
                ArrayList<String> curarr=new ArrayList<String>();
                for(int columnNum = 0 ; columnNum<args.length ; columnNum++){
                    XSSFCell cell = xssfRow.getCell(args[columnNum]);

                    curarr.add(getValue(cell));
                }
                ans.add(curarr);
            }
        }
        return ans;
    }

    //判断后缀为xlsx的excel文件的数据类
    @SuppressWarnings("deprecation")
    public String getValue(XSSFCell xssfRow) {
        if(xssfRow==null){
            return "---";
        }
        if (xssfRow.getCellType() == xssfRow.CELL_TYPE_BOOLEAN) {
            return String.valueOf(xssfRow.getBooleanCellValue());
        } else if (xssfRow.getCellType() == xssfRow.CELL_TYPE_NUMERIC) {
            double cur=xssfRow.getNumericCellValue();
            long longVal = Math.round(cur);
            Object inputValue = null;
            if(Double.parseDouble(longVal + ".0") == cur)
                inputValue = longVal;
            else
                inputValue = cur;
            return String.valueOf(inputValue);
        } else if(xssfRow.getCellType() == xssfRow.CELL_TYPE_BLANK || xssfRow.getCellType() == xssfRow.CELL_TYPE_ERROR){
            return "---";
        }
        else {
            return String.valueOf(xssfRow.getStringCellValue());
        }
    }

    //判断后缀为xls的excel文件的数据类型
    @SuppressWarnings("deprecation")
    public  String getValue(HSSFCell hssfCell) {
        if(hssfCell==null){
            return "---";
        }
        if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
            double cur=hssfCell.getNumericCellValue();
            long longVal = Math.round(cur);
            Object inputValue = null;
            if(Double.parseDouble(longVal + ".0") == cur)
                inputValue = longVal;
            else
                inputValue = cur;
            return String.valueOf(inputValue);
        } else if(hssfCell.getCellType() == hssfCell.CELL_TYPE_BLANK || hssfCell.getCellType() == hssfCell.CELL_TYPE_ERROR){
            return "---";
        }
        else {
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }

    /*//字符串修剪  去除所有空白符号 ， 问号 ， 中文空格
     public String Trim_str(String str){
        if(str==null)
            return null;
        return str.replaceAll("[\\s\\?]", "").replace("　", "");
    }*/

    public static void main(String[] args) throws IOException, JSONException, GitAPIException, ParseException {

        AllDataGraphBuilder test = new AllDataGraphBuilder();
        ArrayList<ArrayList<String>> list;
        list = test.xlsx_reader("projectPaths2.xlsx",0,1,2,3,4);
        for(int i=1;i<list.size();i++){
            ArrayList<String> row = list.get(i);

            String GraphPath = GraphDataBaseRoot + row.get(0);
            System.out.println(GraphPath);

            String codePath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.GitCrawler\\" + row.get(3)+"\\" + row.get(2) + "\\zip";
            System.out.println(codePath);
            //JavaCodeGraphBuilder.process(GraphPath,codePath);

            String jirePath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.JiraIssueCrawler\\" + row.get(4);
            System.out.println(jirePath);
            //JiraGraphBuilder.process(GraphPath,jirePath);

            String mailPath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.MboxCrawler\\" + row.get(2);
            System.out.println(mailPath);
            //MailGraphBuilder.process(GraphPath,mailPath);

            String gitPath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.GitCrawler\\" + row.get(3) + "\\" + row.get(2) + "\\" + row.get(2)+"GIT"+"\\.git";
            System.out.println(gitPath);
            //GitGraphBuilder.process(GraphPath,gitPath);

            String stackoverflowPath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.StackOverflow\\" + row.get(1);
            System.out.println(stackoverflowPath);
            //StackOverflowGraphBuilder.process(GraphPath,stackoverflowPath);

            //CodeTokenizer.process(GraphPath);
            //CodeMentionDetector.process(GraphPath);



            System.out.println(i);
        }

        /*JavaCodeGraphBuilder.process("F:\\graphData\\graph-isis13","F:\\apache data\\isis\\source code\\core");
        MailGraphBuilder.process("F:\\graphData\\graph-isis13","F:\\apache data\\isis\\email");
        JiraGraphBuilder.process("F:\\graphData\\graph-isis13","F:\\apache data\\isis\\bug report");
        GitGraphBuilder.process("F:\\graphData\\graph-isis13","F:\\gitData\\isis\\.git");
        StackOverflowGraphBuilder.process("F:\\graphData\\graph-isis13","F:\\apache data\\isis\\stackoverflow");
        CodeTokenizer.process("F:\\graphData\\graph-isis13");
        CodeMentionDetector.process("F:\\graphData\\graph-isis13");*/
        System.out.println("OK");
    }
}
