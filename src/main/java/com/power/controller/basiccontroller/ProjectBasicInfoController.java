package com.power.controller.basiccontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.basic.BasicInfoEntity;
import com.power.entity.basic.filtersearch.BasicFilterEntity;
import com.power.service.basicservice.ProjectBasicInfoService;
import com.power.service.basicservice.ProjectDataInfoService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 项目基础信息控制层
 * @author cyk
 * @since 2023/11
 */
@RestController
@RequestMapping("/api/proBasInfo")
public class ProjectBasicInfoController {

    @Autowired
    private ProjectBasicInfoService basicInfoService;

    @Autowired
    private ProjectDataInfoService projectDataInfoService;


    /**
     * 项目全部基本信息
     * @param file
     * @return
     */
    @PostMapping("/import")
    public Result importProjectInfoFile(@RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            String importResult = basicInfoService.importProjectInfoExcel(file);
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }


    /**
     * 查询、筛选功能
     * @param basicFilterEntity 筛选model
     * @return
     */
    @PostMapping("/filterSearch")
    public Result filterByCondition(@RequestBody BasicFilterEntity basicFilterEntity) {

        // 判断请求体是否为空
        if (basicFilterEntity != null) {
            IPage<BasicInfoEntity> pages = basicInfoService.filterOrSearchByCondition(basicFilterEntity);
            return ResultUtils.success(pages);
        } else {
            // 返回空数据
            return ResultUtils.success();
        }
    }


    /**
     * 查询区县在维项目数
     * @return
     */
    @GetMapping("/getMaintenanceNCount")
    public Result getMaintenanceNum() {

        // Map<String, Object> maintenanceNumMap = basicInfoService.getMaintenanceNum();
        Map<String, Object> maintenanceNumMap = projectDataInfoService.getMaintenanceNum();
        if (maintenanceNumMap != null) {
            return ResultUtils.success(maintenanceNumMap);
        }else {
            // 返回空
            return ResultUtils.success();
        }
    }


}
