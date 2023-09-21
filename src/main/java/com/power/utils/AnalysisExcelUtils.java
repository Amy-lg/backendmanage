package com.power.utils;

import com.power.entity.Exam;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.entity.fileentity.TOrderEntity;
import com.power.exception.ServiceException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

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

        if (!uploadFile.isEmpty()) {
            // 判断此文件后缀
            String filename = uploadFile.getOriginalFilename();
            // 如果是word文档，则进行转换
            if (filename.matches("^.+\\.(?i)(docx)$") || filename.matches("^.+\\.(?i)(doc)$")) {
                // Word版本判断
                boolean isWord2003 = true;
                if (filename.matches("^.+\\.(?i)(docx)$")) {
                    isWord2003 =false;
                }
                String cachePath = "./src/main/resources/cache";
                String cacheAbsolutePath = new File(cachePath).getAbsolutePath();
                try {
                    String fullFilePathName = cacheAbsolutePath + "/" + filename;
                    // 将需要转换的word文件保存在此项目中，以便转换时获取绝对路径
                    uploadFile.transferTo(new File(fullFilePathName));
                    Workbook workbook = convertWordToExcel(fullFilePathName, isWord2003);
                    List<Exam> examList = excelDataHandle(workbook);
                    return examList;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (filename.matches("^.+\\.(?i)(xlsx)$") || filename.matches("^.+\\.(?i)(xls)$")){
                // Excel版本判断
                boolean isExcel2003 = true;
                if (filename.matches("^.+\\.(?i)(xlsx)$")) {
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
                List<Exam> examList = excelDataHandle(workbook);
                return examList;
            } else {
                try {
                    throw new Exception("上传文件格式不正确，请重新上传！");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else {
            try {
                throw new Exception("上传文件不能为空！");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 业务工单Excel解析
     * @param orderFile
     * @return
     */
    public static List<BusinessOrderEntity> analysisBusinessOrderExcel(MultipartFile orderFile) {

        if (!orderFile.isEmpty()) {
            String filename = orderFile.getOriginalFilename();
            // 判断导入文件是否为Excel
            if (filename.matches("^.+\\.(?i)(xlsx)$") || filename.matches("^.+\\.(?i)(xls)$")) {
                // 如果是Excel，判断版本
                boolean isExcel2003 = true;
                if (filename.matches("^.+\\.(?i)(xlsx)$")) {
                    isExcel2003 = false;
                }
                Workbook workbook = null;
                InputStream is = null;
                try {
                    is = orderFile.getInputStream();
                    if (isExcel2003) {
                        workbook = new HSSFWorkbook(is);
                    } else {
                        workbook = new XSSFWorkbook(is);
                    }
                    List<BusinessOrderEntity> orderEntityList = businessOrderHandle(workbook);
                    return orderEntityList;
                } catch (IOException e) {
                    throw new ServiceException(5002, "文件处理异常");
                }
            }
            // 导入文件类型错误返回null
            return null;
        }
        return null;
    }


    /**
     * 小T工单Excel解析
     * @param tOrderFile
     * @return
     */
    public static List<TOrderEntity> analysisTOrderExcel(MultipartFile tOrderFile) {

        if (!tOrderFile.isEmpty()) {
            String filename = tOrderFile.getOriginalFilename();
            // 判断导入文件是否为Excel
            if (filename.matches("^.+\\.(?i)(xlsx)$") || filename.matches("^.+\\.(?i)(xls)$")) {
                // 如果是Excel，判断版本
                boolean isExcel2003 = true;
                if (filename.matches("^.+\\.(?i)(xlsx)$")) {
                    isExcel2003 = false;
                }
                Workbook workbook = null;
                InputStream is = null;
                try {
                    is = tOrderFile.getInputStream();
                    if (isExcel2003) {
                        workbook = new HSSFWorkbook(is);
                    } else {
                        workbook = new XSSFWorkbook(is);
                    }
                    List<TOrderEntity> tOrderEntities = tOrderHandle(workbook);
                    return tOrderEntities;
                } catch (IOException e) {
                    throw new ServiceException(5002, "文件处理异常");
                }
            }
            // 导入文件类型错误返回null
            return null;
        }
        return null;
    }


    /**
     * 上传文件格式的通用判断方法
     * @param file 文件
     * @return
     */
    public static Workbook isExcelFile(MultipartFile file) {
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            if (!"".equals(fileName) && fileName.length() != 0) {
                // 判断上传文件的格式是否正确
                boolean isXlsx = fileName.matches("^.+\\.(?i)(xlsx)$");
                boolean isXls = fileName.matches("^.+\\.(?i)(xls)$");
                // Excel2003版本
                boolean isExcel2003 = true;
                if (isXlsx || isXls) {
                    if (isXlsx) {
                        isExcel2003 = false;
                    }
                    Workbook workbook = null;
                    InputStream is = null;
                    try {
                        is = file.getInputStream();
                        if (isExcel2003) {
                            workbook = new HSSFWorkbook(is);
                        } else {
                            workbook = new XSSFWorkbook(is);
                        }
                        return workbook;
                    } catch (IOException e) {
                        throw new ServiceException(5002, "文件处理异常");
                    }
                }
                // 文件类型错误
                throw new ServiceException(5003, "文件类型错误");
            }
        }
        return null;
    }

    /**
     * Word转Excel
     * @param fileName
     */
    private static Workbook convertWordToExcel(String fileName, boolean isWord2003) {
        File file = new File(fileName);
        FileInputStream fis = null;
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
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
        return excelXlsx;
    }


    /**
     * 小T工单Excel解析
     * @param workbook
     * @return
     */
    private static List<TOrderEntity> tOrderHandle(Workbook workbook) {
        List<TOrderEntity> tOrderList = new ArrayList<>();
        TOrderEntity tOrderEntity;

        // 这里需要封装成一个通用解析Excel的方法（减少代码冗余。之后再做！！）
        // todo
        if (workbook != null) {
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    // 这里获取一下excel中每列数据的标题，存储一下
                    List<String> dataTitle = new ArrayList<>();
                    Row titleRow = sheet.getRow(0);
                    for (int j = 0; j < titleRow.getLastCellNum(); j++) {
                        Cell cell = titleRow.getCell(j);
                        String cellValue = cell.getStringCellValue();
                        dataTitle.add(cellValue);
                    }
                    // 获取数据总行数
                    int rowNum = sheet.getLastRowNum();
                    // 第一行为标题，从第二行开始为数据
                    for (int j = 1; j < rowNum; j++) {
                        Row row = sheet.getRow(j);
                        if (row == null) {
                            continue;
                        }
                        tOrderEntity = new TOrderEntity();
                        Iterator<Cell> cellIterator = row.iterator();
                        int titleLoop = 0;
                        while (cellIterator.hasNext()) {
                            Cell nextCell = cellIterator.next();
                            switch (titleLoop) {
                                case 0:
                                    String orderNum = nextCell.getStringCellValue();
                                    tOrderEntity.setOrderNum(orderNum);
                                    titleLoop += 1;
                                    break;
                                case 1:
                                    String orderTheme = nextCell.getStringCellValue();
                                    tOrderEntity.setOrderTheme(orderTheme);
                                    titleLoop += 1;
                                    break;
                                case 2:
                                    String projectNum = nextCell.getStringCellValue();
                                    tOrderEntity.setProjectNum(projectNum);
                                    titleLoop += 1;
                                    break;
                                case 3:
                                    String dispatchOrderTime = nextCell.getStringCellValue();
                                    tOrderEntity.setDispatchOrderTime(dispatchOrderTime);
                                    titleLoop += 1;
                                    break;
                                default:
                                    String orderDuration = nextCell.getStringCellValue();
                                    tOrderEntity.setOrderDuration(orderDuration);
                                    break;
                            }
                        }
                        tOrderList.add(tOrderEntity);
                    }
                    return tOrderList;
                }
                // 防止第一个sheet为空，继续遍历后面的sheet
                continue;
            }
        }
        return null;
    }

    /**
     * 业务工单Excel解析
     * @param workbook
     * @return
     */
    private static List<BusinessOrderEntity> businessOrderHandle(Workbook workbook) {

        List<BusinessOrderEntity> businessOrderList = new ArrayList<>();
        BusinessOrderEntity businessOrder;
        if (workbook != null) {
            // 循环遍历workbook中sheet（sheet≥1）
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {

                    // 这里获取一下excel中每列数据的标题，存储一下
                    List<String> dataTitle = new ArrayList<>();
                    Row titleRow = sheet.getRow(0);
                    for (int j = 0; j < titleRow.getLastCellNum(); j++) {
                        Cell cell = titleRow.getCell(j);
                        String cellValue = cell.getStringCellValue();
                        dataTitle.add(cellValue);
                    }

                    // 获取数据总行数
                    int rowNum = sheet.getLastRowNum();
                    // 第一行为标题，从第二行开始为数据
                    for (int j = 1; j < rowNum; j++) {
                        Row row = sheet.getRow(j);
                        if (row == null && rowNum != 0) {
                            continue;
                        }
                        businessOrder = new BusinessOrderEntity();
                        Iterator<Cell> cellIterator = row.iterator();
                        int titleLoop = 0;
                        while (cellIterator.hasNext()) {
                            Cell nextCell = cellIterator.next();
//                            switch (((Map.ValueIterator) cellIterator).next.key) {
                            switch (titleLoop) {
                                case 0:
                                    String orderNum = nextCell.getStringCellValue();
                                    businessOrder.setOrderNum(orderNum);
                                    titleLoop += 1;
                                    break;
                                case 1:
                                    /*问题描述：使用迭代器遍历，存在空单元格，会出现数据错位现象，导致查询不准确
                                      解决：填补空单元格为null，或使用增强for循环
                                    String county = nextCell.getStringCellValue();
                                    if (county != null) {
                                        businessOrder.setCounty(county);
                                    } else {
                                        businessOrder.setCounty(" ");
                                    }*/
                                    String county = nextCell.getStringCellValue();
                                    businessOrder.setCounty(county);
                                    titleLoop += 1;
                                    break;
                                case 2:
                                    String faultyTime = nextCell.getStringCellValue();
                                    businessOrder.setFaultyTime(faultyTime);
                                    titleLoop += 1;
                                    break;
                                case 3:
                                    String faultyEquipmentType = nextCell.getStringCellValue();
                                    businessOrder.setFaultyEquipmentType(faultyEquipmentType);
                                    titleLoop += 1;
                                    break;
                                case 4:
                                    String networkEleName = nextCell.getStringCellValue();
                                    businessOrder.setNetworkElementName(networkEleName);
                                    titleLoop += 1;
                                    break;
                                case 5:
                                    // 这里需要获取一下单元格中的文本格式是String类型还是数字类型（这里先用String）
                                    String faultyDuration = nextCell.getStringCellValue();
                                    businessOrder.setFaultyDuration(faultyDuration);
                                    titleLoop += 1;
                                    break;
                                case 6:
                                    String faultyCauseCategory = nextCell.getStringCellValue();
                                    businessOrder.setFaultyCauseCategory(faultyCauseCategory);
                                    titleLoop += 1;
                                    break;
                                default:
                                    String faultyTitle = nextCell.getStringCellValue();
                                    businessOrder.setFaultyTitle(faultyTitle);
                                    break;
                            }
                        }
                        businessOrderList.add(businessOrder);
                    }
                    return businessOrderList;
                }
                // 防止第一个sheet为空，继续遍历后面的sheet
                continue;
            }
        }
        return null;
    }

    /**
     * Excel数据处理
     * @param workbook
     * @return
     */
    private static List<Exam> excelDataHandle(Workbook workbook) {

        List<Exam> list = new ArrayList<>();
        Exam exam = null;
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
                if (optionsDRow == null) {
                    exam.setOptionsD(" ");
                    rowNum += 1;
                } else {
                    Cell optionsDCol = optionsDRow.getCell(0);
                    if (optionsDCol == null) {
                        // 报异常信息：导入失败
                        throw new RuntimeException("导入失败，选项导入类型不正确！");
                    }else {
                        exam.setOptionsD(optionsDCol.getStringCellValue());
                        rowNum += 1;
                    }
                }

                Row answerRow = sheet.getRow(rowNum);
                Cell answerCol = answerRow.getCell(0);
                if (answerCol == null) {
                    // 报异常信息：导入失败
                    throw new RuntimeException("导入失败，答案导入类型不正确！");
                }else {
                    exam.setAnswer(answerCol.getStringCellValue());
                }
                // 将导入数据添加到集合
                list.add(exam);
            }
        }
        return list;
    }
}
