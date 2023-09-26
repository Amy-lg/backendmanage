package com.power.controller.equipmentcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.equipment.IntranetIPEntity;
import com.power.entity.equipment.PubNetIPEntity;
import com.power.service.equipmentservice.IntranetIPService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/intranet")
public class IntranetIPController {

    @Autowired
    private IntranetIPService intranetIPService;

    /**
     * 数据导入
     * @param file 内网ip拨测excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importIntranetIpFile(@RequestParam MultipartFile file) {
        String importResult = intranetIPService.importIntranetIPExcel(file);
        if (importResult != null) {
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/queryInfo")
    public Result queryIntranetIPInfo(@RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {

        IPage<IntranetIPEntity> intranetIpPages = intranetIPService.queryIntranetIPInfo(pageNum, pageSize);
        if (intranetIpPages != null) {
            return ResultUtils.success(intranetIpPages);
        }
        return ResultUtils.success();
    }
}
