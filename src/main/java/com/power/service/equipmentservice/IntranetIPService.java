package com.power.service.equipmentservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.IntranetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.mapper.equipmentmapper.IntranetIPMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 内网IP表 区县在线总数量
     * @return
     */
    public Map<String, Long> queryAllOnlineCount() {

//        ArrayList<Long> countList = new ArrayList<>();
        Map<String, Long> countMap = new HashMap<>();
        // 存储区县数组（需要遍历）
        String[] counties = {ProStaConstant.CUSTOMER,ProStaConstant.JIA_HE,ProStaConstant.PING_HU,
                ProStaConstant.JIA_SHAN, ProStaConstant.TONG_XIANG, ProStaConstant.HAI_NING,
                ProStaConstant.HAI_YAN, ProStaConstant.XIU_ZHOU, ProStaConstant.NAN_HU};

        for (int i = 0; i < counties.length; i++) {
            QueryWrapper<IntranetIPEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            // 三个条件都满足情况下作为分子，计算在线率
            queryWrapper.eq("project_status", true);
            queryWrapper.eq("task_status", true);
            queryWrapper.eq("dial_status", true);

            queryWrapper.like("target_county", counties[i]);
            long count = this.count(queryWrapper);
            countMap.put(counties[i], count);
//            countList.add(i,count);
        }
//        countList.add(counties);
        return countMap;
    }


    /**
     * 内网IP表 区县总数量
     * @return
     */
    public Map<String, Long> queryAllCount() {
        Map<String, Long> allCountMap = new HashMap<>();
        for (String county : ProStaConstant.counties) {
            QueryWrapper<IntranetIPEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            queryWrapper.like("target_county", county);
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
    public IPage<IntranetIPEntity> searchOrFilter(DialFilterQuery dialFilterQuery) {

        // 取出页码、信息显示条数
        Integer pageNum = dialFilterQuery.getPageNum();
        Integer pageSize = dialFilterQuery.getPageSize();

        IPage<IntranetIPEntity> intranetPage =  new Page<>(pageNum, pageSize);
        QueryWrapper<IntranetIPEntity> queryWrapper = new QueryWrapper<>();

        // 搜索功能；查看搜索条件是否为空
        String projectName = dialFilterQuery.getProjectName();
        String targetIp = dialFilterQuery.getTargetIp();
        // 搜搜判断
        if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(targetIp)) {
            if (!StrUtil.isEmpty(projectName)) {
                queryWrapper.like("project_name", projectName);
            }
            if (!StrUtil.isEmpty(targetIp)) {
                queryWrapper.like("target_ip", targetIp);
            }
            IPage<IntranetIPEntity> searchPage = this.page(intranetPage, queryWrapper);
            return searchPage;
        }
        // 筛选判断
        String county = dialFilterQuery.getCounty();
        String dialResult = dialFilterQuery.getDialResult();
        String taskResult = dialFilterQuery.getTaskStatus();
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(dialResult) || !StrUtil.isEmpty(taskResult)) {
            if (!StrUtil.isEmpty(county)) {
                queryWrapper.eq("target_county", county);
            }
            if (!StrUtil.isEmpty(dialResult) && dialResult.equals(ProStaConstant.OPEN)) {
                queryWrapper.eq("dial_status", true);
            }else if (!StrUtil.isEmpty(dialResult) && dialResult.equals(ProStaConstant.CLOSE)){
                queryWrapper.eq("dial_status", false);
            }
            if (!StrUtil.isEmpty(taskResult) && taskResult.equals(ProStaConstant.NORMAL)) {
                queryWrapper.eq("task_status", true);
            } else if (!StrUtil.isEmpty(taskResult) && taskResult.equals(ProStaConstant.STOP)) {
                queryWrapper.eq("task_status", false);
            }
            IPage<IntranetIPEntity> filterPage = this.page(intranetPage, queryWrapper);
            return filterPage;
        }
        IPage allPage = this.page(intranetPage);
        return allPage;
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


    /**
     * 内网IP在线率计算
     * @return
     */
    public List<Long> calculateIntranetRate() {

        List<Long> intranetIpRateList = new ArrayList<>();
        QueryWrapper<IntranetIPEntity> queryWrapper = new QueryWrapper<>();

        // 分母（任务状态）
        queryWrapper.eq("task_status", true);
        long denominator = this.count(queryWrapper);
        intranetIpRateList.add(denominator);
        // 分子（拨测状态）
        queryWrapper.eq("dial_status", true);
        long numerator = this.count(queryWrapper);
        intranetIpRateList.add(numerator);
        return intranetIpRateList;
    }
}
