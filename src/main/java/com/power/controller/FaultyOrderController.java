package com.power.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ProStaConstant;
import com.power.entity.basic.BasicInfoEntity;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.entity.fileentity.TOrderEntity;
import com.power.service.FaultyOrderService;
import com.power.service.basicservice.ProjectBasicInfoService;
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

    // 获取项目概况所有数据，匹配T工单区县
    @Autowired
    private ProjectBasicInfoService basicInfoService;

    /**
     * 主页面ECharts故障工单概况-折线图表
     * 统计月份故障工单数量
     * @return
     */
    @GetMapping("/queryFaultyCount")
    public Result queryFaultyOrderInfo() {
        Map<String, Object> faultyOrderCountMap = new HashMap<>();
        // 故障工单每月数量
//        List<Object> faultyOrderNum = faultyOrderService.getFaultyOrderNum();
        List<Integer> faultyOrderNum = faultyOrderService.faultyCountOfBefore12Month();
        // 小T工单每月数量
        List<Integer> tOrderCount = tOrderFileService.tOrderOfBefore12Month();
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


    /**
     * 能力分析模块---工单总数
     * @return
     */
    @GetMapping("/getAllOrder")
    public Result getOrderCount() {

        Map<String, Object> map = new HashMap<>();
        // 工单总数
        int orderCount = 0;
        // T工单数量统计
        int tOrderCount = tOrderFileService.getTOrderOfSum();
        map.put(ProStaConstant.T_ORDER_COUNT, tOrderCount);
        // 业务工单数量统计
        int businessOrderCount = businessOrderFileService.getBOrderOfSum();
        map.put(ProStaConstant.B_ORDER_COUNT, businessOrderCount);
        // 总工单数量
        orderCount = tOrderCount + businessOrderCount;
        map.put(ProStaConstant.ORDER_COUNT, orderCount);

        // 未完结工单
        int orderCountOfUnfinished = 0;
        // T工单未完结数量
        int tOrderCountOfUnfinished = tOrderFileService.getTOrderOfUnfinished();
        // 业务工单未完结数量
        int bOrderCountOfUnfinished = businessOrderFileService.getBOrderOfUnfinished();
        orderCountOfUnfinished = tOrderCountOfUnfinished + bOrderCountOfUnfinished;
        map.put(ProStaConstant.UNFINISHED_ORDER, orderCountOfUnfinished);

        // 计算工单处理平均时长
        float tOrderDuration = tOrderFileService.getTOrderOfDuration();
        float bOrderDuration = businessOrderFileService.getBOrderOfDuration();
        String aveTDuration = "0.000";
        String aveBusDuration = "0.000";
        if (tOrderCount != 0) {
            aveTDuration = String.format("%.3f", (tOrderDuration / tOrderCount));
            map.put("小T工单平均处理时长", aveTDuration);
        }
        if (businessOrderCount != 0) {
            aveBusDuration = String.format("%.3f", (bOrderDuration / businessOrderCount));
            map.put("业务工单平均处理时长", aveBusDuration);
        }

        // 业务工单前6月统计，县分
        List<Object> busOrderOfBefore6Month = businessOrderFileService.getBusOrderOfBefore6Month();
        map.put("业务工单前6月工单统计", busOrderOfBefore6Month);

        // 小T工单前6月统计，县分
        List<BasicInfoEntity> basicInfoEntityList = basicInfoService.list();
        List<Map<String, Object>> tOrderOfBefore6Month = tOrderFileService.getTOrderOfBefore6Month(basicInfoEntityList);
        map.put("小T工单前6月工单统计", tOrderOfBefore6Month);

        // 业务工单月份故障平均处理时长
        List<Map<String, Object>> busOrderAveDurByCounty = businessOrderFileService.calcBOrderAveDurationByCounty();
        map.put("业务工单月份故障平均处理时长", busOrderAveDurByCounty);

        List<Map<String, Object>> tOrderAveDurByCounty = tOrderFileService.calcTOrderAveDurationByCounty();
        map.put("t工单月份故障平均处理时长", tOrderAveDurByCounty);

        return ResultUtils.success(map);
    }


}
