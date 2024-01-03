package com.power.controller.ordercontroller;

import com.power.common.Result;
import com.power.service.fileservice.BusinessOrderFileService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 业务工单控制层
 * @author cyk
 * @since 2024/1
 */
@RestController
@RequestMapping("/api/bOrder")
public class BusinessOrderController {

    @Autowired
    private BusinessOrderFileService businessOrderFileService;

    @PostMapping("/import")
    public Result importBusinessOrderFile(@RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            String importResult = businessOrderFileService.importBusinessOrderExcel(file);
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }
}
