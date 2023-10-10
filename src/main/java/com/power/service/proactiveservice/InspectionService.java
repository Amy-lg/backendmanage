package com.power.service.proactiveservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.proactiveservicesentity.InspectionOrderEntity;
import com.power.entity.proactiveservicesentity.VisitingOrderEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.InspectionFilterSearchEntity;
import com.power.mapper.proactivemapper.InspectionMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class InspectionService extends ServiceImpl<InspectionMapper, InspectionOrderEntity> {


    /**
     * 巡检工单数据批量导入
     * @param file
     * @return
     */
    public String importInspectionOrderExcel(MultipartFile file) {
        if (!file.isEmpty()) {
            List<InspectionOrderEntity> inspectionOrderList = this.importData(file);
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
     * 巡检工单数据解析
     * @param inspectionFile
     * @return
     */
    private List<InspectionOrderEntity> importData(MultipartFile inspectionFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(inspectionFile);
        InspectionOrderEntity inspectionOrder;
        ArrayList<InspectionOrderEntity> inspectionOrderList = new ArrayList<>();
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
                        inspectionOrderList.add(inspectionOrder);
                    }
                }
                continue;
            }
            return inspectionOrderList;
        }
        return null;
    }


}
