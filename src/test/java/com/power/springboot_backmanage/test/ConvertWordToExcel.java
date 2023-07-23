package com.power.springboot_backmanage.test;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ConvertWordToExcel {
    public static void main(String[] args) {
        File file = new File("C:\\Users\\Amy\\Desktop\\convert.docx");
        FileInputStream fis = null;
        FileOutputStream fos = null;
        XWPFDocument docx = null;
        Workbook excelXlsx = null;
        try {
            fis = new FileInputStream(file);
            // 处理 .docx word的后缀
            docx = new XWPFDocument(fis);
            List<XWPFParagraph> paragraphList = docx.getParagraphs();
//            System.out.println(paragraphList.size());
            // 写入到 .xlsx 后缀的Excel中
            excelXlsx = new XSSFWorkbook();
            XSSFSheet sheet = (XSSFSheet) excelXlsx.createSheet();
            paragraphList.stream().forEach(paragraph -> {
                // getText()方法是得到所有的文本内容;
                // getParagraphText()是得到每一段的文本内容
                String paragraphText = paragraph.getParagraphText();
//                System.out.println(paragraphText);
                // 这里的rownum需要遍历循环得到
                XSSFRow excelRow = sheet.createRow(paragraphList.indexOf(paragraph));
                XSSFCell cell = excelRow.createCell(0);
                cell.setCellValue(paragraphText);
            });
            // 将文件导出
            String fileName = "word转excel";
            fos = new FileOutputStream("D:\\" + fileName + ".xlsx");
            excelXlsx.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null || fos != null) {
                try {
                    fis.close();
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (docx != null) {
                try {
                    docx.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
