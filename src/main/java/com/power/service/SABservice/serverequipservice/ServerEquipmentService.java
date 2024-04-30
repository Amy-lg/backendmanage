package com.power.service.SABservice.serverequipservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.sabentity.filterseacher.EquipParamEntity;
import com.power.entity.sabentity.machine.ServerEquipmentEntity;
import com.power.mapper.SABmapper.serverequipmapper.ServerEquipmentMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 机房设备信息服务层
 * @author cyk
 * @since 2024/4
 */
@Service
public class ServerEquipmentService extends ServiceImpl<ServerEquipmentMapper, ServerEquipmentEntity> {

    /**
     * 机房数据Excel文件导入
     * @param file 源文件
     * @return 导入结果
     */
    public String importServerEquipExcel(MultipartFile file) {

        List<ServerEquipmentEntity> serverEquipmentList = importServerEquipData(file);
        if (serverEquipmentList != null) {
            boolean saveBatch = this.saveBatch(serverEquipmentList, 200);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
    }


    /**
     * 数据信息分析
     * @param file
     * @return
     */
    private List<ServerEquipmentEntity> importServerEquipData(MultipartFile file) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
        List<ServerEquipmentEntity> serverEquipmentList = new ArrayList<>();
        ServerEquipmentEntity serverEquipment = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                Class<?> clazz = Class.forName("com.power.entity.sabentity.machine.ServerEquipmentEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            serverEquipment = (ServerEquipmentEntity) clazz.getDeclaredConstructor().newInstance();
                            Field[] serverEquipmentFields = clazz.getDeclaredFields();
                            // 获取属性上的注释信息
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(serverEquipmentFields);
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
                                                serverEquipmentFields[k + 2].setAccessible(true);
                                                serverEquipmentFields[k + 2].set(serverEquipment, cellValue);
                                            }
                                        }
                                        break;
                                    case BLANK:
                                        serverEquipmentFields[columnIndex].setAccessible(true);
                                        serverEquipmentFields[columnIndex].set(serverEquipment, cellValue);
                                        break;
                                    case ERROR:
                                        byte errorValue = cell.getErrorCellValue();
                                        serverEquipmentFields[columnIndex].setAccessible(true);
                                        serverEquipmentFields[columnIndex].set(serverEquipment, errorValue);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            serverEquipmentList.add(serverEquipment);
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
            return serverEquipmentList;
        }
        return null;
    }


    /**
     * 分页显示所有机房设备
     * @return
     */
    public List<ServerEquipmentEntity> selectServeEquipment() {

        return list();
    }


    /**
     * 机房设备检索筛选
     * @param equipParam
     * @return
     */
    public List<ServerEquipmentEntity> getServerEquipmentByCondition(EquipParamEntity equipParam) {

        QueryWrapper<ServerEquipmentEntity> queryWrapper = new QueryWrapper<>();
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
            List<ServerEquipmentEntity> searchList = list(queryWrapper);
            return searchList;
        }
        // 筛选(设备类型筛选)
        String equipmentType = equipParam.getEquipmentType();
        if (!StrUtil.isEmpty(equipmentType)) {
            queryWrapper.eq("equipment_type", equipmentType);
            List<ServerEquipmentEntity> filterList = list(queryWrapper);
            return filterList;
        }
        List<ServerEquipmentEntity> allList = list();
        // 数量计算
        List<ServerEquipmentEntity> serverEquipList = statisticsCount(allList);
        return serverEquipList;
    }


    /**
     * 机房设备新增更新接口
     * @param serverEquipment
     * @return
     */
    public String updateServerEquipInfo(ServerEquipmentEntity serverEquipment) {

        String ictProjectNum = serverEquipment.getIctProjectNum();
        String equipmentType = serverEquipment.getEquipmentType();
        if (!StrUtil.isEmpty(ictProjectNum) && !StrUtil.isEmpty(equipmentType)) {
            QueryWrapper<ServerEquipmentEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ict_project_num",ictProjectNum);
            queryWrapper.eq("equipment_type",equipmentType);
            ServerEquipmentEntity serverEquipmentOne = this.getOne(queryWrapper, false);
            // 更新
            if (serverEquipmentOne == null) {
                saveOrUpdate(serverEquipment);
                return ResultStatusCode.SUCCESS_INSERT.getMsg();
            } else {
                update(serverEquipment, queryWrapper);
                return ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg();
            }
        }
        return null;
    }


    /**
     * 机房服务设备删除
     * @param ictProjectNum
     * @param equipmentType
     * @return
     */
    public String delServerEquipByIctNumAndType(String ictProjectNum, String equipmentType) {
        // 先检索
        QueryWrapper<ServerEquipmentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ict_project_num", ictProjectNum);
        queryWrapper.eq("equipment_type", equipmentType);
        ServerEquipmentEntity serverEquipment = getOne(queryWrapper, false);
        if (serverEquipment != null) {
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
    private List<ServerEquipmentEntity> statisticsCount(List<ServerEquipmentEntity> frontList) {

        List<ServerEquipmentEntity> operatedList = new ArrayList<>();
        // 获取到所有前端设备数据信息集合，通过 项目编号+设备类型 确定一条数据信息，统计这条数据出现的次数
        if (frontList != null && frontList.size() >= 1) {
            for (ServerEquipmentEntity serverEquipment : frontList) {
                // 查询相同数据数量问题
                QueryWrapper<ServerEquipmentEntity> queryWrapper = new QueryWrapper<>();
                // 获取项目编号
                String ictProjectNum = serverEquipment.getIctProjectNum();
                String equipmentType = serverEquipment.getEquipmentType();
                if (!StrUtil.isEmpty(ictProjectNum)) {
                    queryWrapper.eq("ict_project_num", ictProjectNum);
                    if (StrUtil.isBlank(equipmentType)) {
                        queryWrapper.eq("equipment_type", "");
                    }
                    queryWrapper.eq("equipment_type", equipmentType);
                    // 查询数量
                    long frontEquipCount = this.count(queryWrapper);
                    serverEquipment.setEquipmentCount(String.valueOf(frontEquipCount));
                    // todo
                    // 删除frontList中的此项数据，提高检索效率
                    // frontList.remove();
                }
                operatedList.add(serverEquipment);
            }
        }
        return operatedList;
    }
}
