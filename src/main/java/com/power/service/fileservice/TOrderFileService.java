package com.power.service.fileservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.User;
import com.power.entity.basic.ProjectDataInfoEntity;
import com.power.entity.fileentity.TOrderEntity;
import com.power.mapper.filemapper.TOrderFileMapper;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.CalculateUtils;
import com.power.utils.TokenUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TOrderFileService extends ServiceImpl<TOrderFileMapper, TOrderEntity> {

    /**
     * 小T工单数据导入
     * @param file
     * @return
     */
    public String importTOrder(MultipartFile file) {
        if (!file.isEmpty()) {
            List<TOrderEntity> tOrderEntities = AnalysisExcelUtils.analysisTOrderExcel(file);
            if (tOrderEntities != null) {
                // 遍历循环存储
                for (TOrderEntity order : tOrderEntities) {
                    saveOrUpdate(order);
                }
            }
            return "数据信息上传成功！";
        }
        return null;
    }


    /**
     * 数据导入
     * @param file
     * @return
     */
    public String importTOrderExcel(MultipartFile file) {

        List<TOrderEntity> tOrderEntityList = this.importProjectInfoData(file);
        if (tOrderEntityList != null) {
            boolean saveBatch = this.saveBatch(tOrderEntityList, 200);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 数据解析
     * @param tOrderFile
     * @return
     */
    private List<TOrderEntity> importProjectInfoData(MultipartFile tOrderFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(tOrderFile);
        List<TOrderEntity> tOrderEntityList = new ArrayList<>();
        TOrderEntity tOrderEntity = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                // 通过反射获取私有属性名称
                Class<?> clazz = Class.forName("com.power.entity.fileentity.TOrderEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
//                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 通过构造方法实例化对象
                            tOrderEntity = (TOrderEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取所有私有属性
                            Field[] tOrderFields = clazz.getDeclaredFields();
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
                                        cellValue = cell.getStringCellValue();
                                        tOrderFields[columnIndex].setAccessible(true);
                                        tOrderFields[columnIndex].set(tOrderEntity, cellValue);
                                        break;
                                    case NUMERIC:
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        tOrderFields[columnIndex].setAccessible(true);
                                        tOrderFields[columnIndex].set(tOrderEntity, cellValue);
                                        break;
                                    case BOOLEAN:
                                        tOrderFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        tOrderFields[columnIndex].setAccessible(true);
                                        tOrderFields[columnIndex].set(tOrderEntity, value);
                                        break;
                                    case BLANK:
                                        tOrderFields[columnIndex].setAccessible(true);
                                        cellValue = "";
                                        break;
                                    case ERROR:
//                                        byte errorCellValue = cell.getErrorCellValue();
                                        tOrderFields[columnIndex].setAccessible(true);
                                        tOrderFields[columnIndex].set(tOrderEntity, false);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            tOrderEntityList.add(tOrderEntity);
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
            return tOrderEntityList;
        }
        return null;
    }


    /**
     * 根据月份查询小T工单数量根
     * @return
     */
    public List<Object> getTOrderCount() {
        ArrayList<Object> countList = new ArrayList<>();
        int[] monthCount = new int[12];
        List<TOrderEntity> list = this.list();
        if (list.size() != 0 && list != null) {
            int oldYear = 0;
            for (TOrderEntity tOrder : list) {
                String tOrderFaultyTime = tOrder.getDispatchOrderTime();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date parseDate = sdf.parse(tOrderFaultyTime);
                    // 判断是否同一年份
                    if (oldYear == 0 || oldYear == parseDate.getYear()) {
                        oldYear = parseDate.getYear();
                        int month = parseDate.getMonth() + 1;
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
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            countList.add(monthCount);
            return countList;
        }
        // 查询结果为空，返回0
        countList.add(monthCount);
        return countList;
    }


    /**
     * 查询和筛选
     * @param pageNum 当前页码
     * @param pageSize 当前页显示数据条数
     * @param orderNum 工单号
     * @param dates 筛选时，筛选的日期时间段
     * @return list
     */
    public IPage<TOrderEntity> queryOrFilterTOrder(Integer pageNum, Integer pageSize,
                                                   String orderNum, List<String> dates) {

        IPage<TOrderEntity> tOrderPage = new Page<>(pageNum, pageSize);
        QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();

        // 管理员权限
        User currentUser = TokenUtils.getCurrentUser();
        String userRole = currentUser.getRole();
        if (!StrUtil.isBlank(userRole) && ProStaConstant.MANAGER.equals(userRole)) {
            String projectCounty = currentUser.getProjectCounty();
            queryWrapper.eq("county", projectCounty);

            // 权限检索
            if (!StrUtil.isEmpty(orderNum) && dates == null) {
                queryWrapper.like("order_num", orderNum);
                IPage<TOrderEntity> tOrderIPage = this.page(tOrderPage, queryWrapper);
                return tOrderIPage;
            }
            // 权限筛选
            if ((dates != null) && StrUtil.isEmpty(orderNum)) {
                // 获取开始时间结束时间
                String beginDate = dates.get(0);
                String beginDateTime = beginDate + " 00:00:00";
                String endDate = dates.get(1);
                String endDateTime = endDate + " 23:59:59";
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date beginTime = sdf.parse(beginDateTime);
                    Date endTime = sdf.parse(endDateTime);
                    queryWrapper.between("dispatch_order_time", beginTime, endTime);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                IPage<TOrderEntity> filterPage = this.page(tOrderPage, queryWrapper);
                return filterPage;
            }
            queryWrapper.orderByDesc("id");
            IPage<TOrderEntity> authorityPage = page(tOrderPage, queryWrapper);
            return authorityPage;
        }

        // 超级管理员
        if (StrUtil.isEmpty(orderNum) && (dates == null || dates.size() == 0)) {
            queryWrapper.orderByDesc("id");
            IPage<TOrderEntity> allPage = this.page(tOrderPage, queryWrapper);
            return allPage;
        } else {
            // 检索
            if (!StrUtil.isEmpty(orderNum) && dates == null) {
                queryWrapper.like("order_num", orderNum);
                IPage<TOrderEntity> tOrderIPage = this.page(tOrderPage, queryWrapper);
                return tOrderIPage;
            }
            // 筛选
            if ((dates != null || dates.size() == 2) && StrUtil.isEmpty(orderNum)) {
                // 获取开始时间结束时间
                String beginDate = dates.get(0);
                String beginDateTime = beginDate + " 00:00:00";
                String endDate = dates.get(1);
                String endDateTime = endDate + " 23:59:59";
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date beginTime = sdf.parse(beginDateTime);
                    Date endTime = sdf.parse(endDateTime);
                    queryWrapper.between("dispatch_order_time", beginTime, endTime);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                IPage<TOrderEntity> filterPage = this.page(tOrderPage, queryWrapper);
                return filterPage;
            }
            return null;
        }
    }


    /**
     * 小T工单新增接口
     * @param tOrder
     * @return
     */
    public String addLittleTOrder(TOrderEntity tOrder) {

        String orderNum = tOrder.getOrderNum();
        if (orderNum != null) {
            // 首先查询数据库是否存在此编号工单
            QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_num", orderNum);
            TOrderEntity tOrderEntity = this.getOne(queryWrapper, false);
            if (tOrderEntity == null) {
                // 不存在工单编号为 orderNum 的数据信息
                // 新增
                boolean updateResult = this.saveOrUpdate(tOrder);
                if (updateResult) {
                    return ResultStatusCode.SUCCESS_INSERT.getMsg();
                }
            } else {
                // 如果有此工单编号的数据信息，那么更新
                boolean b = this.update(tOrder, queryWrapper);
                if (b) {
                    return ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg();
                }
            }
        }
        return ResultStatusCode.ERROR_UPDATE.getMsg();
    }


    /**
     * 1.工单处理时长,显示当前时间月份的平均时长
     * 2.分区县计算工单处理时长
     * @return
     */
    public List<String> calculateAveDuration(String ... county) {

        List<String> tOrderAverageDurationList = new ArrayList<>();
        QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();

        // 当前月份时长
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // currentTime：2023-10-11
        String currentTime = formatter.format(LocalDateTime.now());
        // currentMonth：2023-10
        String currentMonth = currentTime.substring(0,7);

        // 时长显示上一个月的
        Date now = new Date();
        Date beforeMonth;
        // 获取日历
        Calendar calendar = Calendar.getInstance();
        // 当前时间赋值给日历
        calendar.setTime(now);
        // 前月
        calendar.add(Calendar.MONTH, -1);
        // 得到前月的时间
        beforeMonth = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formatBeforeMonth = sdf.format(beforeMonth);
        String oneMonthAgo = formatBeforeMonth.substring(0, 7);

        queryWrapper.like("dispatch_order_time", oneMonthAgo);
        // 当月时长数量
        long count = this.count(queryWrapper);
        if (count != 0) {
            // 查询、计算总时长
            float duration = 0f;
            List<TOrderEntity> tOrderEntityList = this.list(queryWrapper);
            for (TOrderEntity tOrder : tOrderEntityList) {
                String faultyDuration = tOrder.getOrderDuration();
                if (faultyDuration != null && !faultyDuration.isEmpty()) {
                    duration += Float.parseFloat(faultyDuration);
                }
            }
//            String averageDuration = String.format("%.2f", duration / count);
//            businessAverageDurationList.add(averageDuration);
            tOrderAverageDurationList.add(String.valueOf(count)); // 总数为分母
            tOrderAverageDurationList.add(String.valueOf(duration)); // 总时长为分子
            return tOrderAverageDurationList;
        }
        tOrderAverageDurationList.add(String.valueOf(count));
        return tOrderAverageDurationList;
    }


    /**
     * t工单数量
     * @return
     */
    public int getTOrderOfSum() {
        // 查询所有t工单数量
        // todo 去除区县字段值为空的情况
        QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("county");
        long count = this.count(queryWrapper);
        return (int) count;
    }


    /**
     * t未完结工单
     * @return
     */
    public int getTOrderOfUnfinished() {

        QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("order_status", ProStaConstant.PROJECT_FINISHED);
        long count = this.count(queryWrapper);
        return (int) count;
    }


    /**
     * 计算t工单处理平均时长
     * @return
     */
    public float getTOrderOfDuration() {

        // 获取全部数据
        List<TOrderEntity> tOrderEntityList = this.list();
        // 遍历取出工单历时
        if (tOrderEntityList != null && tOrderEntityList.size() >= 1) {
            float duration = 0f;
            for (TOrderEntity tOrder : tOrderEntityList) {
                String orderDuration = tOrder.getOrderDuration();
                if (orderDuration != null && !orderDuration.isEmpty()) {
                    duration += Float.parseFloat(orderDuration);
                }
            }
            return duration;
        }
        return 0f;
    }


    /**
     * T工单前6月份工单数量统计
     * @param basicInfoEntityList
     * @return
     */
    public List<Map<String, Object>> getTOrderOfBefore6Month(List<ProjectDataInfoEntity> basicInfoEntityList) {

        // T工单匹配区县
        matchingCountyByOrderTheme(basicInfoEntityList);

        List<Map<String, Object>> storeByMonthList = new ArrayList<>();
        for (String county : ProStaConstant.counties) {
            Map countyCountMap = new LinkedHashMap<String, Object>();
            for (int i = -5; i <= 0; i++) {
                // 获取日历实例
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.MONTH, i);
                Date beforeMonth = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String formatBeforeMonth = sdf.format(beforeMonth);

                // 先查询到前6月份的数据信息
                QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
                // 条件一：6个月之内的
                queryWrapper.like("dispatch_order_time", formatBeforeMonth);
                queryWrapper.and(qw -> {
                    qw.like("county", county);
                });
                long count = this.count(queryWrapper);
                countyCountMap.put(county + ":" + formatBeforeMonth, count);
            }
            storeByMonthList.add(countyCountMap);
        }
        return storeByMonthList;
    }


    /**
     * T工单匹配区县
     * @param basicInfoEntityList 被匹配数据对象
     * @return 返回带区县的T工单
     */
    private void matchingCountyByOrderTheme(List<ProjectDataInfoEntity> basicInfoEntityList) {
        // 获取全部信息
        List<TOrderEntity> tOrderEntityList = this.list();
        // ArrayList<TOrderEntity> storeNewTOrderList = new ArrayList<>();
        if (tOrderEntityList != null && tOrderEntityList.size() >= 1) {
            for (TOrderEntity tOrder : tOrderEntityList) {
                String orderTheme = tOrder.getOrderTheme();
                // 截取出的项目名称字符串
                String tProjectName = StrUtil.subBetween(orderTheme, "嘉兴市-", "-IT故障-:");
                // 遍历循环项目概况数据，与项目名匹配
                if (basicInfoEntityList != null && basicInfoEntityList.size() > 0) {
                    for (ProjectDataInfoEntity basicInfo : basicInfoEntityList) {
                        String basicInfoIctProjectName = basicInfo.getProjectName();
                        if (!StrUtil.isEmpty(tProjectName) && !StrUtil.isBlank(basicInfoIctProjectName)
                                && tProjectName.equals(basicInfoIctProjectName)) {
                            // 获取区县名
                            String basicInfoCounty = basicInfo.getCounty();
                            tOrder.setCounty(basicInfoCounty);
                            saveOrUpdate(tOrder);
                        }
                    }
                }
                // storeNewTOrderList.add(tOrder);
            }
        }
    }


    /**
     * 能力分析模块
     * t工单每个区县前6月的平均处理时长
     * @return
     */
    public List<Map<String, Object>> calcTOrderAveDurationByCounty() {

        List<Map<String, Object>> storeCalcDataList = new ArrayList<>();
        // 存储秀洲南湖
        Map<String, String> xuiZhouMap = new LinkedHashMap<>();
        Map<String, String> nanHuMap = new LinkedHashMap<>();

        // 登录者权限
        User currentUser = TokenUtils.getCurrentUser();
        String userRole = currentUser.getRole();
        if (!StrUtil.isBlank(userRole) && ProStaConstant.MANAGER.equals(userRole)) {
            String projectCounty = currentUser.getProjectCounty();
            if (!StrUtil.isEmpty(projectCounty)) {
                Map<String, Object> linkedHashMap = new LinkedHashMap<>();
                String currentUserCounty = projectCounty.substring(0, 2);
                for (int i = -5; i <= 0; i++) {
                    String formatBeforeMonth = CalculateUtils.calcBeforeMonth(i);
                    // 先查询到前6月份的数据信息
                    QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.like("dispatch_order_time", formatBeforeMonth);
                    queryWrapper.and(qw -> {
                        qw.like("county", projectCounty);
                    });
                    List<TOrderEntity> tOrderList = this.list(queryWrapper);
                    String durOfMonthByCounty = "0.000";
                    if (!tOrderList.isEmpty()) {
                        float durationSumByCounty = 0f;
                        int countOfCounty = tOrderList.size();
                        for (TOrderEntity tOrder : tOrderList) {
                            // 每条数据的工单历时
                            String orderDuration = tOrder.getOrderDuration();
                            if (!StrUtil.isEmpty(orderDuration)) {
                                Float faultyDuration = Float.parseFloat(orderDuration);
                                // 常量区县 = 数据中的区县 ==> 计算故障历时总和
                                durationSumByCounty += faultyDuration;
                            }
                        }
                        // 计算
                        durOfMonthByCounty = String.format("%.3f", (durationSumByCounty / (float) countOfCounty));
                    }
                    // 存储(南湖+秀洲-->嘉禾)
                    if (currentUserCounty.equals(ProStaConstant.XIU_ZHOU)) {
                        xuiZhouMap.put(formatBeforeMonth, durOfMonthByCounty);
                    }
                    if (currentUserCounty.equals(ProStaConstant.NAN_HU)) {
                        nanHuMap.put(formatBeforeMonth, durOfMonthByCounty);
                    }
                    linkedHashMap.put(currentUserCounty + ":" + formatBeforeMonth, durOfMonthByCounty);
                }
                // 遍历南湖秀洲集合，将值相加
                if (!xuiZhouMap.isEmpty() && !nanHuMap.isEmpty()) {
                    for (Map.Entry<String, String> xuiZhouEntry : xuiZhouMap.entrySet()){
                        String xuiZhouEntryKey = xuiZhouEntry.getKey();
                        for (Map.Entry<String, String> nanHuEntry : nanHuMap.entrySet()) {
                            String nanHuEntryKey = nanHuEntry.getKey();
                            if (xuiZhouEntryKey.equals(nanHuEntryKey)) {
                                String durOfMonthByNanXui = String.format("%.3f", Float.parseFloat(xuiZhouEntry.getValue()) +
                                        Float.parseFloat(nanHuEntry.getValue()));
                                linkedHashMap.put(ProStaConstant.JIA_HE + ":" + nanHuEntryKey, durOfMonthByNanXui);
                                // 将南湖数据删除
                                linkedHashMap.remove(ProStaConstant.NAN_HU + ":" + nanHuEntryKey);
                            }
                        }
                    }
                }
                storeCalcDataList.add(linkedHashMap);
            }
            Map<String, Object> everyMonthOfAveDurationMap = calcAveDurationBefore6Month();
            storeCalcDataList.add(everyMonthOfAveDurationMap);
            return storeCalcDataList;
        }

        // 根据区县存储
        for (String constantCounty : ProStaConstant.counties) {
            Map<String, Object> linkedHashMap = new LinkedHashMap<>();
            for (int i = -5; i <= 0; i++) {
                String formatBeforeMonth = CalculateUtils.calcBeforeMonth(i);
                // 先查询到前6月份的数据信息
                QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.like("dispatch_order_time", formatBeforeMonth);
                queryWrapper.and(qw -> {
                    qw.like("county", constantCounty);
                });
                List<TOrderEntity> tOrderList = this.list(queryWrapper);
                String durOfMonthByCounty = "0.000";
                if (!tOrderList.isEmpty()) {
                    float durationSumByCounty = 0f;
                    int countOfCounty = tOrderList.size();
                    for (TOrderEntity tOrder : tOrderList) {
                        // 每条数据的工单历时
                        String orderDuration = tOrder.getOrderDuration();
                        if (!StrUtil.isEmpty(orderDuration)) {
                            Float faultyDuration = Float.parseFloat(orderDuration);
                            // 常量区县 = 数据中的区县 ==> 计算故障历时总和
                            durationSumByCounty += faultyDuration;
                        }
                    }
                    // 计算
                    durOfMonthByCounty = String.format("%.3f", (durationSumByCounty / (float) countOfCounty));
                }
                // 存储(南湖+秀洲-->嘉禾)
                if (constantCounty.equals(ProStaConstant.XIU_ZHOU)) {
                    xuiZhouMap.put(formatBeforeMonth, durOfMonthByCounty);
                }
                if (constantCounty.equals(ProStaConstant.NAN_HU)) {
                    nanHuMap.put(formatBeforeMonth, durOfMonthByCounty);
                }
                linkedHashMap.put(constantCounty + ":" + formatBeforeMonth, durOfMonthByCounty);
            }
            // 遍历南湖秀洲集合，将值相加
            if (!xuiZhouMap.isEmpty() && !nanHuMap.isEmpty()) {
                for (Map.Entry<String, String> xuiZhouEntry : xuiZhouMap.entrySet()){
                    String xuiZhouEntryKey = xuiZhouEntry.getKey();
                    for (Map.Entry<String, String> nanHuEntry : nanHuMap.entrySet()) {
                        String nanHuEntryKey = nanHuEntry.getKey();
                        if (xuiZhouEntryKey.equals(nanHuEntryKey)) {
                            String durOfMonthByNanXui = String.format("%.3f", Float.parseFloat(xuiZhouEntry.getValue()) +
                                    Float.parseFloat(nanHuEntry.getValue()));
                            linkedHashMap.put(ProStaConstant.JIA_HE + ":" + nanHuEntryKey, durOfMonthByNanXui);
                            // 将南湖数据删除
                            linkedHashMap.remove(ProStaConstant.NAN_HU + ":" + nanHuEntryKey);
                        }
                    }
                }
            }
            storeCalcDataList.add(linkedHashMap);
        }
        Map<String, Object> everyMonthOfAveDurationMap = calcAveDurationBefore6Month();
        storeCalcDataList.add(everyMonthOfAveDurationMap);
        // 返回之前删除第一个嘉禾数据
        storeCalcDataList.remove(1);
        storeCalcDataList.remove(6);
        return storeCalcDataList;
    }


    /**
     * 统计区县总数(去除项目区县为空的情况)
     * @param constantCounty
     * @return
     */
    private int calcCountyCount(String constantCounty) {
        QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("county", constantCounty);
        int countCounty = (int) count(queryWrapper);
        return countCounty;
    }


    /**
     * t工单前12月份每月数量
     * @return
     */
    public List<Integer> tOrderOfBefore12Month() {

        List<TOrderEntity> tFaultyList = list();
        //Map<String, Integer> saveTFaultyMonthCount = null;
        List<Integer> saveFaultyMonthCountList = null;
        if (tFaultyList != null && tFaultyList.size() >= 1) {
            //saveTFaultyMonthCount = new LinkedHashMap<>();
            saveFaultyMonthCountList = new ArrayList<>();
            try{
                // 遍历前12月份
                for (int monthCalc = -11; monthCalc <= 0; monthCalc++) {
                    // 计算月份
                    String formatBeforeMonth = CalculateUtils.calcBeforeMonth(monthCalc);
                    // 计数
                    int count = 0;
                    // 遍历数据信息
                    for (TOrderEntity tOrder : tFaultyList) {
                        // 故障时间
                        String faultyTime = tOrder.getDispatchOrderTime();
                        String faultyMonth = faultyTime.substring(0, 7);
                        if (formatBeforeMonth.equals(faultyMonth)) {
                            count += 1;
                        }
                    }
                    saveFaultyMonthCountList.add(count);
                    //saveTFaultyMonthCount.put(formatBeforeMonth, count);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return saveFaultyMonthCountList;
    }


    /**
     * 计算前六个月每月的平均处理时长
     * @return 返回月份平均值
     */
    private Map<String, Object> calcAveDurationBefore6Month() {
        // Map存储集合
        Map<String, Object> aveTDurationMap = new LinkedHashMap<>();
        for (int i = -5; i <= 0; i++) {
            // 月份计算
            String lastMonth = CalculateUtils.calcBeforeMonth(i);
            QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("dispatch_order_time", lastMonth);
            queryWrapper.isNotNull("county").ne("county", "");
            // 查询出当前月份所有数据
            List<TOrderEntity> currentSearchMonthList = list(queryWrapper);
            String currentMonthAveValue = "0.000";
            if (currentSearchMonthList != null && currentSearchMonthList.size() >= 1) {
                float durationSumOfMonth = 0f;
                int currentMonthCount = currentSearchMonthList.size();
                for (TOrderEntity tOrder : currentSearchMonthList) {
                    String orderDuration = tOrder.getOrderDuration();
                    if (!StrUtil.isEmpty(orderDuration)) {
                        Float faultyDurationOfFloat = Float.parseFloat(orderDuration);
                        // 常量区县 = 数据中的区县 ==> 计算故障历时总和
                        durationSumOfMonth += faultyDurationOfFloat;
                    }
                }
                currentMonthAveValue = String.format("%.3f", (durationSumOfMonth / (float) currentMonthCount));
            }
            aveTDurationMap.put(lastMonth, currentMonthAveValue);
        }
        return aveTDurationMap;
    }


}
