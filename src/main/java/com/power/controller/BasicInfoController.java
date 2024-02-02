package com.power.controller;

import com.power.common.Result;
import com.power.entity.query.FilterModelQuery;
import com.power.service.BasicInfoService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目基本情况数据信息控制层
 * @since 2023/8
 * @author cyk
 */
@RestController
@RequestMapping("/api/basicInfo")
public class BasicInfoController {

    @Autowired
    private BasicInfoService basicInfoService;

    /**
     * 查询区县在维项目数
     * @return
     */
    @GetMapping("/getMaintenanceNum")
    public Result getMaintenanceNum() {
        Map<String, Object> maintenanceNumMap = basicInfoService.getMaintenanceNum();
        if (maintenanceNumMap != null) {
            return ResultUtils.success(maintenanceNumMap);
        }else {
            // 返回空
            return ResultUtils.success();
        }
    }

    /**
     * 各区县详细信息数据
     * @param pageNum 页面数量
     * @param pageSize 页面显示条数
     * @param countyName 需要查询的区县名称
     * @return
     */
    @GetMapping("/getCountyDetail")
    public Result getCountyDetail(@RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize,
                                  @RequestParam String countyName) {

        if (StringUtils.hasLength(countyName) && !countyName.isEmpty()) {
            return ResultUtils.success(basicInfoService.getCountiesDetail(pageNum, pageSize, countyName));
        } else {
            return ResultUtils.success();
        }
    }

    /**
     * 筛选功能
     * @param filterModelQuery 筛选model
     * @return
     */
    @PostMapping("/filter")
    public Result filterByCondition(@RequestBody FilterModelQuery filterModelQuery) {

        // 判断请求体是否为空
        if (filterModelQuery != null) {
            return ResultUtils.success(basicInfoService.filterByCondition(filterModelQuery));
        } else {
            // 返回空数据
            return ResultUtils.success();
        }
    }
}
