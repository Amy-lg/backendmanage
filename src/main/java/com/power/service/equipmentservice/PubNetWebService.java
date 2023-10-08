package com.power.service.equipmentservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.PubNetWebEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.mapper.equipmentmapper.PubNetWebMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公网WebIP拨测业务层
 * @since 2023/9
 * @author cyk
 */
@Service
public class PubNetWebService extends ServiceImpl<PubNetWebMapper, PubNetWebEntity> {

    /**
     * 数据导入
     * @param file 公网web拨测excel文件
     * @return
     */
    public String importPubNetExcel(MultipartFile file) {

        List<PubNetWebEntity> pubNetWebEntityList = this.importData(file);
        if (pubNetWebEntityList != null) {
            this.saveBatch(pubNetWebEntityList, 100);
            /*for (PubNetWebEntity pubNetWeb : pubNetWebEntityList) {
                this.saveOrUpdate(pubNetWeb);
            }*/
            return ResultStatusCode.SUCCESS_UPLOAD.toString();
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 内网IP表 区县在线总数量
     * @return
     */
    public Map<String, Long> queryAllOnlineCount() {
        Map<String, Long> countMap = new HashMap<>();
        String[] counties = {ProStaConstant.CUSTOMER,ProStaConstant.JIA_HE,ProStaConstant.PING_HU,
                ProStaConstant.JIA_SHAN, ProStaConstant.TONG_XIANG, ProStaConstant.HAI_NING,
                ProStaConstant.HAI_YAN, ProStaConstant.XIU_ZHOU, ProStaConstant.NAN_HU};

        for (int i = 0; i < counties.length; i++) {
            QueryWrapper<PubNetWebEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            queryWrapper.eq("project_status", true);
            queryWrapper.like("county", counties[i]);
            long count = this.count(queryWrapper);
            countMap.put(counties[i], count);
        }
        return countMap;
    }


    /**
     * 内网IP表 区县总数量
     * @return
     */
    public Map<String, Long> queryAllCount() {
        Map<String, Long> allCountMap = new HashMap<>();
        for (String county : ProStaConstant.counties) {
            QueryWrapper<PubNetWebEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            queryWrapper.like("county", county);
            long count = this.count(queryWrapper);
            allCountMap.put(county, count);
        }
        return allCountMap;
    }


    /**
     * 文件解析
     * @return
     */
    private List<PubNetWebEntity> importData(MultipartFile excelFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(excelFile);
        PubNetWebEntity pubNetWeb;
        List<PubNetWebEntity> pubNetWebLists = new ArrayList<>();;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    ArrayList<String> titles = new ArrayList<>();
                    // 标题行
                    Row titleRow = sheet.getRow(0);
                    // excel中数据有多少列
                    short columns = titleRow.getLastCellNum();
                    for (int j = 0; j < columns; j++) {
                        Cell column = titleRow.getCell(j);
                        String titleValue = column.getStringCellValue();
                        titles.add(titleValue);
                    }
                    // 循环遍历数据内容
                    for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                        Row contentRow = sheet.getRow(j);
                        short lastCellNum = contentRow.getLastCellNum();
                        pubNetWeb = new PubNetWebEntity();
                        String cellValue = null;
                        BigDecimal cellNumValue = null;
                        for (int k = 0; k < lastCellNum; k++) {
                            Cell cell = contentRow.getCell(k);
                            CellType cellType = cell.getCellType();
                            if (CellType.STRING == cellType) {
                                cellValue = cell.getStringCellValue();
                            } else {
                                cellNumValue = BigDecimal.valueOf(cell.getNumericCellValue());
                            }
                            switch (k) {
                                case 0:
                                    pubNetWeb.setProjectName(cellValue);
                                    break;
                                case 1:
                                    pubNetWeb.setEquipmentName(cellValue);
                                    break;
                                case 2:
                                    pubNetWeb.setCity(cellValue);
                                    break;
                                case 3:
                                    pubNetWeb.setCounty(cellValue);
                                    break;
                                case 4:
                                    pubNetWeb.setDestinationAddress(cellValue);
                                    break;
                                case 5:
                                    pubNetWeb.setDialTime(cellValue);
                                    break;
                                case 6:
                                    if (cellValue != null && ProStaConstant.NORMAL.equals(cellValue)) {
                                        pubNetWeb.setTaskStatus(true);
                                    }else {
                                        pubNetWeb.setTaskStatus(false);
                                    }
                                    break;
                                case 7:
                                    if (cellValue != null && ProStaConstant.OPEN.equals(cellValue)) {
                                        pubNetWeb.setDialResult(true);
                                    }else {
                                        pubNetWeb.setDialResult(false);
                                    }
                                    break;
                                case 8:
                                    pubNetWeb.setDownloadRate(cellNumValue);
                                    break;
                                case 9:
                                    pubNetWeb.setLoadingDelay(cellNumValue.intValue());
                                    break;
                                case 10:
                                    pubNetWeb.setAccessDelay(cellNumValue.intValue());
                                    break;
                                default:
                                    pubNetWeb.setDnsDelay(cellNumValue.intValue());
                                    break;
                            }
                        }
                        pubNetWeb.setProjectStatus(true);
                        pubNetWebLists.add(pubNetWeb);
                    }
                }
                continue;
            }
            return pubNetWebLists;
        }
        return null;
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<PubNetWebEntity> queryPubNetworkInfo(Integer pageNum, Integer pageSize) {
        IPage pubNetPage = new Page<PubNetWebEntity>(pageNum, pageSize);
//        QueryWrapper<PubNetWebEntity> queryWrapper = new QueryWrapper<>();
        IPage page = this.page(pubNetPage);
        return page;
    }

    /**
     * 搜索、筛选
     * @param dialFilterQuery
     * @return
     */
    public IPage<PubNetWebEntity> searchOrFilter(DialFilterQuery dialFilterQuery) {
        Integer pageNum = dialFilterQuery.getPageNum();
        Integer pageSize = dialFilterQuery.getPageSize();

        IPage<PubNetWebEntity> pubNetWebEntityPage =  new Page<>(pageNum, pageSize);
        QueryWrapper<PubNetWebEntity> queryWrapper = new QueryWrapper<>();

        // 搜索功能；查看搜索条件是否为空
        String projectName = dialFilterQuery.getProjectName();
        String targetAddress = dialFilterQuery.getTargetIp();
        // 搜搜判断
        if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(targetAddress)) {
            if (!StrUtil.isEmpty(projectName)) {
                queryWrapper.like("project_name", projectName);
            }
            if (!StrUtil.isEmpty(targetAddress)) {
                queryWrapper.like("destination_address", targetAddress);
            }
            IPage<PubNetWebEntity> searchPage = this.page(pubNetWebEntityPage, queryWrapper);
            return searchPage;
        }
        // 筛选判断
        String county = dialFilterQuery.getCounty();
        String dialResult = dialFilterQuery.getDialResult();
        String taskResult = dialFilterQuery.getTaskStatus();
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(dialResult) || !StrUtil.isEmpty(taskResult)) {
            if (!StrUtil.isEmpty(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isEmpty(dialResult) && dialResult.equals(ProStaConstant.OPEN)) {
                queryWrapper.eq("dial_result", true);
            }else if (!StrUtil.isEmpty(dialResult) && dialResult.equals(ProStaConstant.CLOSE)){
                queryWrapper.eq("dial_result", false);
            }
            if (!StrUtil.isEmpty(taskResult) && taskResult.equals(ProStaConstant.NORMAL)) {
                queryWrapper.eq("task_status", true);
            } else if (!StrUtil.isEmpty(taskResult) && taskResult.equals(ProStaConstant.STOP)) {
                queryWrapper.eq("task_status", false);
            }
            IPage<PubNetWebEntity> filterPage = this.page(pubNetWebEntityPage, queryWrapper);
            return filterPage;
        }
        IPage allPage = this.page(pubNetWebEntityPage);
        return allPage;

    }
}
