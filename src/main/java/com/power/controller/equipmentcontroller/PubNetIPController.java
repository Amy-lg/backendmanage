package com.power.controller.equipmentcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.PubNetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 公网IP拨测控制层
 * @since 2023/9
 * @author cyk
 */
@RestController
@RequestMapping("/api/pubNetIp")
public class PubNetIPController {

    @Autowired
    private PubNetIPService pubNetIPService;

    /**
     * 数据导入
     * @param file 公网ip拨测excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importPubNetFile(@RequestParam MultipartFile file) {
        String importResult = pubNetIPService.importPubNetIPExcel(file);
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
    public Result queryPubNetIPInfo(@RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize) {

        IPage<PubNetIPEntity> pubNetIpPages = pubNetIPService.queryPubNetworkIPInfo(pageNum, pageSize);
        if (pubNetIpPages != null) {
            return ResultUtils.success(pubNetIpPages);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、筛选
     * @param dialFilterQuery
     * @return
     */
    @PostMapping("/searchOrFilterInfo")
    public Result searchIntranetIPInfo(@RequestBody DialFilterQuery dialFilterQuery) {

        IPage<PubNetIPEntity> pubNetIPEntityIPage = pubNetIPService.searchOrFilter(dialFilterQuery);
        if (pubNetIPEntityIPage != null) {
            return ResultUtils.success(pubNetIPEntityIPage);
        }
        return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
    }

}
