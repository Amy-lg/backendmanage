package com.power.service.proactiveservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.proactiveservicesentity.VisitingOrderEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.VisitingFilterSearchEntity;
import com.power.mapper.proactivemapper.VisitingMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
     * @param file
     * @return
     */
    public String importVisitingOrderExcel(MultipartFile file) {

        if (!file.isEmpty()) {
            List<VisitingOrderEntity> visitingOrderList = this.importData(file);
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
        String county = visitingFilterSearch.getCounty();
        String orderStatus = visitingFilterSearch.getOrderStatus();
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
     * 走访工单数据解析
     * @param visitingOrderFile
     * @return
     */
    private List<VisitingOrderEntity> importData(MultipartFile visitingOrderFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(visitingOrderFile);
        VisitingOrderEntity visitingOrder;
        ArrayList<VisitingOrderEntity> visitingOrderList = new ArrayList<>();
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
                        visitingOrderList.add(visitingOrder);
                    }
                }
                continue;
            }
            return visitingOrderList;
        }
        return null;
    }


}