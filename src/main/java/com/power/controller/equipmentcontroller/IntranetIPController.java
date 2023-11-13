package com.power.controller.equipmentcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.IntranetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.service.equipmentservice.IndustryVideoService;
import com.power.service.equipmentservice.IntranetIPService;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.service.equipmentservice.PubNetWebService;
import com.power.utils.CalculateUtils;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/intranet")
public class IntranetIPController {

    @Autowired
    private IntranetIPService intranetIPService;

    // 用于在线率查询服务
    @Autowired
    private IndustryVideoService videoService; // 业务视频

    @Autowired
    private PubNetIPService pubNetIPService; // 公网IP

    @Autowired
    private PubNetWebService pubNetWebService; // 公网web

    /**
     * 数据导入
     * @param file 内网ip拨测excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importIntranetIpFile(@RequestParam MultipartFile file) {
        String importResult = intranetIPService.importIntranetIPExcel(file);
        if (importResult != null && !importResult.equals(ResultStatusCode.ERROR_IMPORT.getMsg())) {
            return ResultUtils.success(importResult);
        }
        return ResultUtils.error(5003, ResultStatusCode.ERROR_IMPORT_001.getMsg());
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


    /**
     * 搜索、筛选
     * @param dialFilterQuery
     * @return
     */
    @PostMapping("/searchOrFilterInfo")
    public Result searchIntranetIPInfo(@RequestBody DialFilterQuery dialFilterQuery) {

        if (dialFilterQuery != null) {
            IPage<IntranetIPEntity> intranetIPEntityIPage = intranetIPService.searchOrFilter(dialFilterQuery);
            if (intranetIPEntityIPage != null) {
                return ResultUtils.success(intranetIPEntityIPage);
            }
            return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
        }
        return ResultUtils.success();
    }

    /**
     * 各个区县在线率计算
     * @return
     */
    @GetMapping("/onlineRate")
    public Result calculateOnlineRate() {

        // 1.四张表区县在线总数量
        // 内网IP拨测表 区县在线总数量
        Map<String, Long> intranetIPCountyCount = intranetIPService.queryAllOnlineCount();
        // 公网IP拨测表 区县在线总数量
        Map<String, Long> pubNetIPCountyCount = pubNetIPService.queryAllOnlineCount();
        // 公网web拨测表 区县在线总数量
        Map<String, Long> pubNetWebCountyCount = pubNetWebService.queryAllOnlineCount();
        // 行业视频表 区县在线总数量
        Map<String, Long> industryVideoCountyCount = videoService.queryAllOnlineCount();

        // 2.查询各个区县所有数量
        Map<String, Long> intranetIPAllCountMap = intranetIPService.queryAllCount();
        Map<String, Long> pubNetIPAllCountMap = pubNetIPService.queryAllCount();
        Map<String, Long> pubNetWebAllCountMap = pubNetWebService.queryAllCount();
        Map<String, Long> videoAllCountMap = videoService.queryAllCount();

        // 调用工具类计算方法
        Map<String, Object> countyOnlineRateResultMap = CalculateUtils.calculateCountyOnlineRate(intranetIPCountyCount, pubNetIPCountyCount,
                pubNetWebCountyCount, industryVideoCountyCount,
                intranetIPAllCountMap, pubNetIPAllCountMap,
                pubNetWebAllCountMap, videoAllCountMap);
        if (countyOnlineRateResultMap != null) {
            return ResultUtils.success(countyOnlineRateResultMap);
        }
        return ResultUtils.success();
    }
}
