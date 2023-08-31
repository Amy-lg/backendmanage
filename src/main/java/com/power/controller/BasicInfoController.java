package com.power.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.service.BasicInfoService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 项目基本情况数据信息控制层
 * @since 2023/8
 * @author cyk
 */
@RestController
@RequestMapping("/basicInfo")
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
            return ResultUtils.success(basicInfoService.getMaintenanceNum());
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
}
