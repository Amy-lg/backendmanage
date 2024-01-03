package com.power.service.fileservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.mapper.filemapper.BusinessOrderFileMapper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
//                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        // 数据总行数
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            // 通过构造方法实例化对象
                            businessOrder = (BusinessOrderEntity) clazz.getDeclaredConstructor().newInstance();
                            // 获取所有私有属性
                            Field[] bOrderFields = clazz.getDeclaredFields();
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
                                        bOrderFields[columnIndex].setAccessible(true);
                                        bOrderFields[columnIndex].set(businessOrder, cellValue);
                                        break;
                                    case NUMERIC:
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        bOrderFields[columnIndex].setAccessible(true);
                                        bOrderFields[columnIndex].set(businessOrder, cellValue);
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
                                        cellValue = "";
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


}
