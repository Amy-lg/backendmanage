package com.power.service.fileservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.User;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.mapper.filemapper.BusinessOrderFileMapper;
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
public class BusinessOrderFileService extends ServiceImpl<BusinessOrderFileMapper, BusinessOrderEntity> {

    /**
     * 业务工单数据导入
     * @param file
     * @return
     */
    public String importBusinessOrder(MultipartFile file) {

        if (!file.isEmpty()) {
            List<BusinessOrderEntity> businessOrderEntities = AnalysisExcelUtils.analysisBusinessOrderExcel(file);
            if (businessOrderEntities != null) {
                // 遍历循环存储
                for (BusinessOrderEntity order : businessOrderEntities) {
                    saveOrUpdate(order);
                }
            }
            return "数据信息上传成功！";
        }
        return null;
    }

    /**
     * 查询和筛选
     * @param pageNum 当前页码
     * @param pageSize 当前页显示数据条数
     * @param orderNum 工单号
     * @param dates 筛选时，筛选的日期时间段
     * @return list
     */
    public IPage<BusinessOrderEntity> queryOrFilter(Integer pageNum, Integer pageSize,
                                                   String orderNum, List<String> dates) {

        // 根据订单号模糊查询
        IPage<BusinessOrderEntity> businessOrderPage = new Page<>(pageNum, pageSize);
        QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();

        queryWrapper.orderByDesc("faulty_time");
        // 管理员
        // 登录人员权限限制问题（只显示登录人员自己所在区县的数据）
        User currentUser = TokenUtils.getCurrentUser();
        // 用户角色
        String userRole = currentUser.getRole();
        if (!StrUtil.isBlank(userRole) && ProStaConstant.MANAGER.equals(userRole)) {
            // 获取当前登录者所在区县
            String projectCounty = currentUser.getProjectCounty();
            // 固定权限查询条件（权限问题必须加入）
            queryWrapper.eq("county", projectCounty);
            // 权限中的检索
            if (!StrUtil.isEmpty(orderNum) && dates == null) {
                queryWrapper.like("order_num", orderNum);
                IPage<BusinessOrderEntity> orderIPage = page(businessOrderPage, queryWrapper);
                return orderIPage;
            }
            // 权限中的筛选
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
                    queryWrapper.between("faulty_time", beginTime, endTime);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                IPage<BusinessOrderEntity> filterPage = page(businessOrderPage, queryWrapper);
                return filterPage;
            }
            // 权限检索所有，只展示登录者所在区县的数据信息
            IPage<BusinessOrderEntity> authorityPage = page(businessOrderPage, queryWrapper);
            return authorityPage;
        }

