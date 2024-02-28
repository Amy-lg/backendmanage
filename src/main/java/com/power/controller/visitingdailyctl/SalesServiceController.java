package com.power.controller.visitingdailyctl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.summary.SalesServiceEntity;
import com.power.entity.summary.pojo.SalesServicePojo;
import com.power.service.dailyservice.SalesService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 走访日常
 * @author cyk
 * @since 2024/2
 */
@RestController
@RequestMapping("/api/visitDaily")
public class SalesServiceController {

    @Autowired
    private SalesService salesService;

    /**
     * 售后日常存储
     * @param salesServicePojo
     * @return
     */
    @PostMapping("/update")
    public Result saveSalesDaily(@RequestBody SalesServicePojo salesServicePojo) {
        int updResultStr = salesService.updateVisitingDaily(salesServicePojo);
        List<Object> respResultList = new ArrayList<>();
        respResultList.add(updResultStr);
        if (updResultStr != 1) {
            respResultList.add(ResultStatusCode.SUCCESS_UPDATE_INFO.getMsg());
            return ResultUtils.success(respResultList);
        }else {
            respResultList.add(ResultStatusCode.ERROR_1.getMsg());
            return ResultUtils.success(respResultList);
        }
    }


    /**
     * 走访日常页面显示
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/getVDly")
    public Result getAllVisitingDaily(@RequestParam Integer pageNum,
                                      @RequestParam Integer pageSize,
                                      @RequestParam(required = false) String associatedProjectNum,
                                      @RequestParam(required = false) String county) {
        IPage<SalesServiceEntity> salesServiceEntityIPage = salesService
                .getAllVisitingDaily(pageNum,pageSize,associatedProjectNum,county);
        List<Object> visitingDailyList = new ArrayList<>();
        if (salesServiceEntityIPage != null) {
            visitingDailyList.add(ResultStatusCode.SUCCESS_SEARCH.getCode());
            visitingDailyList.add(ResultStatusCode.SUCCESS_SEARCH.getMsg());
            visitingDailyList.add(salesServiceEntityIPage);
            return ResultUtils.success(visitingDailyList);
        }
        return ResultUtils.success();
    }
}
