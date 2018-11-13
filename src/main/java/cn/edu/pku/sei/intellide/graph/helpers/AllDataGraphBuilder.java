package cn.edu.pku.sei.intellide.graph.helpers;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class AllDataGraphBuilder {

    public static String GraphDataBaseRoot = "F:\\Apache\\GraphDataBase1\\";
    public static String SourceDirRoot = "J:\\Apache\\";  //输入服务器数据据的根目录

    public static void main(String[] args) throws IOException, JSONException, GitAPIException, ParseException {

        /*AllDataGraphBuilder test = new AllDataGraphBuilder();
        ArrayList<ArrayList<String>> list;
        list = test.xlsx_reader("projectPaths-final.xlsx",0,1,2,3,4,5);
        JSONArray projectJson = new JSONArray();
        int i1=0,i2=0,i3=0,i4=0,i5=0;
        for(int i=1;i<list.size();i++) {
            ArrayList<String> row = list.get(i);
            String name = row.get(0);
            String description = row.get(5);
            JSONObject project = new JSONObject();
            project.put("name", name);
            project.put("description", description);
            projectJson.put(i, project);

           *//*String GraphPath = GraphDataBaseRoot + "Graph-" + row.get(0);
            //System.out.println(GraphPath);


            String codePath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.GitCrawler\\" + row.get(3)+"\\" + row.get(0).toLowerCase() + "\\zip";
            String jirePath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.JiraIssueCrawler\\" + row.get(4);
            String mailPath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.MboxCrawler\\" + row.get(2);
            String gitPath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.GitCrawler\\" + row.get(3) + "\\" + row.get(0).toLowerCase() + "\\" + row.get(0).toLowerCase()+"GIT"+"\\.git";
            String stackoverflowPath = SourceDirRoot + "cn.edu.pku.EOSCN.crawler.StackOverflow\\projects\\" + row.get(1);
            *//**//*System.out.println(codePath);
            System.out.println(jirePath);
            System.out.println(mailPath);
            System.out.println(gitPath);
            System.out.println(stackoverflowPath);*//**//*

            if(codePath.contains("---")){
                GraphPath = GraphPath + "-srcmiss";
                i1++;
            }
            if(jirePath.contains("---")){
                GraphPath = GraphPath + "-jiremiss";
                i2++;
            }
            if(mailPath.contains("---")){
                GraphPath = GraphPath + "-mailmiss";
                i3++;
            }
            if(gitPath.contains("---")){
                GraphPath = GraphPath + "-gitmiss";
                i4++;
            }
            if(stackoverflowPath.contains("---")){
                GraphPath = GraphPath +"-sofmiss";
                i5++;
            }
            System.out.println(GraphPath);

            if(!codePath.contains("---")){
                JavaExtractor.process(GraphPath,codePath);
            }
            if(!jirePath.contains("---")){
                JiraExtractor.process(GraphPath,jirePath);
            }
            if(!mailPath.contains("---")){
                MailExtractor.process(GraphPath,mailPath);
            }
            if(!gitPath.contains("---")){
                GitExtractor.process(GraphPath,gitPath);
            }
            if(!stackoverflowPath.contains("---")){
                StackOverflowExtractor.process(GraphPath,stackoverflowPath);
            }
            if(!GraphPath.contains("srcmiss")){
                TokenExtractor.process(GraphPath);
                CodeMentionExtractor.process(GraphPath);
                System.out.println("Perfect Done !");
            }*//*

            System.out.println(i);

        }
        //System.out.println(i1+" "+i2+" "+i3+" "+i4+" "+i5);
        FileWriter fw = new FileWriter("project-final.json");
        PrintWriter out = new PrintWriter(fw);
        out.write(projectJson.toString());
        out.println();
        fw.close();
        out.close();
        System.out.println("OK");*/


        File file = new File("F:\\Apache\\GraphDataBase");

        File[] fileList = file.listFiles();
        JSONArray projectnamneJson = new JSONArray();

        for (int i = 0; i < fileList.length; i++) {

            if (fileList[i].isDirectory()) {
                String fileName = fileList[i].getName();
                JSONObject project = new JSONObject();
                project.put("name", fileName);
                projectnamneJson.put(i, project);
                System.out.println("目录：" + fileName);
            }
        }
        FileWriter fw = new FileWriter("project-name.json");
        PrintWriter out = new PrintWriter(fw);
        out.write(projectnamneJson.toString());
        out.println();
        fw.close();
        out.close();
    }

    @SuppressWarnings({"resource", "unused"})
    public ArrayList<ArrayList<String>> xlsx_reader(String excel_url, int... args) throws IOException {

        //读取xlsx文件
        XSSFWorkbook xssfWorkbook = null;
        //寻找目录读取文件
        File excelFile = new File(excel_url);
        InputStream is = new FileInputStream(excelFile);
        xssfWorkbook = new XSSFWorkbook(is);

        if (xssfWorkbook == null) {
            System.out.println("未读取到内容,请检查路径！");
            return null;
        }

        ArrayList<ArrayList<String>> ans = new ArrayList<ArrayList<String>>();
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
                ArrayList<String> curarr = new ArrayList<String>();
                for (int columnNum = 0; columnNum < args.length; columnNum++) {
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
        if (xssfRow == null) {
            return "---";
        }
        if (xssfRow.getCellType() == xssfRow.CELL_TYPE_BOOLEAN) {
            return String.valueOf(xssfRow.getBooleanCellValue());
        } else if (xssfRow.getCellType() == xssfRow.CELL_TYPE_NUMERIC) {
            double cur = xssfRow.getNumericCellValue();
            long longVal = Math.round(cur);
            Object inputValue = null;
            if (Double.parseDouble(longVal + ".0") == cur)
                inputValue = longVal;
            else
                inputValue = cur;
            return String.valueOf(inputValue);
        } else if (xssfRow.getCellType() == xssfRow.CELL_TYPE_BLANK || xssfRow.getCellType() == xssfRow.CELL_TYPE_ERROR) {
            return "---";
        } else {
            return String.valueOf(xssfRow.getStringCellValue());
        }
    }

    /*//字符串修剪  去除所有空白符号 ， 问号 ， 中文空格
     public String Trim_str(String str){
        if(str==null)
            return null;
        return str.replaceAll("[\\s\\?]", "").replace("　", "");
    }*/

    //判断后缀为xls的excel文件的数据类型
    @SuppressWarnings("deprecation")
    public String getValue(HSSFCell hssfCell) {
        if (hssfCell == null) {
            return "---";
        }
        if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
            double cur = hssfCell.getNumericCellValue();
            long longVal = Math.round(cur);
            Object inputValue = null;
            if (Double.parseDouble(longVal + ".0") == cur)
                inputValue = longVal;
            else
                inputValue = cur;
            return String.valueOf(inputValue);
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BLANK || hssfCell.getCellType() == hssfCell.CELL_TYPE_ERROR) {
            return "---";
        } else {
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }
}

