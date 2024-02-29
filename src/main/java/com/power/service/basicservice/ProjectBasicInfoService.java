package com.power.service.basicservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.basic.BasicInfoEntity;
import com.power.entity.basic.filtersearch.BasicFilterEntity;
import com.power.mapper.basicmapper.ProjectBasicInfoMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 项目基础信息业务逻辑层
 * @author cyk
 * @since 2023/11
 */
@Service
public class ProjectBasicInfoService extends ServiceImpl<ProjectBasicInfoMapper, BasicInfoEntity>{


    /**
     * 数据导入
     * @param file
     * @return
     */
    public String importProjectInfoExcel(MultipartFile file) {

        List<BasicInfoEntity> basicInfoList = this.importProjectInfoData(file);
        if (basicInfoList != null) {
            boolean saveBatch = this.saveBatch(basicInfoList, 200);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 数据解析
     * @param basicInfoFile
     * @return
     */
    private List<BasicInfoEntity> importProjectInfoData(MultipartFile basicInfoFile) {

        // 文件上传类型是否符合excel类型
        Workbook workbook = AnalysisExcelUtils.isExcelFile(basicInfoFile);
        List<BasicInfoEntity> basicInfoEntities = new ArrayList<>();
        BasicInfoEntity basicInfoEntity = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                // 通过反射获取私有属性名称
                Class<?> clazz = Class.forName("com.power.entity.basic.BasicInfoEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 通过构造方法实例化对象
                            basicInfoEntity = (BasicInfoEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取所有私有属性
                            Field[] basicInfoFields = clazz.getDeclaredFields();
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
                                        basicInfoFields[columnIndex].setAccessible(true);
                                        if (cellValue.equals(ProStaConstant.IS_YES)) {
                                            basicInfoFields[columnIndex].set(basicInfoEntity, true);
                                        } else if (cellValue.equals(ProStaConstant.IS_NO)) {
                                            basicInfoFields[columnIndex].set(basicInfoEntity, false);
                                        } else {
                                            basicInfoFields[columnIndex].set(basicInfoEntity, cellValue);
                                        }
                                        break;
                                    case NUMERIC:
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        if ("1.0".equals(cellValue)) {
                                            basicInfoFields[columnIndex].setAccessible(true);
                                            basicInfoFields[columnIndex].set(basicInfoEntity, true);
                                        } else {
                                            basicInfoFields[columnIndex].set(basicInfoEntity, false);
                                        }
                                        break;
                                    case BOOLEAN:
                                        basicInfoFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
//                                        cellValue = String.valueOf(cell.getCellFormula());
//                                        if ("1.0".equals(value)) {
//                                            basicInfoFields[columnIndex].set(basicInfoEntity, true);
//                                        }
                                        basicInfoFields[columnIndex].setAccessible(true);
                                        basicInfoFields[columnIndex].set(basicInfoEntity, value);
                                        break;
                                    case BLANK:
                                        basicInfoFields[columnIndex].setAccessible(true);
                                        cellValue = "";
                                        break;
                                    case ERROR:
//                                        byte errorCellValue = cell.getErrorCellValue();
                                        basicInfoFields[columnIndex].setAccessible(true);
                                        basicInfoFields[columnIndex].set(basicInfoEntity, false);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            basicInfoEntities.add(basicInfoEntity);
                        }
                    }
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return basicInfoEntities;
        }
        return null;
    }


    /**
     * 查询、筛选功能
     * @param basicFilterEntity
     * @return
     */
    public IPage<BasicInfoEntity> filterOrSearchByCondition(BasicFilterEntity basicFilterEntity) {

        Integer pageNum = basicFilterEntity.getPageNum();
        Integer pageSize = basicFilterEntity.getPageSize();

        IPage<BasicInfoEntity> basicInfoPage =  new Page<>(pageNum, pageSize);
        QueryWrapper<BasicInfoEntity> queryWrapper = new QueryWrapper<>();

        // 搜索
        String ictProjectNum = basicFilterEntity.getIctProjectNum();
        if (ictProjectNum != null) {
            queryWrapper.like("ict_project_num", ictProjectNum);
            IPage<BasicInfoEntity> searchPage = this.page(basicInfoPage, queryWrapper);
            return searchPage;
        }

        // 筛选
        String county = basicFilterEntity.getCounty();
        String ictProjectName = basicFilterEntity.getIctProjectName();
        String constructionMode = basicFilterEntity.getConstructionMode();
        String projectStatus = basicFilterEntity.getProjectStatus();
        String integratedTietong = basicFilterEntity.getIntegratedTietong();

        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(ictProjectName) || !StrUtil.isEmpty(constructionMode) ||
                !StrUtil.isEmpty(projectStatus) ||!StrUtil.isEmpty(integratedTietong)) {
            if (!StrUtil.isEmpty(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isEmpty(ictProjectName)) {
                queryWrapper.eq("ict_project_name", ictProjectName);
            }
            if (!StrUtil.isEmpty(constructionMode)){
                queryWrapper.eq("construction_mode", constructionMode);
            }
            if (!StrUtil.isEmpty(projectStatus)) {
                queryWrapper.eq("project_status", projectStatus);
            }
            if (!StrUtil.isEmpty(integratedTietong)) {
                queryWrapper.eq("integrated_tietong", integratedTietong);
            }
            IPage<BasicInfoEntity> filterPage = this.page(basicInfoPage, queryWrapper);
            return filterPage;
        }
        // 查询所有
        IPage allPage = this.page(basicInfoPage);
        return allPage;

    }


    /**
     * 查询区县在维护的项目数量
     * @return
     */
    public Map<String, Object> getMaintenanceNum() {

        QueryWrapper<BasicInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_status", ProStaConstant.PRO_MAINTENANCE);
        // 查询出
        List<BasicInfoEntity> basicInfoList = this.list(queryWrapper);
        String[] counties = ProStaConstant.counties_jx;
        Map<String, Object> countyNumMap = new HashMap<>();
        if (basicInfoList != null && basicInfoList.size() != 0) {
            // 遍历循环区县，计算数量
            for (String county : counties) {
                AtomicInteger countyCount = new AtomicInteger(0);
                basicInfoList.stream().forEach(basicInfo -> {
                    String countyName = basicInfo.getCounty();
                    if (!countyName.isBlank()) {
                        String substringCountyName = countyName.substring(0,2);
                        if (county.equals(substringCountyName)) {
                            switch (substringCountyName) {
                                case ProStaConstant.CUSTOMER :
                                    // 以原子方式将当前值递增1并在递增后返回新值。它相当于i++操作
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.JIA_HE :
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.PING_HU :
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.JIA_SHAN :
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.TONG_XIANG :
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.HAI_NING :
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.HAI_YAN:
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.NAN_HU:
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.XIU_ZHOU:
                                    countyCount.incrementAndGet();
                                    break;
                                case ProStaConstant.JIA_XING:
                                    countyCount.incrementAndGet();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                });
                countyNumMap.put(county, countyCount.intValue());
            }
        }
        return countyNumMap;
    }


    /**
     * 纳管率计算
     * @param county 可变参数，区县名
     * @return
     */
    public List<Long> calculateAcceptRate(String ... county) {

        List<Long> acceptRateList = new ArrayList<>();
        QueryWrapper<BasicInfoEntity> queryWrapper = new QueryWrapper<>();

        // 项目结束时间之前
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentTime = formatter.format(LocalDateTime.now());

        // 根据county是否有值计算区县的纳管率
        if (county != null && county.length != 0) {
            queryWrapper.like("county", county[0]);
        }
        queryWrapper.gt("project_end_date", currentTime);
        // 分母（维护 or 维护+质保）
        queryWrapper.like("maintenance_type", "维护");
        long denominator = this.count(queryWrapper);
        acceptRateList.add(denominator);
        // 分子（是否纳管）
        queryWrapper.eq("is_accept", true);
        long numerator = this.count(queryWrapper);
        acceptRateList.add(numerator);
        return acceptRateList;
    }


}
