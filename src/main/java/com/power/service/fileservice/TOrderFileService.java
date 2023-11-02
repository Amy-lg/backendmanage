package com.power.service.fileservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.fileentity.TOrderEntity;
import com.power.mapper.filemapper.TOrderFileMapper;
import com.power.utils.AnalysisExcelUtils;
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
        if (StrUtil.isEmpty(orderNum) && (dates == null || dates.size() == 0)) {
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
     * 工单处理时长,显示当前时间月份的平均时长
     * @return
     */
    public List<String> calculateAveDuration() {

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
                duration += Float.parseFloat(faultyDuration);
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


}
