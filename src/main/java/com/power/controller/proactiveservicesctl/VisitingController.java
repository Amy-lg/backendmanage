package com.power.controller.proactiveservicesctl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.proactiveservicesentity.VisitingOrderEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.VisitingFilterSearchEntity;
import com.power.service.proactiveservice.VisitingService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 走访工单控制层
 * @author cyk
 * @since 2023/10
 */
@RestController
@RequestMapping("/api/visiting")
public class VisitingController {

    @Autowired
    private VisitingService visitingService;

    /**
     * 数据导入
     * @param file 走访工单表
     * @return
     */
    @PostMapping("/import")
    public Result importVisitingOrderFile(@RequestParam MultipartFile file,
                                          @RequestParam MultipartFile orderTimeFile) {
        String importResult = visitingService.importVisitingOrderExcel(file, orderTimeFile);
        if (importResult != null) {
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }

    // 走访工单数据时间导入（以便于多表联查，显示主页中的折线图中数量）
//    @PostMapping("/importDealTime")
//    public Result importVisitingOrderDealTimeFile(@RequestParam MultipartFile orderTimeFile) {
//        String str = visitingService.importVisitingOrderDealTimeExcel(orderTimeFile);
//        return ResultUtils.success(str);
//    }


    /**
     * 搜索、筛选（参数中只有pageNum,pageSize时表示分页查询所有）
     * @param visitingFilterSearch
     * @return
     */
    @PostMapping("/searchOrFilterInfo")
    public Result searchVisitingOrderInfo(@RequestBody VisitingFilterSearchEntity visitingFilterSearch) {

        if (visitingFilterSearch != null) {
            IPage<VisitingOrderEntity> visitingOrderIPage = visitingService.searchOrFilter(visitingFilterSearch);
            if (visitingOrderIPage != null) {
                return ResultUtils.success(visitingOrderIPage);
            }
            return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
        }
        return ResultUtils.success();
    }


    /**
     * 月份工单处理数量
     * @return
     */
    @GetMapping("/calcMonCountOfVisitingOrder")
    public Result calculateCountOfDealingOrder() {

        Map<String, Object> dealingOrderCountMap = new HashMap<>();
        List<Object> count = visitingService.countOfVisitingOrder();
        if (!count.isEmpty() && count != null) {
            dealingOrderCountMap.put("走访工单月份处理数量", count);
            return ResultUtils.success(dealingOrderCountMap);
        }
        return ResultUtils.success();
    }


}
