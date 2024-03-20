package com.power.service.evaluationservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.evaluation.EvaluationEntity;
import com.power.entity.evaluation.searchfilter.EvalSearchFilterEntity;
import com.power.mapper.evaluationmapper.EvaluationMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EvaluationService extends ServiceImpl<EvaluationMapper, EvaluationEntity> {


    /**
     * 数据导入
     * @param file
     * @return
     */
    public String importEvaluationExcel(MultipartFile file) {

        if (file != null) {
            List<EvaluationEntity> evaluationList = this.importData(file);
            if (evaluationList != null && evaluationList.size() != 0) {
                this.saveBatch(evaluationList, 100);
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return null;
    }


    /**
     * 各区县满意、非满数量;满意度平均分
     * @return
     */
    public List<Map<String, String>> calcAverScoreAndCount() {

        ArrayList<Map<String, String>> calcResultList = new ArrayList<>();
        // 区县满意度数量
        for (String county : ProStaConstant.counties) {
            Map<String, String> satisfiedMap = new HashMap<>();
            QueryWrapper<EvaluationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("county", county);
            queryWrapper.isNotNull("service_satisfaction").ne("service_satisfaction", "");
            // 区县总数(只统计有分数的)
            long countySum = this.count(queryWrapper);
            // 和筛选查询条件的数量相同
            queryWrapper.eq("service_satisfaction", 10);
            // 区县满意数
            long satisfiedCount = this.count(queryWrapper);
            // 区县满意度平均分计算（区县满意数/区县总数）
            String rate = null;
            if (countySum != 0) {
                rate = String.format("%.2f",(((double) satisfiedCount * 10 / (double) countySum)));
            } else {
                rate = "0.00";
            }
            satisfiedMap.put("区县", county);
            satisfiedMap.put("满意数", String.valueOf(satisfiedCount));
            satisfiedMap.put("平均分", rate);

            calcResultList.add(satisfiedMap);
        }
        // 区县非满意度数量
        int indexCount = 0;
        for (String county : ProStaConstant.counties) {
            QueryWrapper<EvaluationEntity> queryWrapper = new QueryWrapper<>();
            // 条件
            queryWrapper.eq("county", county);
            queryWrapper.isNotNull("service_satisfaction").ne("service_satisfaction","");
            queryWrapper.ne("service_satisfaction", 10);

            long unSatisfiedCount = this.count(queryWrapper);

            Map<String, String> stringMap = calcResultList.get(indexCount);
            stringMap.put("非满意", String.valueOf(unSatisfiedCount));
            indexCount += 1;
        }
        return calcResultList;
    }


    /**
     * 数据表分析
     * @param evaluationFile
     * @return
     */
    private List<EvaluationEntity> importData(MultipartFile evaluationFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(evaluationFile);
        EvaluationEntity evaluation = null;
        List<EvaluationEntity> evaluationEntityArrayList = new ArrayList<>();

        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                Class<?> clazz = Class.forName("com.power.entity.evaluation.EvaluationEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // Excel列标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            evaluation = (EvaluationEntity) clazz.getDeclaredConstructor().newInstance();
                            Field[] evaluationFields = clazz.getDeclaredFields();
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(evaluationFields);
                            // 获取行实例
                            Row contentRow = sheet.getRow(j);
                            Iterator<Cell> cellIterator = contentRow.cellIterator();
                            while (cellIterator.hasNext()) {
                                // 成员变量放在下面，防止上一个数据对下一个数据影响
                                String cellValue = null;
                                String title = null;

                                Cell cell = cellIterator.next();
                                CellType cellType = cell.getCellType();
                                // 利用列索引循环属性赋值（从第3个属性开始）
                                int columnIndex = cell.getColumnIndex() + 2;
                                switch (cellType) {
                                    case STRING:
                                        title = excelTitle.get(cell.getColumnIndex());
                                        for (int k = 0; k < fieldAnnotationList.size(); k++) {
                                            String fieldAnnotation = fieldAnnotationList.get(k);
                                            if (!"".equals(fieldAnnotation) && fieldAnnotation != null
                                                    && title != null && title.equals(fieldAnnotation)) {
                                                cellValue = cell.getStringCellValue();
                                                evaluationFields[k + 2].setAccessible(true);
                                                evaluationFields[k + 2].set(evaluation, cellValue);
                                                break;
                                            }
                                        }
                                        break;
                                    case NUMERIC:
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        evaluationFields[columnIndex].setAccessible(true);
                                        evaluationFields[columnIndex].set(evaluation, cellValue);
                                        break;
                                    case BOOLEAN:
                                        evaluationFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper()
                                                .createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        evaluationFields[columnIndex].setAccessible(true);
                                        evaluationFields[columnIndex].set(evaluation, value);
                                        break;
                                    case BLANK:
                                        title = excelTitle.get(cell.getColumnIndex());
                                        for (int k = 0; k < fieldAnnotationList.size(); k++) {
                                            String fieldAnnotation = fieldAnnotationList.get(k);
                                            if (!"".equals(fieldAnnotation) && fieldAnnotation != null
                                                    && title != null && title.equals(fieldAnnotation)) {
                                                evaluationFields[k + 2].setAccessible(true);
                                                evaluationFields[k + 2].set(evaluation, cellValue);
                                                break;
                                            }
                                        }
                                        break;
                                    case ERROR:
//                                        byte errorCellValue = cell.getErrorCellValue();
                                        evaluationFields[columnIndex].setAccessible(true);
                                        evaluationFields[columnIndex].set(evaluation, false);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }

                            /*short lastCellNum = contentRow.getLastCellNum();
                            evaluation = new EvaluationEntity();
                            for (int k = 1; k < lastCellNum; k++) {
                                Cell cell = contentRow.getCell(k);
                                if (cell != null) {
                                    CellType cellType = cell.getCellType();
                                    if (CellType.STRING == cellType) {
                                        cellValue = cell.getStringCellValue();
                                    } else if (CellType.BLANK == cellType){
                                        cellValue = null;
                                    }else {
                                        cellValue = null;
                                    }
                                }else {
                                    cellValue = null;
                                }
                                switch (k) {
                                    case 1:
                                        evaluation.setCounty(cellValue);
                                        k += 1;
                                        break;
                                    case 3:
                                        evaluation.setProjectNum(cellValue);
                                        break;
                                    case 4:
                                        evaluation.setProjectName(cellValue);
                                        k += 2;
                                        break;
                                    case 7:
                                        evaluation.setCustomerName(cellValue);
                                        k += 4;
                                        break;
                                    case 12:
                                        evaluation.setAfterSalesCustomer(cellValue);
                                        break;
                                    case 13:
                                        evaluation.setAfterSalesPhone(cellValue);
                                        k += 7;
                                        break;
                                    case 21:
                                        evaluation.setIntersectionDate(cellValue);
                                        break;
                                    case 22:
                                        evaluation.setContractEndDate(cellValue);
                                        k += 4;
                                        break;
                                    case 27:
                                        evaluation.setServiceAware(cellValue);
                                        break;
                                    case 28:
                                        evaluation.setAfterSalesPersonnel(cellValue);
                                        break;
                                    case 29:
                                        evaluation.setAfterSalesResponse(cellValue);
                                        break;
                                    case 30:
                                        evaluation.setServiceSatisfaction(cellValue);
                                        break;
                                    case 31:
                                        evaluation.setCustomerAdvisement(cellValue);
                                        k += 3;
                                        break;
                                    case 33:
                                        evaluation.setProblemDescription(cellValue);
                                        k += 1;
                                        break;
                                    case 35:
                                        if (cellValue != null && !(cellValue.contains("-"))) {
                                            double date = Double.parseDouble(cellValue);
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                            Date convertDate = DateUtil.getJavaDate(date);
                                            String formatDate = sdf.format(convertDate);
                                            evaluation.setRevisitingTime(formatDate);
                                        }
                                        evaluation.setRevisitingTime(cellValue);
                                        break;
                                    default:
                                        break;
                                }
                            }*/
                            evaluationEntityArrayList.add(evaluation);
                        }
                    }
                    continue;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return evaluationEntityArrayList;
        }
        return null;
    }


    /**
     * 搜索、筛选操作
     * @param evalSearchFilter
     * @return
     */
    public IPage<EvaluationEntity> searchOrFilterEvaluation(EvalSearchFilterEntity evalSearchFilter) {

        Integer pageNum = evalSearchFilter.getPageNum();
        Integer pageSize = evalSearchFilter.getPageSize();

        IPage<EvaluationEntity> evaluationPages = new Page<>(pageNum, pageSize);
        QueryWrapper<EvaluationEntity> queryWrapper = new QueryWrapper<>();

        // 项目编号
        String projectNum = evalSearchFilter.getProjectNum();
        // 项目名称
        String projectName = evalSearchFilter.getProjectName();
        // 搜索
        if (!StrUtil.isEmpty(projectNum) || !StrUtil.isEmpty(projectName)) {
            if (!StrUtil.isEmpty(projectNum)) {
                queryWrapper.like("project_num", projectNum);
            }
            if (!StrUtil.isEmpty(projectName)) {
                queryWrapper.like("project_name", projectName);
            }
            IPage<EvaluationEntity> searchPage = this.page(evaluationPages, queryWrapper);
            return searchPage;
        }
        // 筛选
        String county = evalSearchFilter.getCounty();
        String serviceSatisfaction = evalSearchFilter.getServiceSatisfaction();
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(serviceSatisfaction)) {
            if (!StrUtil.isEmpty(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isEmpty(serviceSatisfaction)) {
                // 满意
                if (ProStaConstant.SATISFIED.equals(serviceSatisfaction)) {
                    queryWrapper.eq("service_aware", 10).or().isNull("service_aware");
                    queryWrapper.eq("after_sales_personnel", 10).or().isNull("after_sales_personnel");
                    queryWrapper.eq("after_sales_response", 10).or().isNull("after_sales_response");
                    queryWrapper.eq("service_satisfaction", 10);
                    // 非满意
                } else if (ProStaConstant.UNSATISFIED.equals(serviceSatisfaction)) {
                    queryWrapper.ne("service_aware", 10).or().isNull("service_aware");
                    queryWrapper.ne("after_sales_personnel", 10).or().isNull("after_sales_personnel");
                    queryWrapper.ne("after_sales_response", 10).or().isNull("after_sales_response");
                    queryWrapper.ne("service_satisfaction", 10).or().isNull("service_satisfaction");
                } else {
                    queryWrapper.eq("service_satisfaction", serviceSatisfaction);
                }
            }
            IPage<EvaluationEntity> filterPage = this.page(evaluationPages, queryWrapper);
            return filterPage;
        }

        // 抽查预估功能判断(默认false，显示全部数据)
        boolean isCheck = evalSearchFilter.getIsChecked();
        if (isCheck) {

            // 1.新增详细信息页面显示条件（售后联系人为空情况，不显示该条数据信息）
//            queryWrapper.isNotNull("after_sales_customer").ne("after_sales_customer", "");
            queryWrapper.isNotNull("after_sales_customer");

            // 2.去除合同结束的数据信息(相较于当前时间)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String currentTime = formatter.format(LocalDateTime.now());
            // System.out.println(currentTime);
            queryWrapper.gt("contract_end_date", currentTime);

            // 3.当前时间大于交维时间三个月的显示
            Date now = new Date();
            Date before3Month;
            // 获取日历
            Calendar calendar = Calendar.getInstance();
            // 当前时间赋值给日历
            calendar.setTime(now);
            // 前3个月
            calendar.add(Calendar.MONTH, -3);
            // 得到3个月之前的时间
            before3Month = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formatBefore3Month = sdf.format(before3Month);
//          System.out.println("formatBefore3Month = " + formatBefore3Month);
            queryWrapper.lt("intersection_date", formatBefore3Month);

            // 4.有回访时间无客户意见且六个月之内（和当前时间比较）的数据信息不显示
            // 显示：无回访时间，有客户意见，六个月之外
            calendar.add(Calendar.MONTH, -3);
            Date before6Month = calendar.getTime();
            String formatBefore6Month = sdf.format(before6Month);
            queryWrapper.and(qw -> {
                qw.lt("revisiting_time",formatBefore6Month)
                        .or()
                        .isNull("revisiting_time");
            });
            //queryWrapper.lt("revisiting_time", formatBefore6Month);

            // 预估
            IPage<EvaluationEntity> checkPages = this.page(evaluationPages, queryWrapper);
            return checkPages;
        }
        // 没有搜索筛选条件，返回所有
        IPage<EvaluationEntity> allPages = page(evaluationPages, queryWrapper);
        return allPages;
    }



    // 整合备份
    public IPage<EvaluationEntity> queryEvaluation(EvalSearchFilterEntity evalSearchFilter) {

        Integer pageNum = evalSearchFilter.getPageNum();
        Integer pageSize = evalSearchFilter.getPageSize();

        IPage<EvaluationEntity> evaluationPages = new Page<>(pageNum, pageSize);
        QueryWrapper<EvaluationEntity> queryWrapper = new QueryWrapper<>();

        // 项目编号
        String projectNum = evalSearchFilter.getProjectNum();
        // 项目名称
        String projectName = evalSearchFilter.getProjectName();
        // 搜索
        if (!StrUtil.isEmpty(projectNum) || !StrUtil.isEmpty(projectName)) {
            if (!StrUtil.isEmpty(projectNum)) {
                queryWrapper.like("project_num", projectNum);
            }
            if (!StrUtil.isEmpty(projectName)) {
                queryWrapper.like("project_name", projectName);
            }
            IPage<EvaluationEntity> searchPage = this.page(evaluationPages, queryWrapper);
            return searchPage;
        }
        // 筛选
        String county = evalSearchFilter.getCounty();
        String serviceSatisfaction = evalSearchFilter.getServiceSatisfaction();
        if (!StrUtil.isEmpty(county) || !StrUtil.isEmpty(serviceSatisfaction)) {
            if (!StrUtil.isEmpty(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isEmpty(serviceSatisfaction)) {
                // 满意
                if (ProStaConstant.SATISFIED.equals(serviceSatisfaction)) {
                    queryWrapper.eq("service_aware", 10).or().isNull("service_aware");
                    queryWrapper.eq("after_sales_personnel", 10).or().isNull("after_sales_personnel");
                    queryWrapper.eq("after_sales_response", 10).or().isNull("after_sales_response");
                    queryWrapper.eq("service_satisfaction", 10);
                    // 非满意
                } else if (ProStaConstant.UNSATISFIED.equals(serviceSatisfaction)) {
                    queryWrapper.ne("service_aware", 10).or().isNull("service_aware");
                    queryWrapper.ne("after_sales_personnel", 10).or().isNull("after_sales_personnel");
                    queryWrapper.ne("after_sales_response", 10).or().isNull("after_sales_response");
                    queryWrapper.ne("service_satisfaction", 10).or().isNull("service_satisfaction");
                } else {
                    queryWrapper.eq("service_satisfaction", serviceSatisfaction);
                }
            }
            IPage<EvaluationEntity> filterPage = this.page(evaluationPages, queryWrapper);
            return filterPage;
        }

        // 1.新增详细信息页面显示条件（售后联系人为空情况，不显示该条数据信息）
        queryWrapper.isNotNull("after_sales_customer").ne("after_sales_customer", "");

        // 2.去除合同结束的数据信息(相较于当前时间)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentTime = formatter.format(LocalDateTime.now());
//        System.out.println(currentTime);
        queryWrapper.gt("contract_end_date", currentTime);

        // 3.当前时间大于交维时间三个月的显示
        Date now = new Date();
        Date before3Month;
        // 获取日历
        Calendar calendar = Calendar.getInstance();
        // 当前时间赋值给日历
        calendar.setTime(now);
        // 前3个月
        calendar.add(Calendar.MONTH, -3);
        // 得到前3月的时间
        before3Month = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formatBefore3Month = sdf.format(before3Month);
//        System.out.println("formatBefore3Month = " + formatBefore3Month);
        queryWrapper.lt("intersection_date", formatBefore3Month);

        // 4.有回访时间无客户意见且六个月之内（和当前时间比较）的数据信息不显示
        // 显示：无回访时间，有客户意见，六个月之外
        calendar.add(Calendar.MONTH, -3);
        Date before6Month = calendar.getTime();
        String formatBefore6Month = sdf.format(before6Month);
        queryWrapper.lt("revisiting_time", formatBefore6Month);

        // 没有搜索筛选条件，返回所有
        IPage<EvaluationEntity> allPages = this.page(evaluationPages, queryWrapper);


        return allPages;
    }



}
