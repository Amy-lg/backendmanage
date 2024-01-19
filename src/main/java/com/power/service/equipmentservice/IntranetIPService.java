package com.power.service.equipmentservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.equipment.IntranetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.mapper.equipmentmapper.IntranetIPMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class IntranetIPService extends ServiceImpl<IntranetIPMapper, IntranetIPEntity> {

    /**
     * 数据导入
     * @param file 内网ip拨测excel文件
     * @return
     */
    public String importIntranetIPExcel(MultipartFile file) {

//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename.contains("拨测任务")) {
//            List<IntranetIPEntity> intranetIPEntityList = this.importData(file);
        List<IntranetIPEntity> intranetIPEntityList = this.importDataByIterator(file);
        if (intranetIPEntityList != null) {
            // 每次导入时需排除重复数据或已导入的数据
            for (IntranetIPEntity intranetIp : intranetIPEntityList) {
                QueryWrapper<IntranetIPEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("target_ip", intranetIp.getTargetIp());
                this.saveOrUpdate(intranetIp, queryWrapper);
            }
            return ResultStatusCode.SUCCESS_UPLOAD.toString();
        }
//      this.saveBatch(intranetIPEntityList, 100);
//        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
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
     * 内网IP表搜索、筛选后导出使用
     * @param dialFilterQuery
     * @return
     */
    public List<IntranetIPEntity> searchOrFilterByExport(DialFilterQuery dialFilterQuery) {

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
            List<IntranetIPEntity> searchList = this.list(queryWrapper);
            return searchList;
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
            List<IntranetIPEntity> filterList = this.list(queryWrapper);
            return filterList;
        }
        List<IntranetIPEntity> allList = this.list();
        return allList;
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
     * 使用迭代器进行数据导入
     * @param excelFile
     * @return
     */
    private List<IntranetIPEntity> importDataByIterator(MultipartFile excelFile) {
        Workbook workbook = AnalysisExcelUtils.isExcelFile(excelFile);
        IntranetIPEntity intranetIP = null;
        List<IntranetIPEntity> intranetIPList = new ArrayList<>();
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                // 通过反射获取私有属性名称
                Class<?> clazz = Class.forName("com.power.entity.equipment.IntranetIPEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 通过构造方法实例化对象
                            intranetIP = (IntranetIPEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取所有私有属性
                            Field[] intranetIpFields = clazz.getDeclaredFields();
                            // 获取属性上的注释信息
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(intranetIpFields);
                            // 获取各个行实例
                            Row contentRow = sheet.getRow(j);
                            String cellValue = null;
                            Iterator<Cell> cellIterator = contentRow.cellIterator();
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                CellType cellType = cell.getCellType();
                                // 利用列索引循环属性赋值（从第3个属性开始）
                                int columnIndex = cell.getColumnIndex() + 2;
                                switch (cellType) {
                                    case STRING:
                                        String title = excelTitle.get(cell.getColumnIndex());
                                        for (int k = 0; k < fieldAnnotationList.size(); k++) {
                                            String fieldAnnotation = fieldAnnotationList.get(k);
                                            if (!"".equals(fieldAnnotation) && fieldAnnotation != null
                                                    && title != null && title.equals(fieldAnnotation)) {
                                                cellValue = cell.getStringCellValue();
                                                intranetIpFields[k + 2].setAccessible(true);
                                                // 拨测状态、任务状态
                                                if (ProStaConstant.NORMAL.equals(cellValue)
                                                        && ProStaConstant.DIAL_STATUS.equals(fieldAnnotation)) {
                                                    intranetIpFields[k + 2].set(intranetIP, true);
                                                } else if (ProStaConstant.NORMAL.equals(cellValue)
                                                        && ProStaConstant.TASK_STATUS.equals(fieldAnnotation)) {
                                                    intranetIpFields[k + 2].set(intranetIP, true);
                                                } else {
                                                    intranetIpFields[k + 2].set(intranetIP, cellValue);
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case NUMERIC:
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        intranetIpFields[columnIndex].setAccessible(true);
                                        intranetIpFields[columnIndex].set(intranetIP, cellValue);
                                        break;
                                    case BOOLEAN:
                                        intranetIpFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        intranetIpFields[columnIndex].setAccessible(true);
                                        intranetIpFields[columnIndex].set(intranetIP, value);
                                        break;
                                    case BLANK:
                                        intranetIpFields[columnIndex].setAccessible(true);
                                        cellValue = "";
                                        break;
                                    case ERROR:
//                                        byte errorCellValue = cell.getErrorCellValue();
                                        intranetIpFields[columnIndex].setAccessible(true);
                                        intranetIpFields[columnIndex].set(intranetIP, false);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            intranetIP.setProjectStatus(true);
                            intranetIPList.add(intranetIP);
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


    /**
     * 删除操作（未使用假删除，操作后数据库信息将直接删除）
     * @param ids 删除数据的id
     * @return
     */
    public List<Object> delBatchByIds(List<Integer> ids) {
        List<Object> delResultSta = new ArrayList<>();
        if (ids != null && ids.size() >= 1) {
            boolean removeStatus = removeBatchByIds(ids);
            if (removeStatus) {
                delResultSta.add(ResultStatusCode.SUCCESS_DELETE_USER.getCode());
                delResultSta.add(ResultStatusCode.SUCCESS_DELETE_USER.getMsg());
                return delResultSta;
            }
        }
        delResultSta.add(ResultStatusCode.ERROR_DEL_USER_1002.getCode());
        delResultSta.add(ResultStatusCode.ERROR_DEL_USER_1002.getMsg());
        return delResultSta;
    }
}
