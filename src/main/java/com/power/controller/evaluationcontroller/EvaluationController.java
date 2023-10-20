package com.power.controller.evaluationcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.evaluation.EvaluationEntity;
import com.power.entity.evaluation.searchfilter.EvalSearchFilterEntity;
import com.power.service.evaluationservice.EvaluationService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 客户满意度表信息导入
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
}
