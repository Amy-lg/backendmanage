package com.power.controller.equipmentcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.equipment.IndustryVideoEntity;
import com.power.entity.equipment.PubNetWebEntity;
import com.power.service.equipmentservice.PubNetWebService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 公网WebIP拨测控制层
 * @since 2023/9
 * @author cyk
 */
@RestController
@RequestMapping("/api/pubNet")
public class PubNetWebController {

    @Autowired
    private PubNetWebService pubNetWebService;

    /**
     * 数据导入接口
     * @param file 公网web拨测excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importPubNetFile(@RequestParam MultipartFile file) {
        if (!file.isEmpty()) {
            String importResultInfo = pubNetWebService.importPubNetExcel(file);
            return ResultUtils.success(importResultInfo);
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
    public Result queryPubNetInfo(@RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize) {

        IPage<PubNetWebEntity> pubNetWebPages = pubNetWebService.queryPubNetworkInfo(pageNum, pageSize);
        if (pubNetWebPages != null) {
            return ResultUtils.success(pubNetWebPages);
        }
        return ResultUtils.success();
    }
}
