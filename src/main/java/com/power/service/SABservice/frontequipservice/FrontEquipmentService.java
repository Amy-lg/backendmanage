package com.power.service.SABservice.frontequipservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.sabentity.filterseacher.EquipParamEntity;
import com.power.entity.sabentity.headend.FrontEquipmentEntity;
import com.power.mapper.SABmapper.frontequipmapper.FrontEquipmentMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class FrontEquipmentService extends ServiceImpl<FrontEquipmentMapper, FrontEquipmentEntity> {


    /**
     * 前端设备信息导入分析
     * @param file 源文件
     * @return
     */
    public String importFrontEquipExcel(MultipartFile file) {

        List<FrontEquipmentEntity> frontEquipmentList = importFrontEquipData(file);
        if (frontEquipmentList != null) {
            boolean saveBatch = this.saveBatch(frontEquipmentList, 1000);
            if (saveBatch) {
                // 导入完成后计算数量并更新
                List<FrontEquipmentEntity> frontEquipStatistics = statisticsCount(frontEquipmentList);
                saveOrUpdateBatch(frontEquipStatistics, 1000);
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
    }


    /**
     * 导入数据解析
     * @param file Excel源文件
     * @return
     */
    private List<FrontEquipmentEntity> importFrontEquipData(MultipartFile file) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
        List<FrontEquipmentEntity> frontEquipmentList = new ArrayList<>();
        FrontEquipmentEntity frontEquipment = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                Class<?> clazz = Class.forName("com.power.entity.sabentity.headend.FrontEquipmentEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            frontEquipment = (FrontEquipmentEntity) clazz.getDeclaredConstructor().newInstance();
                            Field[] frontEquipmentFields = clazz.getDeclaredFields();
                            // 获取属性上的注释信息
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(frontEquipmentFields);
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
                                                frontEquipmentFields[k + 2].setAccessible(true);
                                                frontEquipmentFields[k + 2].set(frontEquipment, cellValue);
                                            }
                                        }
                                        break;
                                    case BLANK:
                                        frontEquipmentFields[columnIndex].setAccessible(true);
                                        frontEquipmentFields[columnIndex].set(frontEquipment, cellValue);
                                        break;
                                    case ERROR:
                                        byte errorValue = cell.getErrorCellValue();
                                        frontEquipmentFields[columnIndex].setAccessible(true);
                                        frontEquipmentFields[columnIndex].set(frontEquipment, errorValue);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            frontEquipmentList.add(frontEquipment);
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
            return frontEquipmentList;
        }
        return null;
    }


    /**
     * 分页显示所有前端设备
     * @return
     */
    public List<FrontEquipmentEntity> selectFrontEquipment() {

        List<FrontEquipmentEntity> frontEquipmentList = this.list();

        return frontEquipmentList;
    }


    /**
     * 前端设备筛选检索
     * @param equipParam
     * @return
     */
    public List<FrontEquipmentEntity> getFrontEquipmentByCondition(EquipParamEntity equipParam) {

        QueryWrapper<FrontEquipmentEntity> queryWrapper = new QueryWrapper<>();
        // 检索
        String ictProjectNum = equipParam.getIctProjectNum();
        String ictProjectName = equipParam.getIctProjectName();
        if (!StrUtil.isEmpty(ictProjectNum) || !StrUtil.isEmpty(ictProjectName)) {
            if (!StrUtil.isBlank(ictProjectNum)) {
                queryWrapper.eq("ict_project_num", ictProjectNum);
            }
            if (!StrUtil.isBlank(ictProjectName)) {
                queryWrapper.like("ict_project_name", ictProjectName);
            }
            List<FrontEquipmentEntity> searchList = list(queryWrapper);
            return searchList;
        }
        // 筛选(设备类型筛选)
        String equipmentType = equipParam.getEquipmentType();
        if (!StrUtil.isEmpty(equipmentType)) {
            queryWrapper.eq("equipment_type", equipmentType);
            List<FrontEquipmentEntity> filterList = list(queryWrapper);
            return filterList;
        }
        List<FrontEquipmentEntity> allList = list();
        // 数量统计
        // List<FrontEquipmentEntity> frontEquipStatistics = statisticsCount(allList);
        return allList;
    }


    /**
     * 前端设备新增，更新功能接口
     * @param frontEquipment 更新内容
     * @return
     */
    public String updateFrontEquipInfo(FrontEquipmentEntity frontEquipment) {

        String ictProjectNum = frontEquipment.getIctProjectNum();
        String equipmentType = frontEquipment.getEquipmentType();
        if (!StrUtil.isEmpty(ictProjectNum) && !StrUtil.isEmpty(equipmentType)) {
            QueryWrapper<FrontEquipmentEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ict_project_num",ictProjectNum);
            queryWrapper.eq("equipment_type",equipmentType);
            FrontEquipmentEntity frontEquipmentOne = this.getOne(queryWrapper, false);
            // 更新
            if (frontEquipmentOne == null) {
                saveOrUpdate(frontEquipment);
                return ResultStatusCode.SUCCESS_INSERT.getMsg();
            } else {
                update(frontEquipment, queryWrapper);
                return ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg();
            }
        }
        return null;
    }


    /**
     * 删除
     * @param ictProjectNum
     * @param equipmentType
     * @return
     */
    public String delEquipByIctNumAndType(String ictProjectNum, String equipmentType) {

        // 先检索
        QueryWrapper<FrontEquipmentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ict_project_num", ictProjectNum);
        queryWrapper.eq("equipment_type", equipmentType);
        FrontEquipmentEntity frontEquipment = getOne(queryWrapper, false);
        if (frontEquipment != null) {
            remove(queryWrapper);
            return ResultStatusCode.SUCCESS_DELETE.getMsg();
        }
        return ResultStatusCode.ERROR_DELETE.getMsg();
    }


    /**
     * 数量统计
     * @param frontList
     * @return
     */
    private List<FrontEquipmentEntity> statisticsCount(List<FrontEquipmentEntity> frontList) {

        ArrayList<FrontEquipmentEntity> operatedList = new ArrayList<>();
        // 获取到所有前端设备数据信息集合，通过 项目编号+设备类型 确定一条数据信息，统计这条数据出现的次数
        if (frontList != null && frontList.size() >= 1) {
            for (FrontEquipmentEntity frontEquipment : frontList) {
                // 查询相同数据数量问题
                QueryWrapper<FrontEquipmentEntity> queryWrapper = new QueryWrapper<>();
                // 获取项目编号
                String ictProjectNum = frontEquipment.getIctProjectNum();
                String equipmentType = frontEquipment.getEquipmentType();
                if (!StrUtil.isEmpty(ictProjectNum)) {
                    queryWrapper.eq("ict_project_num", ictProjectNum);
                    if (StrUtil.isBlank(equipmentType)) {
                        queryWrapper.eq("equipment_type", "");
                    }
                    queryWrapper.eq("equipment_type", equipmentType);
                    // 查询数量
                    long frontEquipCount = this.count(queryWrapper);
                    frontEquipment.setEquipmentCount(String.valueOf(frontEquipCount));
                    // todo
                    // 删除frontList中的此项数据，提高检索效率
                    // frontList.remove();
                }
                operatedList.add(frontEquipment);
            }
        }
        return operatedList;
    }


}
