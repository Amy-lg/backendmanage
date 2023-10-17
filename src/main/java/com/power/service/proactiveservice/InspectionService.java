package com.power.service.proactiveservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.dto.NoteInfoEntity;
import com.power.entity.proactiveservicesentity.InspectionOrderEntity;
import com.power.entity.proactiveservicesentity.ordertimeentity.OrderDealingTimeEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.InspectionFilterSearchEntity;
import com.power.mapper.proactivemapper.InspectionMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class InspectionService extends ServiceImpl<InspectionMapper, InspectionOrderEntity> {


    /**
     * 巡检工单数据批量导入
     * @param file
     * @return
     */
    public String importInspectionOrderExcel(MultipartFile file, MultipartFile orderTimeFile) {
        if (!file.isEmpty() && !orderTimeFile.isEmpty()) {
            List<InspectionOrderEntity> inspectionOrderList = this.importData(file, orderTimeFile);
            if (inspectionOrderList != null) {
                this.saveBatch(inspectionOrderList, 100);
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 搜索、筛选
     * @param inspectionFilterSearch
     * @return
     */
    public IPage<InspectionOrderEntity> searchOrFilter(InspectionFilterSearchEntity inspectionFilterSearch) {

        Integer pageNum = inspectionFilterSearch.getPageNum();
        Integer pageSize = inspectionFilterSearch.getPageSize();

        IPage<InspectionOrderEntity> inspectionOrderPage =  new Page<>(pageNum, pageSize);
        QueryWrapper<InspectionOrderEntity> queryWrapper = new QueryWrapper<>();

        String orderNum = inspectionFilterSearch.getOrderNum();
        String inspectionProject = inspectionFilterSearch.getInspectionProject();
        // 搜搜判断
        if (!StrUtil.isEmpty(orderNum) || !StrUtil.isEmpty(inspectionProject)) {
            if (!StrUtil.isEmpty(orderNum)) {
                queryWrapper.like("order_num", orderNum);
            }
            if (!StrUtil.isEmpty(inspectionProject)) {
                queryWrapper.like("inspection_project", inspectionProject);
            }
            IPage<InspectionOrderEntity> searchPage = this.page(inspectionOrderPage, queryWrapper);
            return searchPage;
        }

        String county = inspectionFilterSearch.getCounty();
        String orderStatus = inspectionFilterSearch.getOrderStatus();
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(orderStatus)) {
            if (!StrUtil.isEmpty(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isEmpty(orderStatus)) {
                queryWrapper.eq("order_status", orderStatus);
            }
            IPage<InspectionOrderEntity> filterPage = this.page(inspectionOrderPage, queryWrapper);
            return filterPage;
        }
        IPage allPage = this.page(inspectionOrderPage);
        return allPage;
    }


    /**
     * 巡检工单月份处理数量统计
     * @return
     */
    public List<Object> countOfInspectionOrder() {

        List<Object> resultCountList = new ArrayList<>();
        int[] monthCount = new int[12];
        List<InspectionOrderEntity> inspectionOrderList = this.list();
        if (!inspectionOrderList.isEmpty() && inspectionOrderList.size() != 0) {
            int oldYear = 0;
            for (InspectionOrderEntity inspectOrder : inspectionOrderList) {
                String dealTime = inspectOrder.getDealTime();
                if (!StrUtil.isBlank(dealTime)) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date parseTime = sdf.parse(dealTime);
                        if (oldYear == 0 || oldYear == parseTime.getYear()) {
                            oldYear = parseTime.getYear();
                            int month = parseTime.getMonth() + 1;
                            switch (month) {
                                case ProStaConstant.JANUARY:
                                    monthCount[0]++;
                                    break;
                                case ProStaConstant.FEBRUARY:
                                    monthCount[1]++;
                                    break;
                                case ProStaConstant.MARCH:
                                    monthCount[2]++;
                                    break;
                                case ProStaConstant.APRIL:
                                    monthCount[3]++;
                                    break;
                                case ProStaConstant.MAY:
                                    monthCount[4]++;
                                    break;
                                case ProStaConstant.JUNE:
                                    monthCount[5]++;
                                    break;
                                case ProStaConstant.JULY:
                                    monthCount[6]++;
                                    break;
                                case ProStaConstant.AUGUST:
                                    monthCount[7]++;
                                    break;
                                case ProStaConstant.SEPTEMBER:
                                    monthCount[8]++;
                                    break;
                                case ProStaConstant.OCTOBER:
                                    monthCount[9]++;
                                    break;
                                case ProStaConstant.NOVEMBER:
                                    monthCount[10]++;
                                    break;
                                default:
                                    monthCount[11]++;
                                    break;
                            }
                        }
                        // 年份不相等，另作处理
                        // todo
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        resultCountList.add(monthCount);
        return resultCountList;
    }


    /**
     * 保存备注信息
     * @param noteInfoEntity
     * @return
     */
    public String updateNote(NoteInfoEntity noteInfoEntity) {

        String orderNum = noteInfoEntity.getOrderNum();
        String note = noteInfoEntity.getNote();
        if (!StrUtil.isEmpty(orderNum)) {
            // 通过订单编号查询到
            QueryWrapper<InspectionOrderEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_num", orderNum);
            // 默认为true，查询出多个时，直接抛异常；false，查询出多个结果，选择第一个
            InspectionOrderEntity inspectionOrder = this.getOne(queryWrapper, false);
            if (!StrUtil.isBlank(note)) {
                inspectionOrder.setNote(note);
                boolean saveResult = this.saveOrUpdate(inspectionOrder);
                if (saveResult) {
                    return ResultStatusCode.SUCCESS_UPDATE.getMsg();
                }
            }
            inspectionOrder.setNote(note);
            this.saveOrUpdate(inspectionOrder);
            return ResultStatusCode.SUCCESS_EMPTY.getMsg();
        }
        return null;
    }

    /**
     * 巡检工单数据解析
     * @param inspectionFile
     * @return
     */
    private List<InspectionOrderEntity> importData(MultipartFile inspectionFile, MultipartFile orderTimeFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(inspectionFile);
        InspectionOrderEntity inspectionOrder;
        ArrayList<InspectionOrderEntity> inspectionOrderList = new ArrayList<>();

        // 获取到每个工单的处理时间
        List<OrderDealingTimeEntity> inspectionOrderTimeList = this.importInspectionOrderTimeData(orderTimeFile);

        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                    int lastRowNum = sheet.getLastRowNum();
                    for (int j = 1; j <= lastRowNum; j++) {
                        Row contentRow = sheet.getRow(j);
                        short lastCellNum = contentRow.getLastCellNum();
                        inspectionOrder = new InspectionOrderEntity();
                        String cellValue = null;
                        for (int k = 0; k < lastCellNum; k++) {
                            Cell cell = contentRow.getCell(k);
                            if (cell != null) {
                                CellType cellType = cell.getCellType();
                                if (CellType.STRING == cellType) {
                                    cellValue = cell.getStringCellValue();
                                } else if (CellType.BLANK == cellType){
                                    cellValue = null;
                                }else {
                                    cellValue = null;
                                }
                            }else {
                                cellValue = null;
                            }
                            switch (k) {
                                case 0:
                                    inspectionOrder.setOrderNum(cellValue);
                                    break;
                                case 1:
                                    inspectionOrder.setOrderTheme(cellValue);
                                    break;
                                case 2:
                                    inspectionOrder.setInspectionCustomers(cellValue);
                                    break;
                                case 3:
                                    inspectionOrder.setOrderStatus(cellValue);
                                    break;
                                case 4:
                                    inspectionOrder.setCity(cellValue);
                                    break;
                                case 5:
                                    inspectionOrder.setCounty(cellValue);
                                    break;
                                case 6:
                                    inspectionOrder.setInspectionProject(cellValue);
                                    k += 4;
                                    break;
                                case 11:
                                    inspectionOrder.setCreateDate(cellValue);
                                    break;
                                case 12:
                                    inspectionOrder.setEndDate(cellValue);
                                    k += 1;
                                    break;
                                default:
                                    inspectionOrder.setNote(cellValue);
                                    break;
                            }
                        }
                        if (inspectionOrderTimeList != null) {
                            for (OrderDealingTimeEntity inspectionOrderTime : inspectionOrderTimeList) {
                                String orderDict = inspectionOrderTime.getOrderDict();
                                // 原工单编号
                                String orderNum = inspectionOrder.getOrderNum();
                                if (orderDict.equals(orderNum)) {
                                    String orderDealTime = inspectionOrderTime.getOrderDealTime();
                                    inspectionOrder.setDealTime(orderDealTime);
                                }
                            }
                        }
                        inspectionOrderList.add(inspectionOrder);
                    }
                }
                continue;
            }
            return inspectionOrderList;
        }
        return null;
    }


    /**
     * 巡检工单处理时间统计
     * @param orderTimeFile
     * @return
     */
    private List<OrderDealingTimeEntity> importInspectionOrderTimeData(MultipartFile orderTimeFile) {
        Workbook workbook = AnalysisExcelUtils.isExcelFile(orderTimeFile);
        OrderDealingTimeEntity inspectionOrderTime;
        ArrayList<OrderDealingTimeEntity> inspectionOrderTimeList = new ArrayList<>();
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    // 获取excel数据标题(之后通过封装方法使用)
                    List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);

                    int lastRowNum = sheet.getLastRowNum();
                    for (int j = 1; j <= lastRowNum; j++) {
                        Row contentRow = sheet.getRow(j);

                        // 一行数据存在多少列
                        short lastCellNum = contentRow.getLastCellNum();

                        inspectionOrderTime = new OrderDealingTimeEntity();
                        String cellValue = null;
                        for (int k = 12; k < 33; k++) {
                            Cell cell = contentRow.getCell(k);
                            if (cell != null) {
                                CellType cellType = cell.getCellType();
                                if (CellType.STRING == cellType) {
                                    cellValue = cell.getStringCellValue();
                                } else if (CellType.BLANK == cellType){
                                    cellValue = null;
                                }else {
                                    cellValue = null;
                                }
                            }else {
                                cellValue = null;
                            }
                            switch (k) {
                                case 12:
                                    inspectionOrderTime.setOrderDict(cellValue);
                                    k += 19;
                                    break;
                                case 32:
                                    inspectionOrderTime.setOrderDealTime(cellValue);
                                    break;
                                default:
                                    break;
                            }
                        }
                        inspectionOrderTimeList.add(inspectionOrderTime);
                    }
                }
                continue;
            }
            return inspectionOrderTimeList;
        }
        return null;
    }


}
