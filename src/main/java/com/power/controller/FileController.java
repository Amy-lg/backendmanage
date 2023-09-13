package com.power.controller;

import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.service.fileservice.BusinessOrderFileService;
import com.power.service.fileservice.TOrderFileService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件导入
 * @since 2023/8
 * @author cyk
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private BusinessOrderFileService businessOrderFileService;

    @Autowired
    private TOrderFileService tOrderFileService;

    /**
     * 业务工单数据导入
     * @param file excel表
     * @return
     */
    @PostMapping("/importBusinessOrder")
    public Result importBusinessOrder(@RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            String substringFileName = originalFilename.substring(0, originalFilename.indexOf("."));
            if ("业务工单".equals(substringFileName)) {
                return ResultUtils.success(businessOrderFileService.importBusinessOrder(file));
            } else if ("小T工单".equals(substringFileName)) {
                return ResultUtils.success(tOrderFileService.importTOrder(file));
            }
        }
        return ResultUtils.error(ResultStatusCode.ERROR_IMPORT, "业务工单数据导入失败，导入的文件为空");
    }
}
