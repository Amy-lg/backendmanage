package com.power.controller.faultcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.fault.FaultTrackingEntity;
import com.power.entity.fault.filtersearch.FaultFilterSearch;
import com.power.service.faultservice.FaultTrackingService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/fault")
public class FaultTrackingController {

    @Autowired
    private FaultTrackingService faultTrackingService;


    /**
     * 数据导入
     * @param file
     * @return
     */
    @PostMapping("/import")
    public Result importFaultData(@RequestParam("file") MultipartFile file) {

        String importResult = faultTrackingService.importFaultTrackingFile(file);
        List<Object> importMsgList = new ArrayList<>();
        if (importResult != null) {
            importMsgList.add(ResultStatusCode.SUCCESS_UPLOAD.getCode());
            importMsgList.add(ResultStatusCode.SUCCESS_UPLOAD.getMsg());
            return ResultUtils.success(importMsgList);
        } else {
            importMsgList.add(ResultStatusCode.ERROR_IMPORT.getCode());
            importMsgList.add(ResultStatusCode.ERROR_IMPORT.getMsg());
            return ResultUtils.success(importMsgList);
        }
    }


    /**
     * 搜索、筛选
     * @param faultFilterSearch
     * @return
     */
    @PostMapping("/filOrSea")
    public Result faultTrackingFilterSearch(@RequestBody FaultFilterSearch faultFilterSearch) {

        IPage<FaultTrackingEntity> resultPages = faultTrackingService.filterSearchByCondition(faultFilterSearch);
        if (resultPages != null) {
            return ResultUtils.success(resultPages);
        }
        return ResultUtils.success();
    }


}
