package com.power.controller.ordercontroller;

import com.power.common.Result;
import com.power.service.fileservice.TOrderFileService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小T工单控制层
 * @author cyk
 * @since 2024/1
 */
@RestController
@RequestMapping("/api/tOrder")
public class TOrderController {

    @Autowired
    private TOrderFileService tOrderFileService;

    @PostMapping("/import")
    public Result importTOrderFile(@RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            String importResult = tOrderFileService.importTOrderExcel(file);
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }
}
