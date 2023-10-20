package com.power.service.evaluationservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.evaluation.EvaluationEntity;
import com.power.entity.evaluation.searchfilter.EvalSearchFilterEntity;
import com.power.mapper.evaluationmapper.EvaluationMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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
     * 数据表分析
     * @param evaluationFile
     * @return
     */
    private List<EvaluationEntity> importData(MultipartFile evaluationFile) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(evaluationFile);
        EvaluationEntity evaluation;
        List<EvaluationEntity> evaluationEntityArrayList = new ArrayList<>();

        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                    int lastRowNum = sheet.getLastRowNum();
                    for (int j = 1; j <= lastRowNum; j++) {
                        Row contentRow = sheet.getRow(j);
                        short lastCellNum = contentRow.getLastCellNum();
                        evaluation = new EvaluationEntity();
                        String cellValue = null;
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
                                    k += 13;
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
                                case 35:
                                    evaluation.setRevisitingTime(cellValue);
                                    break;
                                default:
                                    break;
                            }
                        }
                        evaluationEntityArrayList.add(evaluation);
                    }
                }
                continue;
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
        // 没有搜索筛选条件，返回所有
        IPage<EvaluationEntity> allPages = this.page(evaluationPages);
        return allPages;
    }
}
