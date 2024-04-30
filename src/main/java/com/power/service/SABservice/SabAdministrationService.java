package com.power.service.SABservice;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.sabentity.SABAdministrationEntity;
import com.power.entity.sabentity.filterseacher.SabParamEntity;
import com.power.mapper.SABmapper.SabAdministrationMapper;
import com.power.utils.AnalysisExcelUtils;
import com.power.vo.sab.SabAdministrationVO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * SAB项目服务层
 * @author cyk
 * @since 2024/4
 */
@Service
public class SabAdministrationService extends ServiceImpl<SabAdministrationMapper, SABAdministrationEntity> {


    /**
     * SAB项目数据导入
     * @param file 数据源文件
     * @return 导入结果信息
     */
    public String importSabAdministrationFile(MultipartFile file) {

        List<SABAdministrationEntity> sabAdministrationList = importSabDataFile(file);
        if (sabAdministrationList != null) {
            boolean saveBatch = this.saveBatch(sabAdministrationList, 200);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
    }


    /**
     * 数据导入处理
     * @param file 源文件
     * @return 返回处理后的List集合
     */
    private List<SABAdministrationEntity> importSabDataFile(MultipartFile file) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
        List<SABAdministrationEntity> sabAdministrationList = new ArrayList<>();
        SABAdministrationEntity sabAdministration = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                Class<?> clazz = Class.forName("com.power.entity.sabentity.SABAdministrationEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            sabAdministration = (SABAdministrationEntity) clazz.getDeclaredConstructor().newInstance();
                            Field[] sabAdministrationFields = clazz.getDeclaredFields();
                            // 获取属性上的注释信息
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(sabAdministrationFields);
                            Row contentRow = sheet.getRow(j);
                            String cellValue = null;
                            Iterator<Cell> cellIterator = contentRow.cellIterator();
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                CellType cellType = cell.getCellType();
                                // 利用列索引循环属性赋值（从第3个属性开始）
                                int columnIndex = cell.getColumnIndex() + 2;
                                switch (cellType) {
                                    case STRING:
                                        String title = excelTitle.get(cell.getColumnIndex());
                                        for (int k = 0; k < fieldAnnotationList.size(); k++) {
                                            String fieldAnnotation = fieldAnnotationList.get(k);
                                            if (!"".equals(fieldAnnotation) && fieldAnnotation != null
                                                    && title != null && title.equals(fieldAnnotation)) {
                                                cellValue = cell.getStringCellValue();
                                                sabAdministrationFields[k + 2].setAccessible(true);
                                                // 是否重点保障项目
                                                if (ProStaConstant.IS_YES.equals(cellValue)) {
                                                    sabAdministrationFields[k + 2].set(sabAdministration, true);
                                                } else if (ProStaConstant.IS_NO.equals(cellValue)) {
                                                    sabAdministrationFields[k + 2].set(sabAdministration, false);
                                                } else {
                                                    sabAdministrationFields[k + 2].set(sabAdministration, cellValue);
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case NUMERIC:
                                        double date = cell.getNumericCellValue();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        // Java在读取Excel单元格中日期格式的内容时，会自动将日期格式转换为数字格式；
                                        // 这里需要将读取到的Excel单元格中的日期格式的数字，转换成日期格式
                                        Date convertDate = DateUtil.getJavaDate(date);
                                        String formatDate = sdf.format(convertDate);
                                        sabAdministrationFields[columnIndex].setAccessible(true);
                                        sabAdministrationFields[columnIndex].set(sabAdministration, formatDate);
                                        break;
                                    case BOOLEAN:
                                        sabAdministrationFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        sabAdministrationFields[columnIndex].set(sabAdministration, cellValue);
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        sabAdministrationFields[columnIndex].setAccessible(true);
                                        sabAdministrationFields[columnIndex].set(sabAdministration, value);
                                        break;
                                    case BLANK:
                                        sabAdministrationFields[columnIndex].setAccessible(true);
                                        sabAdministrationFields[columnIndex].set(sabAdministration, cellValue);
                                        break;
                                    case ERROR:
                                        byte errorValue = cell.getErrorCellValue();
                                        sabAdministrationFields[columnIndex].setAccessible(true);
                                        sabAdministrationFields[columnIndex].set(sabAdministration, errorValue);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            sabAdministrationList.add(sabAdministration);
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
            return sabAdministrationList;
        }
        return null;
    }


    /**
     * 分页展示数据信息
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<SABAdministrationEntity> getSabData(Integer pageNum, Integer pageSize) {
        IPage<SABAdministrationEntity> page = new Page<>(pageNum, pageSize);
        IPage<SABAdministrationEntity> sabAdministrationIPage = this.page(page);

        List<SABAdministrationEntity> sabAdministrationList = list();
        if (sabAdministrationList != null && sabAdministrationList.size() >= 1) {
            for (SABAdministrationEntity sabAdministration : sabAdministrationList) {
                SabAdministrationVO sabAdministrationVO = new SabAdministrationVO();
                BeanUtil.copyProperties(sabAdministration, sabAdministrationVO, false);

            }
        }


        if (sabAdministrationIPage != null) {
            SabAdministrationVO sabAdministrationVO = new SabAdministrationVO();
            BeanUtil.copyProperties(sabAdministrationIPage, sabAdministrationVO, false);
        }
        return sabAdministrationIPage;
    }


    /**
     * 检索、筛选
     * （若没有传检索筛选条件则表示查询全部数据）
     * @param sabParam
     * @return
     */
    public IPage<SABAdministrationEntity> getSabDataInfoByCondition(SabParamEntity sabParam) {

        Integer pageNum = sabParam.getPageNum();
        Integer pageSize = sabParam.getPageSize();

        IPage<SABAdministrationEntity> sabAdministrationPage = new Page<>(pageNum, pageSize);
        QueryWrapper<SABAdministrationEntity> queryWrapper = new QueryWrapper<>();
        // 检索请求
        String ictProjectNum = sabParam.getIctProjectNum();
        String ictProjectName = sabParam.getIctProjectName();
        if (!StrUtil.isEmpty(ictProjectNum) || !StrUtil.isEmpty(ictProjectName)) {
            if (!StrUtil.isBlank(ictProjectNum)) {
                queryWrapper.eq("ict_project_num", ictProjectNum);
            }
            if (!StrUtil.isBlank(ictProjectName)) {
                queryWrapper.like("ict_project_name", ictProjectName);
            }
            IPage<SABAdministrationEntity> searchPage = this.page(sabAdministrationPage, queryWrapper);
            return searchPage;
        }
        // 筛选请求
        String county = sabParam.getCounty();
        String projectLevel = sabParam.getProjectLevel();
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(projectLevel)) {
            if (!StrUtil.isBlank(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isBlank(projectLevel)) {
                queryWrapper.like("project_level", projectLevel);
            }
            IPage<SABAdministrationEntity> filterPage = this.page(sabAdministrationPage, queryWrapper);
            return filterPage;
        }
        // 全部数据返回
        IPage<SABAdministrationEntity> allPage = this.page(sabAdministrationPage, queryWrapper);
        return allPage;
    }


    /**
     * 修改，新增
     * @param sabAdministration
     * @return
     */
    public String updateSabDataByIctNum(SABAdministrationEntity sabAdministration) {

        // 获取ictNum，查询是否存在
        String ictProjectNum = sabAdministration.getIctProjectNum();
        QueryWrapper<SABAdministrationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ict_project_num", ictProjectNum);
        SABAdministrationEntity one = this.getOne(queryWrapper, false);
        if (one == null) {
            saveOrUpdate(sabAdministration);
            return ResultStatusCode.SUCCESS_INSERT.getMsg();
        } else {
            update(sabAdministration, queryWrapper);
            return ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg();
        }
    }


    /**
     * 根据ictNum删除数据
     * @param ictProjectNum
     * @return
     */
    public String delSabDataByIctNum(String ictProjectNum) {

        QueryWrapper<SABAdministrationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ict_project_num", ictProjectNum);
        SABAdministrationEntity sabAdministration = this.getOne(queryWrapper, false);
        if (sabAdministration != null) {
            remove(queryWrapper);
            return ResultStatusCode.SUCCESS_DELETE.getMsg();
        } else {
            return ResultStatusCode.ERROR_DELETE.getMsg();
        }
    }


    /**
     * 在检索和筛选条件一致时，封装共有逻辑（此方法未使用）
     * @param sabParam 共有条件参数体
     * @return
     */
    private IPage<SABAdministrationEntity> searchFilterCommonCode(IPage<SABAdministrationEntity> sabAdministrationPage,
                                                                  QueryWrapper<SABAdministrationEntity> queryWrapper,
                                                                  SabParamEntity sabParam) {

        IPage<SABAdministrationEntity> resultPage = null;
        String ictProjectNum = sabParam.getIctProjectNum();
        String ictProjectName = sabParam.getIctProjectName();
        if (!ictProjectNum.isEmpty() || !ictProjectName.isEmpty()) {
            if (!StrUtil.isEmpty(ictProjectNum)) {
                queryWrapper.eq("ict_project_num", ictProjectNum);
            }
            if (!StrUtil.isEmpty(ictProjectName)) {
                queryWrapper.like("ict_project_name", ictProjectName);
            }
            resultPage = this.page(sabAdministrationPage, queryWrapper);
        }
        return resultPage;
    }


    /**
     * 筛选后导出方法
     * @return
     */
    public List<SABAdministrationEntity> exportFilterResult(SabParamEntity sabParam) {

        QueryWrapper<SABAdministrationEntity> queryWrapper = new QueryWrapper<>();
        String ictProjectNum = sabParam.getIctProjectNum();
        String ictProjectName = sabParam.getIctProjectName();
        if (!StrUtil.isEmpty(ictProjectNum) || !StrUtil.isEmpty(ictProjectName)) {
            if (!StrUtil.isBlank(ictProjectNum)) {
                queryWrapper.eq("ict_project_num", ictProjectNum);
            }
            if (!StrUtil.isBlank(ictProjectName)) {
                queryWrapper.like("ict_project_name", ictProjectName);
            }
            List<SABAdministrationEntity> searchList = this.list();
            return searchList;
        }
        // 筛选请求
        String county = sabParam.getCounty();
        String projectLevel = sabParam.getProjectLevel();
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(projectLevel)) {
            if (!StrUtil.isBlank(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isBlank(projectLevel)) {
                queryWrapper.like("project_level", projectLevel);
            }
            List<SABAdministrationEntity> filterList = this.list();
            return filterList;
        }
        List<SABAdministrationEntity> allList = list();
        return allList;
    }
}
