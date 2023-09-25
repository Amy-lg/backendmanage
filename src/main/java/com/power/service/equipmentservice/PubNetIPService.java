package com.power.service.equipmentservice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.PubNetIPEntity;
import com.power.mapper.equipmentmapper.PubNetIPMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PubNetIPService extends ServiceImpl<PubNetIPMapper, PubNetIPEntity> {

    /**
     * 数据导入
     * @param file 公网ip拨测excel文件
     * @return
     */
    public String importPubNetIPExcel(MultipartFile file) {

        List<PubNetIPEntity> pubNetIPEntityList = this.importData(file);
        if (pubNetIPEntityList != null) {
            this.saveBatch(pubNetIPEntityList, 100);
            return ResultStatusCode.SUCCESS_UPLOAD.toString();
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 文件解析
     * @return
     */
    private List<PubNetIPEntity> importData(MultipartFile excelFile) {
        Workbook workbook = AnalysisExcelUtils.isExcelFile(excelFile);
        PubNetIPEntity pubNetIP;
        List<PubNetIPEntity> pubNetIPList = new ArrayList<>();;
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
                        pubNetIP = new PubNetIPEntity();
                        String cellValue = null;
                        BigDecimal cellNumValue = null;
                        for (int k = 0; k < lastCellNum; k++) {
                            Cell cell = contentRow.getCell(k);
                            CellType cellType = cell.getCellType();
                            if (CellType.STRING == cellType) {
                                cellValue = cell.getStringCellValue();
                            } else if (CellType.BLANK == cellType){
                                cellValue = cell.getStringCellValue();
                                cellNumValue = BigDecimal.valueOf(cell.getNumericCellValue());
                            } else {
                                cellNumValue = BigDecimal.valueOf(cell.getNumericCellValue());
                            }
                            switch (k) {
                                case 0:
                                    pubNetIP.setProjectName(cellValue);
                                    break;
                                case 1:
                                    pubNetIP.setEquipmentName(cellValue);
                                    break;
                                case 2:
                                    pubNetIP.setCity(cellValue);
                                    break;
                                case 3:
                                    pubNetIP.setCounty(cellValue);
                                    break;
                                case 4:
                                    pubNetIP.setDestinationIp(cellValue);
                                    break;
                                case 5:
                                    pubNetIP.setServePort(cellValue);
                                    break;
                                case 6:
                                    pubNetIP.setDialTime(cellValue);
                                    break;
                                case 7:
                                    pubNetIP.setDialType(cellValue);
                                    break;
                                case 8:
                                    if (cellValue != null && ProStaConstant.NORMAL.equals(cellValue)) {
                                        pubNetIP.setTaskStatus(true);
                                    }else {
                                        pubNetIP.setTaskStatus(false);
                                    }
                                    break;
                                case 9:
                                    if (cellValue != null && ProStaConstant.OPEN.equals(cellValue)) {
                                        pubNetIP.setDialResult(true);
                                    }else {
                                        pubNetIP.setDialResult(false);
                                    }
                                    break;
                                case 10:
                                    pubNetIP.setLossRate(cellValue);
                                    break;
                                case 11:
                                    pubNetIP.setLoadingDelay(cellNumValue);
                                    break;
                                default:
                                    pubNetIP.setShake(cellNumValue.intValue());
                                    break;
                            }
                        }
                        pubNetIP.setProjectStatus(true);
                        pubNetIPList.add(pubNetIP);
                    }
                }
                continue;
            }
            return pubNetIPList;
        }
        return null;
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<PubNetIPEntity> queryPubNetworkIPInfo(Integer pageNum, Integer pageSize) {
        IPage pubNetIpPage = new Page<PubNetIPEntity>(pageNum, pageSize);
//        QueryWrapper<PubNetIPEntity> queryWrapper = new QueryWrapper<>();
        IPage page = this.page(pubNetIpPage);
        return page;
    }
}
