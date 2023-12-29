package com.power.service.faultservice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.basic.BasicInfoEntity;
import com.power.entity.fault.FaultTrackingEntity;
import com.power.entity.fault.filtersearch.FaultFilterSearch;
import com.power.entity.fault.updateinfo.UpdateFaultTracking;
import com.power.mapper.faultmapper.FaultTrackingMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class FaultTrackingService extends ServiceImpl<FaultTrackingMapper, FaultTrackingEntity> {

    /**
     * 导入故障跟踪信息Excel表
     * @param file
     * @return
     */
    public String importFaultTrackingFile(MultipartFile file) {

        if (!file.isEmpty()) {
            List<FaultTrackingEntity> faultTrackingList = this.importData(file);
            if (faultTrackingList != null && faultTrackingList.size() != 0) {
                for (FaultTrackingEntity fault : faultTrackingList) {
                    this.saveOrUpdate(fault);
                }
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return null;
    }


    /**
     * 数据信息解析
     * @param file
     * @return
     */
    private List<FaultTrackingEntity> importData(MultipartFile file) {

        // 判断文件类型
        Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
        List<FaultTrackingEntity> faultTrackingList = new ArrayList<>();
        FaultTrackingEntity faultTracking = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                // 通过反射获取实体类实例化对象
                Class<?> clazz = Class.forName("com.power.entity.fault.FaultTrackingEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 获取实例对象
                            faultTracking = (FaultTrackingEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取实体类的私有属性
                            Field[] privateFields = clazz.getDeclaredFields();
                            // 通过sheet获取行实例
                            Row contentRow = sheet.getRow(j);
                            // 数据列数
                            short columnNum = contentRow.getLastCellNum();
                            String cellValue = null;
                            Iterator<Cell> cellIterator = contentRow.cellIterator();
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                // 单元格的类型
                                CellType cellType = cell.getCellType();
                                int columnIndex = cell.getColumnIndex() + 2;
                                switch (cellType) {
                                    case STRING:
                                        cellValue = cell.getStringCellValue();
                                        privateFields[columnIndex].setAccessible(true);
                                        privateFields[columnIndex].set(faultTracking, cellValue);
                                        break;
                                    case BLANK:
                                        privateFields[columnIndex].setAccessible(true);
                                        cellValue = "";
                                        privateFields[columnIndex].set(faultTracking, cellValue);
                                        break;
                                    default:
                                        privateFields[columnIndex].setAccessible(true);
                                        cellValue = null;
                                        privateFields[columnIndex].set(faultTracking, cellValue);
                                        break;
                                }
                            }
                            faultTrackingList.add(faultTracking);
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
            return faultTrackingList;
        }
        return null;
    }


    /**
     * 筛选，搜索接口
     * @param faultFilterSearch
     * @return
     */
    public IPage<FaultTrackingEntity> filterSearchByCondition(FaultFilterSearch faultFilterSearch) {
        Integer pageNum = faultFilterSearch.getPageNum();
        Integer pageSize = faultFilterSearch.getPageSize();
        IPage<FaultTrackingEntity> resultPages = new Page<>(pageNum, pageSize);
        QueryWrapper<FaultTrackingEntity> queryWrapper = new QueryWrapper<>();
        // 模糊检索条件
        String projectName = faultFilterSearch.getProjectName();
        String targetIp = faultFilterSearch.getTargetIp();
        if (StringUtils.hasLength(projectName) || StringUtils.hasText(targetIp)) {
            if (projectName != null && !"".equals(projectName)) {
                queryWrapper.like("project_name", projectName);
            }
            if (targetIp != null && !"".equals(targetIp)) {
                queryWrapper.like("target_ip", targetIp);
            }
            IPage<FaultTrackingEntity> searchPage = this.page(resultPages, queryWrapper);
            return searchPage;
        }

        // 筛选
        String projectCounty = faultFilterSearch.getProjectCounty();
        String progressStatus = faultFilterSearch.getProgressStatus();
        if (StringUtils.hasLength(projectCounty) || StringUtils.hasText(progressStatus)) {
            if (projectCounty != null && !"".equals(projectCounty)) {
                queryWrapper.eq("project_county", projectCounty);
            }
            if (progressStatus != null && !"".equals(progressStatus)) {
                queryWrapper.eq("progress_status", progressStatus);
            }
            IPage<FaultTrackingEntity> filterPage = this.page(resultPages, queryWrapper);
            return filterPage;
        }
        // 如果不是筛选 or 检索，检索所有返回
        IPage<FaultTrackingEntity> allPage = this.page(resultPages);
        return allPage;
    }


    /**
     * 更新数据字段信息
     * @param updateFaultTracking
     * @return
     */
    public List<Object> updateFaultInfo(UpdateFaultTracking updateFaultTracking) {

        ArrayList<Object> updFaultList = new ArrayList<>();
        if (updateFaultTracking != null) {
            FaultTrackingEntity selFaultInfoById = getById(updateFaultTracking.getId());
            if (selFaultInfoById != null) {
                // 预计修复日期
                String expRepairDate = updateFaultTracking.getExpRepairDate();
                selFaultInfoById.setExpRepairDate(expRepairDate != null ? expRepairDate :
                        selFaultInfoById.getExpRepairDate());
                // 进度状态
                String progressStatus = updateFaultTracking.getProgressStatus();
                selFaultInfoById.setProgressStatus(progressStatus != null ? progressStatus :
                        selFaultInfoById.getProgressStatus());
                // 备注
                String notes = updateFaultTracking.getNotes();
                selFaultInfoById.setNotes(notes != null ? notes : selFaultInfoById.getNotes());
                // 更新保存
                boolean updateSta = saveOrUpdate(selFaultInfoById);
                if (updateSta) {
                    updFaultList.add(ResultStatusCode.SUCCESS_UPDATE_INFO.getCode());
                    updFaultList.add(ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg());
                    return updFaultList;
                }
            }
        }
        updFaultList.add(ResultStatusCode.ERROR_UPDATE_INFO.getCode());
        updFaultList.add(ResultStatusCode.ERROR_UPDATE_INFO.getMsg());
        return updFaultList;
    }


    /**
     * 县分填充
     * @param basicInfoEntityList 项目信息表数据
     * @return
     */
    public String updateProjectCounty(List<BasicInfoEntity> basicInfoEntityList) {

        // 获取故障追踪数据信息内容
        List<FaultTrackingEntity> faultTrackingEntityList = list();
        if (faultTrackingEntityList.isEmpty() || faultTrackingEntityList == null) {
            return "故障追踪表暂无数据信息";
        }
        for (FaultTrackingEntity fault : faultTrackingEntityList) {
            String faultProjectName = fault.getProjectName();
            for (BasicInfoEntity basicInfo : basicInfoEntityList) {
                String basicProjectName = basicInfo.getIctProjectName();
                if (faultProjectName != null && !"".equals(faultProjectName) &&
                        basicProjectName.equals(faultProjectName)) {
                    QueryWrapper<FaultTrackingEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("project_name", faultProjectName);
                    String basicInfoCounty = basicInfo.getCounty();
                    fault.setProjectCounty(basicInfoCounty);
                    saveOrUpdate(fault, queryWrapper);
                }
            }
        }
        return "故障跟踪区县信息更新成功";
    }
}
