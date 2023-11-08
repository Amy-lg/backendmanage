package com.power.service.equipmentservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.PubNetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.mapper.equipmentmapper.PubNetIPMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PubNetIPService extends ServiceImpl<PubNetIPMapper, PubNetIPEntity> {

    /**
     * 数据导入
     * @param file 公网ip拨测excel文件
     * @return
     */
    public String importPubNetIPExcel(MultipartFile file) {

        // 先判断上传的文件是否对应数据库信息
        String originalFilename = file.getOriginalFilename();
        if (originalFilename.contains("公网ip拨测数据")) {
            List<PubNetIPEntity> pubNetIPEntityList = this.importData(file);
            if (pubNetIPEntityList != null) {
                for (PubNetIPEntity pubNetIp : pubNetIPEntityList) {
                    QueryWrapper<PubNetIPEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("project_name", pubNetIp.getProjectName());
                    this.saveOrUpdate(pubNetIp, queryWrapper);
                }
//            this.saveBatch(pubNetIPEntityList, 100);
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
    }


    /**
     * 文件解析
     * @return
     */
    private List<PubNetIPEntity> importData(MultipartFile excelFile) {
        Workbook workbook = AnalysisExcelUtils.isExcelFile(excelFile);
        PubNetIPEntity pubNetIP;
        List<PubNetIPEntity> pubNetIPList = new ArrayList<>();
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
                            if (cell != null) {
                                CellType cellType = cell.getCellType();
                                if (CellType.STRING == cellType) {
                                    cellValue = cell.getStringCellValue();
                                } else if (CellType.BLANK == cellType) {
                                    cellValue = cell.getStringCellValue();
                                    cellNumValue = BigDecimal.valueOf(cell.getNumericCellValue());
                                } else {
                                    cellNumValue = BigDecimal.valueOf(cell.getNumericCellValue());
                                }
                            } else {
                                cellValue = null;
                            }
//                            CellType cellType = cell.getCellType();
//                            if (CellType.STRING == cellType) {
//                                cellValue = cell.getStringCellValue();
//                            } else if (CellType.BLANK == cellType){
//                                cellValue = cell.getStringCellValue();
//                                cellNumValue = BigDecimal.valueOf(cell.getNumericCellValue());
//                            } else {
//                                cellNumValue = BigDecimal.valueOf(cell.getNumericCellValue());
//                            }
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
            QueryWrapper<PubNetIPEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            queryWrapper.eq("project_status", true);

            queryWrapper.eq("task_status", true);
            queryWrapper.eq("dial_result", true);
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
            QueryWrapper<PubNetIPEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            queryWrapper.like("county", county);
            long count = this.count(queryWrapper);
            allCountMap.put(county, count);
        }
        return allCountMap;
    }

    /**
     * 搜索、筛选
     * @param dialFilterQuery
     * @return
     */
    public IPage<PubNetIPEntity> searchOrFilter(DialFilterQuery dialFilterQuery) {
        Integer pageNum = dialFilterQuery.getPageNum();
        Integer pageSize = dialFilterQuery.getPageSize();

        IPage<PubNetIPEntity> pubNetIPEntityPage =  new Page<>(pageNum, pageSize);
        QueryWrapper<PubNetIPEntity> queryWrapper = new QueryWrapper<>();

        // 搜索功能；查看搜索条件是否为空
        String projectName = dialFilterQuery.getProjectName();
        String targetIp = dialFilterQuery.getTargetIp();
        // 搜搜判断
        if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(targetIp)) {
            if (!StrUtil.isEmpty(projectName)) {
                queryWrapper.like("project_name", projectName);
            }
            if (!StrUtil.isEmpty(targetIp)) {
                queryWrapper.like("destination_ip", targetIp);
            }
            IPage<PubNetIPEntity> searchPage = this.page(pubNetIPEntityPage, queryWrapper);
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
            IPage<PubNetIPEntity> filterPage = this.page(pubNetIPEntityPage, queryWrapper);
            return filterPage;
        }
        IPage allPage = this.page(pubNetIPEntityPage);
        return allPage;
    }


    /**
     * 公网IP在线率计算
     * @return
     */
    public List<Long> calculateNetIpRate() {

        List<Long> netIpRateList = new ArrayList<>();
        QueryWrapper<PubNetIPEntity> queryWrapper = new QueryWrapper<>();

        // 分母（任务状态）
        queryWrapper.eq("task_status", true);
        long denominator = this.count(queryWrapper);
        netIpRateList.add(denominator);
        // 分子（拨测状态）
        queryWrapper.eq("dial_result", true);
        long numerator = this.count(queryWrapper);
        netIpRateList.add(numerator);
        return netIpRateList;
    }


}
