package com.power.service.equipmentservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.User;
import com.power.entity.equipment.PubNetWebEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.mapper.equipmentmapper.PubNetWebMapper;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.TokenUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

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

        String originalFilename = file.getOriginalFilename();
        if (originalFilename.contains("公网web拨测数据")) {
            List<PubNetWebEntity> pubNetWebEntityList = this.importDataByIterator(file);
            if (pubNetWebEntityList != null) {
//            this.saveBatch(pubNetWebEntityList, 100);
                for (PubNetWebEntity pubNetWeb : pubNetWebEntityList) {
                    QueryWrapper<PubNetWebEntity> queryWrapper = new QueryWrapper<>();
                    // 使用 项目名称+web链接 确定数据的唯一性
                    queryWrapper.eq("project_name", pubNetWeb.getProjectName());
                    queryWrapper.eq("destination_address", pubNetWeb.getDestinationAddress());
                    this.saveOrUpdate(pubNetWeb, queryWrapper);
                }
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
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
//                                case 8:
//                                    pubNetWeb.setDownloadRate(cellNumValue);
//                                    break;
//                                case 9:
//                                    pubNetWeb.setLoadingDelay(cellNumValue.intValue());
//                                    break;
//                                case 10:
//                                    pubNetWeb.setAccessDelay(cellNumValue.intValue());
//                                    break;
//                                default:
//                                    pubNetWeb.setDnsDelay(cellNumValue.intValue());
//                                    break;
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
     * 使用迭代器进行数据导入
     * @param excelFile
     * @return
     */
    private List<PubNetWebEntity> importDataByIterator(MultipartFile excelFile) {
        Workbook workbook = AnalysisExcelUtils.isExcelFile(excelFile);
        PubNetWebEntity pubNetWeb = null;
        List<PubNetWebEntity> pubNetWebList = new ArrayList<>();
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                // 通过反射获取私有属性名称
                Class<?> clazz = Class.forName("com.power.entity.equipment.PubNetWebEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 通过构造方法实例化对象
                            pubNetWeb = (PubNetWebEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取所有私有属性
                            Field[] pubNetWebFields = clazz.getDeclaredFields();
                            // 获取属性上的注释信息
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(pubNetWebFields);
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
                                                pubNetWebFields[k + 2].setAccessible(true);
                                                // 拨测状态、任务状态
                                                if (ProStaConstant.NORMAL.equals(cellValue)
                                                        && ProStaConstant.TASK_STATUS.equals(fieldAnnotation)) {
                                                    pubNetWebFields[k + 2].set(pubNetWeb, true);
                                                } else if (ProStaConstant.OPEN.equals(cellValue)
                                                        && ProStaConstant.DIAL_RESULT.equals(fieldAnnotation)) {
                                                    pubNetWebFields[k + 2].set(pubNetWeb, true);
                                                } else {
                                                    pubNetWebFields[k + 2].set(pubNetWeb, cellValue);
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case NUMERIC:
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        pubNetWebFields[cell.getColumnIndex() - 2].setAccessible(true);
                                        pubNetWebFields[cell.getColumnIndex() - 2].set(pubNetWeb, cellValue);
                                        break;
                                    case BOOLEAN:
                                        pubNetWebFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        pubNetWebFields[columnIndex].setAccessible(true);
                                        pubNetWebFields[columnIndex].set(pubNetWeb, value);
                                        break;
                                    case BLANK:
                                        pubNetWebFields[columnIndex].setAccessible(true);
                                        cellValue = "";
                                        break;
                                    case ERROR:
//                                        byte errorCellValue = cell.getErrorCellValue();
                                        pubNetWebFields[columnIndex].setAccessible(true);
                                        pubNetWebFields[columnIndex].set(pubNetWeb, false);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            pubNetWeb.setProjectStatus(true);
                            pubNetWebList.add(pubNetWeb);
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
            return pubNetWebList;
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

        String county = dialFilterQuery.getCounty();
        String dialResult = dialFilterQuery.getDialResult();
        String taskResult = dialFilterQuery.getTaskStatus();
        // 管理员权限
        User currentUser = TokenUtils.getCurrentUser();
        String userRole = currentUser.getRole();
        if (!StrUtil.isBlank(userRole) && ProStaConstant.MANAGER.equals(userRole)) {
            String projectCounty = currentUser.getProjectCounty();
            if (!StrUtil.isEmpty(projectCounty)){
                queryWrapper.eq("county", projectCounty);
                if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(targetAddress)) {
                    if (!StrUtil.isEmpty(projectName)) {
                        queryWrapper.like("project_name", projectName);
                    }
                    if (!StrUtil.isEmpty(targetAddress)) {
                        queryWrapper.like("destination_address", targetAddress);
                    }
                    IPage<PubNetWebEntity> authoritySearchPage = page(pubNetWebEntityPage, queryWrapper);
                    return authoritySearchPage;
                }
                if (!StrUtil.isEmpty(dialResult) || !StrUtil.isEmpty(taskResult)) {
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
                    IPage<PubNetWebEntity> authorityFilterPage = page(pubNetWebEntityPage, queryWrapper);
                    return authorityFilterPage;
                }
                IPage authorityPage = page(pubNetWebEntityPage, queryWrapper);
                return authorityPage;
            }
        }

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


    /**
     * 公网web在线率计算
     * @return
     */
    public List<Long> calculateNetWebRate() {

        List<Long> netWebRateList = new ArrayList<>();
        QueryWrapper<PubNetWebEntity> queryWrapper = new QueryWrapper<>();

        // 分母（任务状态）
        queryWrapper.eq("task_status", true);
        long denominator = this.count(queryWrapper);
        netWebRateList.add(denominator);
        // 分子（拨测状态）
        queryWrapper.eq("dial_result", true);
        long numerator = this.count(queryWrapper);
        netWebRateList.add(numerator);
        return netWebRateList;
    }


    /**
     * 搜索、筛选后导出使用
     * @param dialFilterQuery
     * @return
     */
    public List<PubNetWebEntity> searchOrFilterByExport(DialFilterQuery dialFilterQuery) {

        QueryWrapper<PubNetWebEntity> queryWrapper = new QueryWrapper<>();
        // 搜索功能；查看搜索条件是否为空
        String projectName = dialFilterQuery.getProjectName();
        String targetAddress = dialFilterQuery.getTargetIp();

        String county = dialFilterQuery.getCounty();
        String dialResult = dialFilterQuery.getDialResult();
        String taskResult = dialFilterQuery.getTaskStatus();

        // 管理员权限
        User currentUser = TokenUtils.getCurrentUser();
        String userRole = currentUser.getRole();
        if (!StrUtil.isBlank(userRole) && ProStaConstant.MANAGER.equals(userRole)) {
            String projectCounty = currentUser.getProjectCounty();
            if (!StrUtil.isEmpty(projectCounty)){
                queryWrapper.eq("county", projectCounty);
                if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(targetAddress)) {
                    if (!StrUtil.isEmpty(projectName)) {
                        queryWrapper.like("project_name", projectName);
                    }
                    if (!StrUtil.isEmpty(targetAddress)) {
                        queryWrapper.like("destination_address", targetAddress);
                    }
                    List<PubNetWebEntity> authoritySearchList = list(queryWrapper);
                    return authoritySearchList;
                }
                if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(dialResult) || !StrUtil.isEmpty(taskResult)) {
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
                    List<PubNetWebEntity> authorityFilterList = list(queryWrapper);
                    return authorityFilterList;
                }
                List<PubNetWebEntity> authorityPage = list(queryWrapper);
                return authorityPage;
            }
        }

        // 搜搜判断
        if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(targetAddress)) {
            if (!StrUtil.isEmpty(projectName)) {
                queryWrapper.like("project_name", projectName);
            }
            if (!StrUtil.isEmpty(targetAddress)) {
                queryWrapper.like("destination_address", targetAddress);
            }
            List<PubNetWebEntity> searchList = this.list(queryWrapper);
            return searchList;
        }
        // 筛选判断
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
            List<PubNetWebEntity> filterList = this.list(queryWrapper);
            return filterList;
        }
        List<PubNetWebEntity> allPage = this.list();
        return allPage;
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
