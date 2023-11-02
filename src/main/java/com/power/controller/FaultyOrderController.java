package com.power.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.entity.fileentity.TOrderEntity;
import com.power.service.FaultyOrderService;
import com.power.service.fileservice.BusinessOrderFileService;
import com.power.service.fileservice.TOrderFileService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 故障工单概述控制层
 * @since 2023/9
 * @author cyk
 */
@RestController
@RequestMapping("/api/faulty")
public class FaultyOrderController {

    @Autowired
    private FaultyOrderService faultyOrderService;

    @Autowired
    private TOrderFileService tOrderFileService;

    @Autowired
    private BusinessOrderFileService businessOrderFileService;

    /**
     * 主页面ECharts故障工单概况-折线图表
     * 统计月份故障工单数量
     * @return
     */
    @GetMapping("/queryFaultyCount")
    public Result queryFaultyOrderInfo() {
        Map<String, Object> faultyOrderCountMap = new HashMap<>();
        // 故障工单每月数量
        List<Object> faultyOrderNum = faultyOrderService.getFaultyOrderNum();
        // 小T工单每月数量
        List<Object> tOrderCount = tOrderFileService.getTOrderCount();
        faultyOrderCountMap.put("业务工单数量", faultyOrderNum);
        faultyOrderCountMap.put("小T工单数量", tOrderCount);
        if (faultyOrderCountMap != null) {
            return ResultUtils.success(faultyOrderCountMap);
        }
        return ResultUtils.success();
    }


    /**
     * 业务工单故障查询筛选功能
     * @param pageNum 当前页码
     * @param pageSize 当前页显示数据条数
     * @param orderNum 工单号
     * @param dates 筛选时，筛选的日期时间段
     * @return
     */
    @GetMapping("/searchOrFilter")
    public Result searchOrFilter(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                 @RequestParam(required = false) String orderNum,
                                 @RequestParam(required = false) List<String> dates) {

        IPage<BusinessOrderEntity> pages = businessOrderFileService.queryOrFilter(pageNum, pageSize, orderNum, dates);
        if (pages != null) {
            return ResultUtils.success(pages);
        } else {
            return ResultUtils.success();
        }
    }


    /**
     * 业务工单新增、修改（修改工单状态）接口
     * @param businessOrder
     * @return
     */
    @PostMapping("/updateBusinessOrder")
    public Result updateBusinessOrder(@RequestBody BusinessOrderEntity businessOrder) {

        String addResult = businessOrderFileService.addBusinessOrder(businessOrder);
        if (addResult != null) {
            return ResultUtils.success(addResult);
        }
        return ResultUtils.success();
    }


    /**
     * 小T工单故障查询筛选功能
     * @param pageNum 当前页码
     * @param pageSize 当前页显示数据条数
     * @param orderNum 工单号
     * @param dates 筛选时，筛选的日期时间段
     * @return
     */
    @GetMapping("/searchOrFilterTOrder")
    public Result searchOrFilterTOrder(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                 @RequestParam(required = false) String orderNum,
                                 @RequestParam(required = false) List<String> dates) {

        IPage<TOrderEntity> tOrderIPage = tOrderFileService.queryOrFilterTOrder(pageNum, pageSize, orderNum, dates);
        if (tOrderIPage != null) {
            return ResultUtils.success(tOrderIPage);
        } else {
            return ResultUtils.success();
        }
    }


    /**
     * 小T工单新增、修改（修改工单状态）接口
     * @param tOrderEntity
     * @return
     */
    @PostMapping("/updateTOrder")
    public Result updateTOrder(@RequestBody TOrderEntity tOrderEntity) {

        String addResult = tOrderFileService.addLittleTOrder(tOrderEntity);
        if (addResult != null) {
            return ResultUtils.success(addResult);
        }
        return ResultUtils.success();
    }


}
