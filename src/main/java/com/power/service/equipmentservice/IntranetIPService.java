package com.power.service.equipmentservice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.IntranetIPEntity;
import com.power.mapper.equipmentmapper.IntranetIPMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class IntranetIPService extends ServiceImpl<IntranetIPMapper, IntranetIPEntity> {

    /**
     * 数据导入
     * @param file 内网ip拨测excel文件
     * @return
     */
    public String importIntranetIPExcel(MultipartFile file) {
        List<IntranetIPEntity> intranetIPEntityList = this.importData(file);
        if (intranetIPEntityList != null) {
            this.saveBatch(intranetIPEntityList, 100);
            return ResultStatusCode.SUCCESS_UPLOAD.toString();
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<IntranetIPEntity> queryIntranetIPInfo(Integer pageNum, Integer pageSize) {
        IPage intranetIpPage = new Page<IntranetIPEntity>(pageNum, pageSize);
        IPage page = this.page(intranetIpPage);
        return page;
    }


    /**
     * 文件解析
     * @return
     */
    private List<IntranetIPEntity> importData(MultipartFile excelFile) {
        Workbook workbook = AnalysisExcelUtils.isExcelFile(excelFile);
        IntranetIPEntity intranetIP;
        List<IntranetIPEntity> intranetIPList = new ArrayList<>();
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    // 获取标题行公用方法(标题暂时没用到，以后可能需要使用)
                    List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                    // 循环遍历数据内容
                    int lastRowNum = sheet.getLastRowNum();
                    for (int j = 1; j <= lastRowNum; j++) {
                        Row contentRow = sheet.getRow(j);
                        // 多少列
                        short lastCellNum = contentRow.getLastCellNum();
                        intranetIP = new IntranetIPEntity();
                        String cellValue = null;
                        for (int k = 0; k < lastCellNum; k++) {
                            Cell cell = contentRow.getCell(k);
                            if (cell != null) {
                                CellType cellType = cell.getCellType();
                                if (CellType.STRING == cellType) {
                                    cellValue = cell.getStringCellValue();
                                } else if (CellType.BLANK == cellType){
                                    cellValue = null;
                                } else {
                                    cellValue = cell.getStringCellValue();
                                }
                            } else {
                                cellValue = null;
                            }
                            switch (k) {
                                case 0:
                                    intranetIP.setSourcePoint(cellValue);
                                    break;
                                case 1:
                                    intranetIP.setSourcePointIp(cellValue);
                                    break;
                                case 2:
                                    intranetIP.setSourcePointCity(cellValue);
                                    break;
                                case 3:
                                    intranetIP.setTargetIp(cellValue);
                                    break;
                                case 4:
                                    intranetIP.setTargetCounty(cellValue);
                                    break;
                                case 5:
                                    intranetIP.setDialMethod(cellValue);
                                    break;
                                case 6:
                                    intranetIP.setTestCycle(cellValue);
                                    break;
                                case 7:
                                    intranetIP.setDialResult(cellValue);
                                    break;
                                case 8:
                                    if (ProStaConstant.NORMAL.equals(cellValue)) {
                                        intranetIP.setDialStatus(true);
                                    }else {
                                        intranetIP.setDialStatus(false);
                                    }
                                    break;
                                case 9:
                                    if (ProStaConstant.NORMAL.equals(cellValue)) {
                                        intranetIP.setTaskStatus(true);
                                    }else {
                                        intranetIP.setTaskStatus(true);
                                    }
                                    break;
                                case 10:
                                    intranetIP.setProjectName(cellValue);
                                    break;
                                case 11:
                                    intranetIP.setSubProjectName(cellValue);
                                    break;
                                case 12:
                                    intranetIP.setDialStartTime(cellValue);
                                    break;
                                case 13:
                                    intranetIP.setDialEndTime(cellValue);
                                    break;
                                default:
                                    intranetIP.setNotes(cellValue);
                                    break;
                            }
                        }
                        intranetIP.setProjectStatus(true);
                        intranetIPList.add(intranetIP);
                    }
                }
                continue;
            }
            return intranetIPList;
        }
        return null;
    }

}
