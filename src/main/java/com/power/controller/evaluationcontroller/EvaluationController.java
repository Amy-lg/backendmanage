package com.power.controller.evaluationcontroller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.evaluation.EvaluationEntity;
import com.power.entity.evaluation.searchfilter.EvalSearchFilterEntity;
import com.power.service.evaluationservice.EvaluationService;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.ResultUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 客户满意度控制层
 * @author cyk
 * @since 2023/10
 */
@RestController
@RequestMapping("/api/customer")
public class EvaluationController {


    @Autowired
    private EvaluationService evaluationService;


    /**
     * 客户满意度表信息导入
     * @param file
     * @return
     */
    @PostMapping("/import")
    public Result importEvaluationFile(@RequestParam MultipartFile file) {

        String importResult = evaluationService.importEvaluationExcel(file);
        if (importResult != null) {
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、筛选操作
     * @param evalSearchFilter
     * @return
     */
    @PostMapping("/searchOrFilterEvaluation")
    public Result searchOrFilterCustomerEvaluation(@RequestBody EvalSearchFilterEntity evalSearchFilter) {

        if (evalSearchFilter != null) {
            IPage<EvaluationEntity> evalSearchFilterIPage = evaluationService.searchOrFilterEvaluation(evalSearchFilter);
            if (evalSearchFilterIPage != null) {
                return ResultUtils.success(evalSearchFilterIPage);
            }
            return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
        }
        return ResultUtils.success();
    }


    /**
     * 各区县满意、非满数量;满意度平均分
     * @return
     */
    @GetMapping("/averScoreAndCount")
    public Result averScoreAndCount() {

        List<Map<String, String>> calcResult = evaluationService.calcAverScoreAndCount();
        if (calcResult != null) {
            return ResultUtils.success(calcResult);
        }
        return ResultUtils.success();
    }


    /**
     * 导出筛选检索结果
     * @param response 响应体
     * @param evalSearchFilter 筛选检索条件体
     */
    @PostMapping("/exportEval")
    public void exportDataFile(HttpServletResponse response, @RequestBody EvalSearchFilterEntity evalSearchFilter) {

        // 查询到所有数据信息
        List<EvaluationEntity> evaluationList = evaluationService.searchOrFilterByExport(evalSearchFilter);
        try {
            // 通过工具类创建writer
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题别名
            writer.addHeaderAlias("county", "区县");
            writer.addHeaderAlias("projectNum", "项目编号");
            writer.addHeaderAlias("projectName", "项目名称");
            writer.addHeaderAlias("customerName", "集团客户名称");
            writer.addHeaderAlias("afterSalesCustomer", "售后集团客户联系人");
            writer.addHeaderAlias("afterSalesPhone", "售后集团客户联系人电话");
            writer.addHeaderAlias("intersectionDate", "交维时间");
            writer.addHeaderAlias("contractEndDate", "合同履行结束时间");
            writer.addHeaderAlias("serviceAware", "业务感知");
            writer.addHeaderAlias("afterSalesPersonnel", "售后人员");
            writer.addHeaderAlias("afterSalesResponse", "售后响应");
            writer.addHeaderAlias("serviceSatisfaction", "整体服务满意度");
            writer.addHeaderAlias("customerAdvisement", "客户意见");
            writer.addHeaderAlias("problemDescription", "问题描述");
            writer.addHeaderAlias("revisitingTime", "回访时间");

            writer.write(evaluationList, true);
            String fileName = "客户满意度表";
            // 设置导出Excel的文件格式信息
            AnalysisExcelUtils.settingExcelFileFormat(response, fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            writer.flush(outputStream, true);
            // 关闭writer，释放内存
            writer.close();
            IoUtil.close(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
