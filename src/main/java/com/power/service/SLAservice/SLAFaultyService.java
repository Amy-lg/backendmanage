package com.power.service.SLAservice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.slaentity.SLAFaultyEntity;
import com.power.mapper.SLAmapper.SLAFaultyMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class SLAFaultyService extends ServiceImpl<SLAFaultyMapper, SLAFaultyEntity> {


    /**
     * 数据导入
     * @param file
     * @return
     */
    public String importSLAFaultyExcel(MultipartFile file) {

        List<SLAFaultyEntity> slaFaultyList = this.importSlaData(file);
        if (slaFaultyList != null) {
            boolean saveBatch = this.saveBatch(slaFaultyList, 200);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
    }


    /**
     * 数据解析
     * @param file
     * @return
     */
    private List<SLAFaultyEntity> importSlaData(MultipartFile file) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
        List<SLAFaultyEntity> slaFaultyEntityList = new ArrayList<>();
        SLAFaultyEntity slaFaulty = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            // 通过反射给属性赋值
            try {
                Class<?> clazz = Class.forName("com.power.entity.slaentity.SLAFaultyEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 通过构造方法实例化对象
                            slaFaulty = (SLAFaultyEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取所有私有属性
                            Field[] slaFaultyFields = clazz.getDeclaredFields();
                            // 获取各个行实例
                            Row contentRow = sheet.getRow(j);
                            // 通过行实例，查看一行存在多少列数据信息，以便于之后遍历
                            short lastCellNum = contentRow.getLastCellNum();
                            String cellValue = null;
                            Iterator<Cell> cellIterator = contentRow.cellIterator();
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                CellType cellType = cell.getCellType();
                                // 利用列索引循环属性赋值（从第3个属性开始）
                                int columnIndex = cell.getColumnIndex() + 2;
                                switch (cellType) {
                                    case STRING:
                                        cellValue = cell.getStringCellValue();
                                        slaFaultyFields[columnIndex].setAccessible(true);
                                        slaFaultyFields[columnIndex].set(slaFaulty, cellValue);
                                        break;
                                    case NUMERIC:
                                        double date = cell.getNumericCellValue();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        // Java在读取Excel单元格中日期格式的内容时，会自动将日期格式转换为数字格式；
                                        // 这里需要将读取到的Excel单元格中的日期格式的数字，转换成日期格式
                                        Date convertDate = DateUtil.getJavaDate(date);
                                        String formatDate = sdf.format(convertDate);
//                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        slaFaultyFields[columnIndex].setAccessible(true);
                                        slaFaultyFields[columnIndex].set(slaFaulty, formatDate);
                                        break;
                                    case BOOLEAN:
                                        slaFaultyFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        slaFaultyFields[columnIndex].set(slaFaulty, cellValue);
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        slaFaultyFields[columnIndex].setAccessible(true);
                                        slaFaultyFields[columnIndex].set(slaFaulty, value);
                                        break;
                                    case BLANK:
                                        slaFaultyFields[columnIndex].setAccessible(true);
                                        cellValue = "";
                                        slaFaultyFields[columnIndex].set(slaFaulty, cellValue);
                                        break;
                                    case ERROR:
                                        byte errorValue = cell.getErrorCellValue();
                                        slaFaultyFields[columnIndex].setAccessible(true);
                                        slaFaultyFields[columnIndex].set(slaFaulty, errorValue);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            slaFaultyEntityList.add(slaFaulty);
                        }
                    }
                    continue;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return slaFaultyEntityList;
        }
        return null;
    }


    /**
     * 查询所有数据信息
     * @param pageNum
     * @param pageSize
     * @param ictNum
     * @return
     */
    public IPage<SLAFaultyEntity> querySLAFaulty(Integer pageNum, Integer pageSize, String ictNum) {

        IPage<SLAFaultyEntity> slaFaultyPage = new Page<>(pageNum, pageSize);
        // 如果有传入ict编号，表示搜索
        if (ictNum != null && StringUtils.hasLength(ictNum)) {
            QueryWrapper<SLAFaultyEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ict_num", ictNum);
            IPage<SLAFaultyEntity> searchPage = this.page(slaFaultyPage, queryWrapper);
            return searchPage;
        } else {
            // 未传入搜索参数，表示显示全部
            IPage page = this.page(slaFaultyPage);
            return page;
        }
    }


    /**
     * 批量删除
     * @param ids
     * @return
     */
    public List<Object> delBatchSLAById(List<Integer> ids) {

        List<Object> delResultSta = new ArrayList<>();
        if (ids != null && ids.size() >= 1) {
            boolean delSta = this.removeBatchByIds(ids);
            if (delSta) {
                delResultSta.add(ResultStatusCode.SUCCESS_DELETE_USER.getCode());
                delResultSta.add(ResultStatusCode.SUCCESS_DELETE_USER.getMsg());
                return delResultSta;
            }
        }
        delResultSta.add(ResultStatusCode.ERROR_DEL_USER_1002.getCode());
        delResultSta.add(ResultStatusCode.ERROR_DEL_USER_1002.getMsg());
        return delResultSta;
    }


    /**
     * 新增SLA数据信息
     * @param slaFaulty
     * @return
     */
    public List<Object> insertSLAInfo(SLAFaultyEntity slaFaulty) {

        List<Object> insertResultSta = new ArrayList<>();
        if (slaFaulty != null) {
            boolean saveRes = this.save(slaFaulty);
            if (saveRes) {
                insertResultSta.add(ResultStatusCode.SUCCESS_INSERT.getCode());
                insertResultSta.add(ResultStatusCode.SUCCESS_INSERT.getMsg());
                return insertResultSta;
            }
        }
        insertResultSta.add(ResultStatusCode.ERROR_UPDATE.getCode());
        insertResultSta.add(ResultStatusCode.ERROR_UPDATE.getMsg());
        return insertResultSta;
    }


    /**
     * 更新数据信息
     * @param slaFaulty
     * @return
     */
    public List<Object> updSLAInfo(SLAFaultyEntity slaFaulty) {

        List<Object> updateResultSta = new ArrayList<>();
        if (slaFaulty != null) {
            boolean saveOrUpdate = this.saveOrUpdate(slaFaulty);
            if (saveOrUpdate) {
                updateResultSta.add(ResultStatusCode.SUCCESS_UPDATE_INFO.getCode());
                updateResultSta.add(ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg());
                return updateResultSta;
            }
        }
        return null;
    }
}
