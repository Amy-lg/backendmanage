package com.power.service.proactiveservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.User;
import com.power.entity.dto.NoteInfoEntity;
import com.power.entity.proactiveservicesentity.VisitingOrderEntity;
import com.power.entity.proactiveservicesentity.ordertimeentity.OrderDealingTimeEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.VisitingFilterSearchEntity;
import com.power.mapper.proactivemapper.VisitingMapper;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.TokenUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 走访工单服务层
 * @author cyk
 * @since 2023/10
 */
@Service
public class VisitingService extends ServiceImpl<VisitingMapper, VisitingOrderEntity> {

    /**
     * 走访工单数据批量导入
     * @param file 走访工单文件
     * @param orderTimeFile 工单处理时间
     * @return
     */
    public String importVisitingOrderExcel(MultipartFile file, MultipartFile orderTimeFile) {

        if (!file.isEmpty() && !orderTimeFile.isEmpty()) {
            List<VisitingOrderEntity> visitingOrderList = this.importData(file, orderTimeFile);
            if (visitingOrderList != null) {
                this.saveBatch(visitingOrderList, 100);
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 搜索、筛选功能（参数中只有pageNum,pageSize时表示分页查询所有）
     * @param visitingFilterSearch
     * @return
     */
    public IPage<VisitingOrderEntity> searchOrFilter(VisitingFilterSearchEntity visitingFilterSearch) {

        Integer pageNum = visitingFilterSearch.getPageNum();
        Integer pageSize = visitingFilterSearch.getPageSize();

        IPage<VisitingOrderEntity> visitingOrderPage =  new Page<>(pageNum, pageSize);
        QueryWrapper<VisitingOrderEntity> queryWrapper = new QueryWrapper<>();
        // 搜索功能；查看搜索条件是否为空
        String orderNum = visitingFilterSearch.getOrderNum();
        String visitingProject = visitingFilterSearch.getVisitingProject();
        String county = visitingFilterSearch.getCounty();
        String orderStatus = visitingFilterSearch.getOrderStatus();

        // 登录者权限
        User currentUser = TokenUtils.getCurrentUser();
        String userRole = currentUser.getRole();
        if (!StrUtil.isBlank(userRole) && ProStaConstant.MANAGER.equals(userRole)) {
            String projectCounty = currentUser.getProjectCounty();
            if (!StrUtil.isEmpty(projectCounty)) {
                queryWrapper.eq("county", projectCounty);
                if (!StrUtil.isEmpty(orderNum) || !StrUtil.isEmpty(visitingProject)) {
                    if (!StrUtil.isBlank(orderNum)) {
                        queryWrapper.like("order_num", orderNum);
                    }
                    if (!StrUtil.isBlank(visitingProject)) {
                        queryWrapper.like("visiting_project", visitingProject);
                    }
                    IPage<VisitingOrderEntity> authoritySearchPage = page(visitingOrderPage, queryWrapper);
                    return authoritySearchPage;
                }
                if (!StrUtil.isEmpty(orderStatus)) {
                    queryWrapper.eq("order_status", orderStatus);
                    IPage<VisitingOrderEntity> authorityFilterPage = page(visitingOrderPage, queryWrapper);
                    return authorityFilterPage;
                }
                IPage authorityPage = this.page(visitingOrderPage, queryWrapper);
                return authorityPage;
            }
        }

        // 搜搜判断
        if (!StrUtil.isEmpty(orderNum) || !StrUtil.isEmpty(visitingProject)) {
            if (!StrUtil.isEmpty(orderNum)) {
                queryWrapper.like("order_num", orderNum);
            }
            if (!StrUtil.isEmpty(visitingProject)) {
                queryWrapper.like("visiting_project", visitingProject);
            }
            IPage<VisitingOrderEntity> searchPage = this.page(visitingOrderPage, queryWrapper);
            return searchPage;
        }

        // 筛选判断
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(orderStatus)) {
            if (!StrUtil.isEmpty(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isEmpty(orderStatus)) {
                queryWrapper.eq("order_status", orderStatus);
            }
            IPage<VisitingOrderEntity> filterPage = this.page(visitingOrderPage, queryWrapper);
            return filterPage;
        }
        IPage allPage = this.page(visitingOrderPage);
        return allPage;
    }


    /**
     * 月份工单处理数量
     * @return
     */
    public List<Object> countOfVisitingOrder() {

        List<Object> resultCountList = new ArrayList<>();
        int[] monthCount = new int[12];
        List<VisitingOrderEntity> visitingOrderList = this.list();
        if (!visitingOrderList.isEmpty() && visitingOrderList.size() != 0) {
            int oldYear = 123; // 先显示2023年
            for (VisitingOrderEntity visitingOrder :
                    visitingOrderList) {
                String dealTime = visitingOrder.getDealTime();
                if (!StrUtil.isBlank(dealTime)) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date parseTime = sdf.parse(dealTime);
//                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//                        dtf.format(dealTime);
//                        TemporalAccessor parse = dtf.parse(dealTime);
                        if (oldYear == parseTime.getYear()) {
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
    public String updateVisitingNote(NoteInfoEntity noteInfoEntity) {
        String orderNum = noteInfoEntity.getOrderNum();
        String note = noteInfoEntity.getNote();
        if (!StrUtil.isEmpty(noteInfoEntity.getOrderNum())) {
            // 通过订单编号查询到
            QueryWrapper<VisitingOrderEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_num", orderNum);
            // 默认为true，查询出多个时，直接抛异常；false，查询出多个结果，选择第一个
            VisitingOrderEntity inspectionOrder = this.getOne(queryWrapper, false);
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
     * 走访工单数据解析
     * @param visitingOrderFile
     * @param orderTimeFile
     * @return
     */
    private List<VisitingOrderEntity> importData(MultipartFile visitingOrderFile, MultipartFile orderTimeFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(visitingOrderFile);
        VisitingOrderEntity visitingOrder;
        ArrayList<VisitingOrderEntity> visitingOrderList = new ArrayList<>();

        // 获取到每个工单的处理时间
        List<OrderDealingTimeEntity> visitingOrderTimeList = this.importVisitingOrderTimeData(orderTimeFile);

        // 工单导入处理
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                    int lastRowNum = sheet.getLastRowNum();
                    for (int j = 1; j <= lastRowNum; j++) {
                        Row contentRow = sheet.getRow(j);
                        // 多少列
                        short lastCellNum = contentRow.getLastCellNum();
                        visitingOrder = new VisitingOrderEntity();
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
                                    visitingOrder.setOrderNum(cellValue);
                                    break;
                                case 1:
                                    visitingOrder.setOrderTheme(cellValue);
                                    break;
                                case 2:
                                    visitingOrder.setVisitingCustomers(cellValue);
                                    break;
                                case 3:
                                    visitingOrder.setVisitingProject(cellValue);
                                    break;
                                case 4:
                                    visitingOrder.setOrderStatus(cellValue);
                                    break;
                                case 5:
                                    visitingOrder.setCity(cellValue);
                                    break;
                                case 6:
                                    visitingOrder.setCounty(cellValue);
                                    break;
                                case 7:
                                    visitingOrder.setVisitingTeam(cellValue);
                                    break;
                                case 8:
                                    visitingOrder.setExecutionTeam(cellValue);
                                    break;
                                case 9:
                                    visitingOrder.setExecutor(cellValue);
                                    break;
                                case 10:
                                    visitingOrder.setTimeoutDuration(cellValue);
                                    break;
                                case 11:
                                    visitingOrder.setCreateDate(cellValue);
                                    break;
                                case 12:
                                    visitingOrder.setEndDate(cellValue);
                                    break;
                                case 13:
                                    visitingOrder.setCreator(cellValue);
                                    break;
                                case 14:
                                    visitingOrder.setCreateDept(cellValue);
                                    break;
                                default:
                                    visitingOrder.setNote(cellValue);
                                    break;
                            }
                        }
                        // 比较两个工单编号，如果工单编号相同，则将时间设置到这个工单编号，否则设置为空
                        for (OrderDealingTimeEntity visitingOrderTime : visitingOrderTimeList) {
                            // 带有时间的工单号
                            String orderDict = visitingOrderTime.getOrderDict();
                            // 原工单编号
                            String orderNum = visitingOrder.getOrderNum();
                            if (orderDict.equals(orderNum)) {
                                String orderDealTime = visitingOrderTime.getOrderDealTime();
                                visitingOrder.setDealTime(orderDealTime);
                            }
                        }
                        visitingOrderList.add(visitingOrder);
                    }
                }
                continue;
            }
            return visitingOrderList;
        }
        return null;
    }


    /**
     * 工单处理时间Excel文件-数据处理
     * @param orderTimeFile
     * @return
     */
    private List<OrderDealingTimeEntity> importVisitingOrderTimeData(MultipartFile orderTimeFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(orderTimeFile);
        OrderDealingTimeEntity visitingOrderTime;
        ArrayList<OrderDealingTimeEntity> visitingOrderTimeList = new ArrayList<>();
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    int lastRowNum = sheet.getLastRowNum();
                    for (int j = 1; j <= lastRowNum; j++) {
                        Row contentRow = sheet.getRow(j);
                        visitingOrderTime = new OrderDealingTimeEntity();
                        String cellValue = null;
                        for (int k = 11; k < 43; k++) {
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
                                case 11:
                                    visitingOrderTime.setOrderDict(cellValue);
                                    k += 30;
                                    break;
                                case 42:
                                    visitingOrderTime.setOrderDealTime(cellValue);
                                    break;
                                default:
                                    break;
                            }
                        }
                        visitingOrderTimeList.add(visitingOrderTime);
                    }
                }
                continue;
            }
            return visitingOrderTimeList;
        }
        return null;
    }


}
