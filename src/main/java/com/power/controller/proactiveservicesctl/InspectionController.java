package com.power.controller.proactiveservicesctl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.proactiveservicesentity.InspectionOrderEntity;
import com.power.entity.proactiveservicesentity.VisitingOrderEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.InspectionFilterSearchEntity;
import com.power.entity.proactiveservicesentity.visitingfiltersearch.VisitingFilterSearchEntity;
import com.power.service.proactiveservice.InspectionService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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
    public Result importInspectionOrderFile(@RequestParam MultipartFile file) {
        String importResult = inspectionService.importInspectionOrderExcel(file);
        if (importResult != null) {
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、筛选（参数中只有pageNum,pageSize时表示分页查询所有）
     * @param visitingFilterSearch
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

}