        // 超级管理员
        // 如果都为空，那么查询所有
        if (StrUtil.isEmpty(orderNum) && (dates == null || dates.size() == 0)) {
            IPage<BusinessOrderEntity> allPage = this.page(businessOrderPage, queryWrapper);
            return allPage;
        } else {
            // 检索
            if (!StrUtil.isEmpty(orderNum) && dates == null) {
                queryWrapper.like("order_num", orderNum);
                IPage<BusinessOrderEntity> orderIPage = this.page(businessOrderPage, queryWrapper);
                return orderIPage;
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
                    queryWrapper.between("faulty_time", beginTime, endTime);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                IPage<BusinessOrderEntity> filterPage = this.page(businessOrderPage, queryWrapper);
                return filterPage;
            }
            return null;
        }
    }


    /**
     * 业务工单新增接口
     * @param businessOrder
     * @return
     */
    public String addBusinessOrder(BusinessOrderEntity businessOrder) {

        // 获取工单编号（唯一，不重复，不为空）
        String orderNum = businessOrder.getOrderNum();
        if (orderNum != null) {
            // 首先查询数据库是否存在此编号工单
            QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_num", orderNum);
            BusinessOrderEntity businessOrderEntity = this.getOne(queryWrapper, false);
            // 不存在工单编号为 orderNum 的数据信息
            if (businessOrderEntity == null) {
                // 新增
                boolean updateResult = this.saveOrUpdate(businessOrder);
                if (updateResult) {
                    return ResultStatusCode.SUCCESS_INSERT.getMsg();
                }
            } else {
                // 如果有此工单编号的数据信息，那么更新
                boolean b = this.update(businessOrder, queryWrapper);
                if (b) {
                    return ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg();
                }
            }
        }
        return ResultStatusCode.ERROR_UPDATE.getMsg();
    }


    /**
     * 1.工单处理时长，显示当前时间月份的平均时长
     * 2.分区县计算工单处理时长
     * @return
     */
    public List<String> calculateAveDuration(String ... county) {

        List<String> businessAverageDurationList = new ArrayList<>();
        QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // currentTime：2023-10-11
        String currentTime = formatter.format(LocalDateTime.now());
        // currentMonth：2023-10
        String currentMonth = currentTime.substring(0,7);

        // 如果参数不为null，那么就是分区县的平均时长
        if (county.length != 0) {
            queryWrapper.like("county", county[0]);
        }
        queryWrapper.like("faulty_time", currentMonth);
        // 当月时长数量
        long count = this.count(queryWrapper);
        if (count != 0) {
            // 查询、计算总时长
            float duration = 0f;
            List<BusinessOrderEntity> businessOrderEntityList = this.list(queryWrapper);
            for (BusinessOrderEntity businessOrder : businessOrderEntityList) {
                String faultyDuration = businessOrder.getFaultyDuration();
                duration += Float.parseFloat(faultyDuration);
            }
//            String averageDuration = String.format("%.2f", duration / count);
//            businessAverageDurationList.add(averageDuration);
            businessAverageDurationList.add(String.valueOf(count)); // 总数为分母
            businessAverageDurationList.add(String.valueOf(duration)); // 总时长为分子
            return businessAverageDurationList;
        }
        businessAverageDurationList.add(String.valueOf(count));
        return businessAverageDurationList;
    }


    /**
     * 业务工单数据导入
     * @param file
     * @return
     */
    public String importBusinessOrderExcel(MultipartFile file) {

        List<BusinessOrderEntity> bOrderEntityList = importProjectInfoData(file);
        if (bOrderEntityList != null) {
            boolean saveBatch = this.saveBatch(bOrderEntityList, 200);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }


    /**
     * 数据解析
     * @param file
     * @return
     */
    private List<BusinessOrderEntity> importProjectInfoData(MultipartFile file) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
        List<BusinessOrderEntity> bOrderEntityList = new ArrayList<>();
        BusinessOrderEntity businessOrder = null;

        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                // 通过反射获取私有属性名称
                Class<?> clazz = Class.forName("com.power.entity.fileentity.BusinessOrderEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 通过构造方法实例化对象
                            businessOrder = (BusinessOrderEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取所有私有属性
                            Field[] bOrderFields = clazz.getDeclaredFields();
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(bOrderFields);
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
                                            if (!"".equals(fieldAnnotation) && title != null
                                                    && title.equals(fieldAnnotation)) {
                                                cellValue = cell.getStringCellValue();
                                                bOrderFields[k + 2].setAccessible(true);
                                                bOrderFields[columnIndex].set(businessOrder, cellValue);
                                            }
                                        }
                                        break;
                                    case NUMERIC:
                                        double date = cell.getNumericCellValue();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        // Java在读取Excel单元格中日期格式的内容时，会自动将日期格式转换为数字格式；
                                        // 这里需要将读取到的Excel单元格中的日期格式的数字，转换成日期格式
                                        Date convertDate = DateUtil.getJavaDate(date);
                                        String formatDate = sdf.format(convertDate);
                                        bOrderFields[columnIndex].setAccessible(true);
                                        bOrderFields[columnIndex].set(businessOrder, formatDate);
                                        break;
                                    case BOOLEAN:
                                        bOrderFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        bOrderFields[columnIndex].setAccessible(true);
                                        bOrderFields[columnIndex].set(businessOrder, value);
                                        break;
                                    case BLANK:
                                        bOrderFields[columnIndex].setAccessible(true);
                                        bOrderFields[columnIndex].set(businessOrder, "");
                                        break;
                                    case ERROR:
//                                        byte errorCellValue = cell.getErrorCellValue();
                                        bOrderFields[columnIndex].setAccessible(true);
                                        bOrderFields[columnIndex].set(businessOrder, false);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            bOrderEntityList.add(businessOrder);
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
            return bOrderEntityList;
        }
        return null;
    }


    /**
     * 业务工单数量
     * @return
     */
    public int getBOrderOfSum() {
        long count = this.count();
        return (int) count;
    }


    /**
     * 未完结业务工单
     * @return
     */
    public int getBOrderOfUnfinished() {

        QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("order_status", ProStaConstant.PROJECT_FINISHED);
        long count = this.count(queryWrapper);
        return (int) count;
    }


    /**
     * 业务工单平均时长计算
     * @return
     */
    public float getBOrderOfDuration() {

        List<BusinessOrderEntity> businessOrderEntityList = this.list();
        if (businessOrderEntityList != null && businessOrderEntityList.size() >= 1) {
            float duration = 0f;
            for (BusinessOrderEntity businessOrder : businessOrderEntityList) {
                String faultyDuration = businessOrder.getFaultyDuration();
                if (faultyDuration != null && !faultyDuration.isEmpty()) {
                    duration += Float.parseFloat(faultyDuration);
                }
            }
            return duration;
        }
        return 0f;
    }


    /**
     * 业务工单前6月份工单数量统计
     * @return
     */
    public List<Object> getBusOrderOfBefore6Month() {

        // 相对于当前日期前六个月的时间
        List<Object> storeByMonthList = new ArrayList<>();
        // 条件二：区县划分数量
        /*for (int i = 0; i < 6; i++) {
            Map countyCountMap = new HashMap<String, Object>();
            // 获取日历实例
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MONTH, -i);
            Date beforeMonth = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String formatBeforeMonth = sdf.format(beforeMonth);
            // 遍历区县
            for (String county : ProStaConstant.counties) {
                // 先查询到前6月份的数据信息
                QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
                // 条件一：6个月之内的
                queryWrapper.like("faulty_time", formatBeforeMonth);
                queryWrapper.and(qw -> {
                    qw.like("county", county);
                });
                long count = this.count(queryWrapper);
                countyCountMap.put(county + ":" + formatBeforeMonth, count);
            }
            storeByMonthList.add(i, countyCountMap);
        }*/

        for (String county : ProStaConstant.counties) {
            Map countyCountMap = new LinkedHashMap<String, Object>();
            // List<Long> list = new ArrayList<>();
            for (int i = -5; i <= 0; i++) {
                // 获取日历实例
                String formatBeforeMonth = CalculateUtils.calcBeforeMonth(i);

                // 先查询到前6月份的数据信息
                QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
                // 条件一：6个月之内的
                queryWrapper.like("faulty_time", formatBeforeMonth);
                queryWrapper.and(qw -> {
                    qw.like("county", county);
                });
                long count = this.count(queryWrapper);
                // list.add(count);
                countyCountMap.put(county + ":" + formatBeforeMonth, count);
            }
            storeByMonthList.add(countyCountMap);
//            storeByMonthList.add(list);
        }
        return storeByMonthList;
    }


    /**
     * 业务工单每个区县前6月的平均处理时长
     * @return list存储
     */
    public List<Map<String, Object>> calcBOrderAveDurationByCounty() {

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
                    QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.like("faulty_time", formatBeforeMonth);
                    queryWrapper.and(qw -> {
                        qw.like("county", projectCounty);
                    });
                    List<BusinessOrderEntity> businessOrderList = list(queryWrapper);
                    String durOfMonthByCounty = "0.000";
                    if (!businessOrderList.isEmpty()) {
                        float durationSumByCounty = 0f;
                        int countOfCounty = businessOrderList.size();
                        for (BusinessOrderEntity businessOrder : businessOrderList) {
                            // 每条数据的工单历时
                            String orderFaultyDuration = businessOrder.getFaultyDuration();
                            if (!StrUtil.isEmpty(orderFaultyDuration)) {
                                Float faultyDuration = Float.parseFloat(orderFaultyDuration);
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
            // 每月平均处理时长
            storeCalcDataList.add(everyMonthOfAveDurationMap);
            return storeCalcDataList;
        }

        // 根据区县存储
        for (String constantCounty : ProStaConstant.counties) {
            Map<String, Object> linkedHashMap = new LinkedHashMap<>();
            for (int i = -5; i <= 0; i++) {
                String formatBeforeMonth = CalculateUtils.calcBeforeMonth(i);
                // 先查询到前6月份的数据信息
                QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.like("faulty_time", formatBeforeMonth);
                queryWrapper.and(qw -> {
                    qw.like("county", constantCounty);
                });
                List<BusinessOrderEntity> businessOrderList = this.list(queryWrapper);
                String durOfMonthByCounty = "0.000";
                if (!businessOrderList.isEmpty()) {
                    float durationSumByCounty = 0f;
                    int countOfCounty = businessOrderList.size();
                    for (BusinessOrderEntity businessOrder : businessOrderList) {
                        // 每条数据的工单历时
                        String orderFaultyDuration = businessOrder.getFaultyDuration();
                        if (!StrUtil.isEmpty(orderFaultyDuration)) {
                            Float faultyDuration = Float.parseFloat(orderFaultyDuration);
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
        // 每月平均处理时长
        storeCalcDataList.add(everyMonthOfAveDurationMap);
        // 返回之前删除第一个嘉禾数据
        storeCalcDataList.remove(1);
        storeCalcDataList.remove(6);
        return storeCalcDataList;
    }


    /**
     * 计算前六个月每月的平均处理时长
     * @return 返回月份平均值
     */
    private Map<String, Object> calcAveDurationBefore6Month() {
        // Map存储集合
        Map<String, Object> aveDurationMap = new LinkedHashMap<>();
        for (int i = -5; i <= 0; i++) {
            // 月份计算
            String lastMonth = CalculateUtils.calcBeforeMonth(i);
            QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("faulty_time", lastMonth);
            // 查询出当前月份所有数据
            List<BusinessOrderEntity> currentSearchMonthList = list(queryWrapper);
            String currentMonthAveValue = "0.000";
            if (currentSearchMonthList != null && currentSearchMonthList.size() >= 1) {
                float durationSumOfMonth = 0f;
                int currentMonthCount = currentSearchMonthList.size();
                for (BusinessOrderEntity businessOrder : currentSearchMonthList) {
                    String faultyDuration = businessOrder.getFaultyDuration();
                    if (!StrUtil.isEmpty(faultyDuration)) {
                        Float faultyDurationOfFloat = Float.parseFloat(faultyDuration);
                        // 常量区县 = 数据中的区县 ==> 计算故障历时总和
                        durationSumOfMonth += faultyDurationOfFloat;
                    }
                }
                currentMonthAveValue = String.format("%.3f", (durationSumOfMonth / (float) currentMonthCount));
            }
            aveDurationMap.put(lastMonth, currentMonthAveValue);
        }
        return aveDurationMap;
    }


    /**
     * 封装计算前6个月份的方法
     * @param number
     * @return
     */
    private String calculateMonth(int number) {
        // 获取日历实例
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, number);
        Date beforeMonth = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String formatBeforeMonth = sdf.format(beforeMonth);
        return formatBeforeMonth;
    }


}
