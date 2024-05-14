package com.power.controller.proactiveservicesctl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.dto.NoteInfoEntity;
import com.power.entity.proactiveservicesentity.InspectionOrderEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.InspectionFilterSearchEntity;
import com.power.service.proactiveservice.InspectionService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 巡检工单控制层
 * @author cyk
 * @since 2023/10
 */
@RestController
@RequestMapping("/api/inspection")
public class InspectionController {

    @Autowired
    private InspectionService inspectionService;

    /**
     * 数据导入
     * @param file 巡检工单表
     * @return
     */
    @PostMapping("/import")
    public Result importInspectionOrderFile(@RequestParam MultipartFile file,
                                            @RequestParam MultipartFile orderTimeFile) {
        String importResult = inspectionService.importInspectionOrderExcel(file, orderTimeFile);
        if (importResult != null) {
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、筛选（参数中只有pageNum,pageSize时表示分页查询所有）
     * @param inspectionFilterSearch
     * @return
     */
    @PostMapping("/searchOrFilterInfo")
    public Result searchInspectionOrderInfo(@RequestBody InspectionFilterSearchEntity inspectionFilterSearch) {

        if (inspectionFilterSearch != null) {
            IPage<InspectionOrderEntity> inspectionOrderIPage = inspectionService.searchOrFilter(inspectionFilterSearch);
            if (inspectionOrderIPage != null) {
                return ResultUtils.success(inspectionOrderIPage);
            }
            return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
        }
        return ResultUtils.success();
    }


    /**
     * 巡检工单月份处理数量统计
     * @return
     */
    @GetMapping("/calcMonCountOfInspectionOrder")
    public Result calculateCountOfDealingOrder() {
        Map<String, Object> dealingOrderCountMap = new HashMap<>();
        List<Object> count = inspectionService.countOfInspectionOrder();
        if (!count.isEmpty() && count != null) {
            dealingOrderCountMap.put("巡检工单月份处理数量", count);
            return ResultUtils.success(dealingOrderCountMap);
        }
        return ResultUtils.success();
    }


    /**
     * 备注提交接口
     * @param noteInfoEntity
     * @return
     */
    @PostMapping("/updNote")
    public Result modifyNote(@RequestBody NoteInfoEntity noteInfoEntity) {

        String updateNoteRes = inspectionService.updateNote(noteInfoEntity);
        if (updateNoteRes != null) {
            return ResultUtils.success(updateNoteRes);
        }
        return ResultUtils.error(501, "设置备注信息的订单编号为空，请选择订单编号");
    }


    /**
     * 计算合格率
     * @return
     */
    @GetMapping("/calcQuaRate")
    public Result calcOrderPassRate() {

        // 巡检工单当前月份各区县数量
        Map<String, String> insOrderCountMap = inspectionService.insOrderCountOfCurrentMonth();

        if (insOrderCountMap != null) {
            return ResultUtils.success(insOrderCountMap);
        }
        return ResultUtils.success();
    }


}
