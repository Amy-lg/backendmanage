package com.power.controller.summarycontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.summary.ProjectSummaryEntity;
import com.power.entity.summary.pojo.ProjectSummaryPojo;
import com.power.service.summaryservice.ProjectSummaryService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目小结控制层
 * @author cyk
 * @since 2024/1
 */
@RestController
@RequestMapping("/api/summary")
public class ProjectSummaryController {

    @Autowired
    private ProjectSummaryService summaryService;

    // 页面新建
    @PostMapping("/update")
    public Result saveSummary(@RequestBody ProjectSummaryPojo summaryPojo) {
        int updResultStr = summaryService.updateSummary(summaryPojo);
        List<Object> respResultList = new ArrayList<>();
        respResultList.add(updResultStr);
        if (updResultStr != 1) {
            respResultList.add(ResultStatusCode.SUCCESS_UPDATE_INFO.getCode());
            respResultList.add(ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg());
            return ResultUtils.success(respResultList);
        }else {
            respResultList.add(ResultStatusCode.ERROR_1.getCode());
            respResultList.add(ResultStatusCode.ERROR_1.getMsg());
            return ResultUtils.success(respResultList);
        }
    }


    /**
     * 页面显示
     * @param pageNum
     * @param pageSize
     * @param associatedProjectNum 检索项目编号
     * @return 查询结果
     */
    @GetMapping("/getSummary")
    public Result getAllSummary(@RequestParam Integer pageNum,
                                @RequestParam Integer pageSize,
                                @RequestParam(required = false) String associatedProjectNum,
                                @RequestParam(required = false) String county) {

        IPage<ProjectSummaryEntity> summaryEntityIPage = summaryService
                .getAllProjectSummary(pageNum,pageSize,associatedProjectNum,county);
        List<Object> summaryList = new ArrayList<>();
        if (summaryEntityIPage != null) {
            summaryList.add(ResultStatusCode.SUCCESS_SEARCH.getCode());
            summaryList.add(ResultStatusCode.SUCCESS_SEARCH.getMsg());
            summaryList.add(summaryEntityIPage);
            return ResultUtils.success(summaryList);
        }
        return ResultUtils.success();
    }


}
