package com.power.utils;

import com.power.entity.Exam;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel、Word解析
 * @since 2023/07
 * @author 陈义宽
 */
public class AnalysisExcelUtils {

    /**
     * 解析需要上传的Excel文件
     * @param uploadFile
     * @return List<Exam> 返回解析后数据集合
     */
    public static List<Exam> analysisExcel(MultipartFile uploadFile) {

        // 先判断此文件后缀


        List<Exam> list = new ArrayList<>();
        Exam exam = null;
        try {
            // 获取Excel文件名
            String fileName = uploadFile.getOriginalFilename();
            // 判断文件格式是否正确
            if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
                throw new Exception("上传文件格式不正确，请重新上传！");
            }
            // Excel版本判断
            boolean isExcel2003 = true;
            if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
                isExcel2003 = false;
            }
            Workbook workbook = null;
            InputStream is = null;
            try {
                is = uploadFile.getInputStream();
                if (isExcel2003) {
                    workbook = new HSSFWorkbook(is);
                }else {
                    workbook = new XSSFWorkbook(is);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 获取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet != null) {
                for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row questionRow = sheet.getRow(rowNum);
                    if (questionRow == null) {
                        continue;
                    }
                    exam = new Exam();

                    Cell queContentCol = questionRow.getCell(0);
//                    CellType cellType = queContentCol.getCellType();
                    if (queContentCol == null) {
                        // 报异常信息：导入失败，订单金额格式不正确
                        throw new RuntimeException("导入失败，题目导入类型不正确！");
                    }else {
                        exam.setQuestion(queContentCol.getStringCellValue());
                        rowNum += 1;
                    }

                    Row optionsARow = sheet.getRow(rowNum);
                    Cell optionsACol = optionsARow.getCell(0);
                    if (optionsACol == null) {
                        // 报异常信息：导入失败
                        throw new RuntimeException("导入失败，选项导入类型不正确！");
                    }else {
                        exam.setOptionsA(optionsACol.getStringCellValue());
                        rowNum += 1;
                    }

                    Row optionsBRow = sheet.getRow(rowNum);
                    Cell optionsBCol = optionsBRow.getCell(0);
                    if (optionsBCol == null) {
                        // 报异常信息：导入失败
                        throw new RuntimeException("导入失败，选项导入类型不正确！");
                    }else {
                        exam.setOptionsB(optionsBCol.getStringCellValue());
                        rowNum += 1;
                    }

                    Row optionsCRow = sheet.getRow(rowNum);
                    Cell optionsCCol = optionsCRow.getCell(0);
                    if (optionsCCol == null) {
                        // 报异常信息：导入失败
                        throw new RuntimeException("导入失败，选项导入类型不正确！");
                    }else {
                        exam.setOptionsC(optionsCCol.getStringCellValue());
                        rowNum += 1;
                    }

                    Row optionsDRow = sheet.getRow(rowNum);
                    Cell optionsDCol = optionsDRow.getCell(0);
                    if (optionsDCol == null) {
                        // 报异常信息：导入失败
                        throw new RuntimeException("导入失败，选项导入类型不正确！");
                    }else {
                        exam.setOptionsD(optionsDCol.getStringCellValue());
                        rowNum += 1;
                    }

                    Row answerRow = sheet.getRow(rowNum);
                    Cell answerCol = answerRow.getCell(0);
                    if (answerCol == null) {
                        // 报异常信息：导入失败
                        throw new RuntimeException("导入失败，答案导入类型不正确！");
                    }else {
                        exam.setAnswer(answerCol.getStringCellValue());
                    }

                    // 题目类型
//                    Cell queTypeCol = row.getCell(7);
//                    if (queTypeCol != null) {
//                        exam.setQuestionType(queTypeCol.getCellType() + 1);
//                    }else {
//                        exam.setQuestionType(0);
//                    }

                    // 将导入数据添加到集合
                    list.add(exam);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * Word转Excel
     * @param fileName
     */
    public static void convertWordToExcel(String fileName) {
        File file = new File(fileName);
        FileInputStream fis = null;
        FileOutputStream fos = null;
        XWPFDocument docx = null;
        Workbook excelXlsx = null;
        try {
            fis = new FileInputStream(file);
            // 处理 .docx word的后缀
            docx = new XWPFDocument(fis);
            List<XWPFParagraph> paragraphList = docx.getParagraphs();
            // 写入到 .xlsx 后缀的Excel中
            excelXlsx = new XSSFWorkbook();
            XSSFSheet sheet = (XSSFSheet) excelXlsx.createSheet();
            paragraphList.stream().forEach(paragraph -> {
                // getText()方法是得到所有的文本内容;
                // getParagraphText()是得到每一段的文本内容
                String paragraphText = paragraph.getParagraphText();
                // 这里的rownum需要遍历循环得到
                XSSFRow excelRow = sheet.createRow(paragraphList.indexOf(paragraph));
                XSSFCell cell = excelRow.createCell(0);
                cell.setCellValue(paragraphText);
            });
            // 将文件导出
            String convertFileName = "word转excel";
            fos = new FileOutputStream("D:\\" + convertFileName + ".xlsx");
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
