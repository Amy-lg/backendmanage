package com.power.controller.equipmentcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.dto.ProjectOnlineRateDTO;
import com.power.entity.equipment.IntranetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.service.equipmentservice.IndustryVideoService;
import com.power.service.equipmentservice.IntranetIPService;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.service.equipmentservice.PubNetWebService;
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

        // 存储各区县在线总数
        Map<String, Long> calculateResultMap = new HashMap<>();
        // 存储内网IP拨测和公网IP拨测表 各区县在线总数
        Map<String, Long> intranetIPAndPubNetIPSumMap = new HashMap<>();
        // 存储公网web拨测和行业视频表 各区县在线总数
        Map<String, Long> pubNetWebAndIndustryVideoSumMap = new HashMap<>();
        // 两个表之间遍历 遍历map集合
        Set<Map.Entry<String, Long>> entries = intranetIPCountyCount.entrySet();
        for (Map.Entry<String, Long> entry : entries) {
            String key = entry.getKey();
            Long value = entry.getValue();
            Set<String> stringSet = pubNetIPCountyCount.keySet();
            for (String str : stringSet) {
                if (key == str) {
                    Long aLong = pubNetIPCountyCount.get(str);
                    Long val = value + aLong;
                    intranetIPAndPubNetIPSumMap.put(str, val);
                }
            }
        }

        // 其他两张表
        for (Map.Entry<String, Long> entry : pubNetWebCountyCount.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            Set<String> strings = industryVideoCountyCount.keySet();
            for (String str : strings) {
                if (key == str) {
                    Long aLong = industryVideoCountyCount.get(str);
                    Long val = value + aLong;
                    pubNetWebAndIndustryVideoSumMap.put(str, val);
                }
            }
        }

        for (Map.Entry<String, Long> entry : intranetIPAndPubNetIPSumMap.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            Set<String> strings = pubNetWebAndIndustryVideoSumMap.keySet();
            for (String str : strings) {
                if (key == str) {
                    Long aLong = pubNetWebAndIndustryVideoSumMap.get(str);
                    Long val = value + aLong;
                    calculateResultMap.put(str, val);
                }
            }
        }

        // 在线区县数量转换；嘉禾-->要客  南湖、秀洲-->嘉禾
        Long nanHuAndXuiZhouOnlineSum = 0L;
        Set<Map.Entry<String, Long>> countyEntryModify = calculateResultMap.entrySet();
        for (Map.Entry<String, Long> countyEntry : countyEntryModify) {
            String countyKey = countyEntry.getKey();
            if (ProStaConstant.JIA_HE == countyKey) {
                Long jiaHeCount = countyEntry.getValue();
                calculateResultMap.replace(ProStaConstant.CUSTOMER, jiaHeCount);
                calculateResultMap.replace(ProStaConstant.JIA_HE, 0L);
            }
        }
        for (Map.Entry<String, Long> countyEntry : countyEntryModify) {
            String countyKey = countyEntry.getKey();
            if (ProStaConstant.NAN_HU == countyKey || ProStaConstant.XIU_ZHOU == countyKey) {
                Long nanHuOrXuiZhouCount = countyEntry.getValue();
                nanHuAndXuiZhouOnlineSum += nanHuOrXuiZhouCount;
                calculateResultMap.replace(ProStaConstant.JIA_HE, nanHuAndXuiZhouOnlineSum);
            }
        }

        // 2.查询各个区县所有数量
        Map<String, Long> calculateAllResultMap = new HashMap<>();
        Map<String, Long> intranetIPAllCountMap = intranetIPService.queryAllCount();
        Map<String, Long> pubNetIPAllCountMap = pubNetIPService.queryAllCount();
        Map<String, Long> pubNetWebAllCountMap = pubNetWebService.queryAllCount();
        Map<String, Long> videoAllCountMap = videoService.queryAllCount();

        for (Map.Entry<String, Long> entry : intranetIPAllCountMap.entrySet()) {
            String intranetIPKey = entry.getKey();
            Long intranetIPValue = entry.getValue();
            Set<String> pubNetIPKeySets = pubNetIPAllCountMap.keySet();
            for (String pubNetIPKey : pubNetIPKeySets) {
                if (intranetIPKey == pubNetIPKey) {
                    Long pubNetIPVal = pubNetIPAllCountMap.get(pubNetIPKey);
                    Set<String> pubNetWebKeySets = pubNetWebAllCountMap.keySet();
                    for (String pubNetWebKey : pubNetWebKeySets) {
                        if (pubNetIPKey == pubNetWebKey) {
                            Long pubNetWebVal = pubNetWebAllCountMap.get(pubNetWebKey);
                            Set<String> videoKeySets = videoAllCountMap.keySet();
                            for (String videoKey : videoKeySets) {
                                if (pubNetWebKey == videoKey) {
                                    Long videoVal = videoAllCountMap.get(videoKey);
                                    Long countySum = videoVal + pubNetWebVal + pubNetIPVal + intranetIPValue;
                                    calculateAllResultMap.put(videoKey, countySum);
                                }
                            }
                        }
                    }
                }
            }
        }

        // 各区县所有数量统计转换；嘉禾-->要客  南湖、秀洲-->嘉禾
        Long nanHuAndXuiZhouSum = 0L;
        Set<Map.Entry<String, Long>> allCountyEntryModify = calculateAllResultMap.entrySet();
        for (Map.Entry<String, Long> countyEntry : allCountyEntryModify) {
            String countyKey = countyEntry.getKey();
            if (ProStaConstant.JIA_HE == countyKey) {
                Long jiaHeCount = countyEntry.getValue();
                calculateAllResultMap.replace(ProStaConstant.CUSTOMER, jiaHeCount);
                calculateAllResultMap.replace(ProStaConstant.JIA_HE, 0L);
            }
        }
        for (Map.Entry<String, Long> countyEntry : allCountyEntryModify) {
            String countyKey = countyEntry.getKey();
            if (ProStaConstant.NAN_HU == countyKey || ProStaConstant.XIU_ZHOU == countyKey) {
                Long nanHuOrXuiZhouCount = countyEntry.getValue();
                nanHuAndXuiZhouSum += nanHuOrXuiZhouCount;
                calculateAllResultMap.replace(ProStaConstant.JIA_HE, nanHuAndXuiZhouSum);
//                calculateAllResultMap.remove(ProStaConstant.NAN_HU);
//                calculateAllResultMap.remove(ProStaConstant.XIU_ZHOU);
            }
        }

        // 在线率计算，封装
        ArrayList<ProjectOnlineRateDTO> countList = new ArrayList<>();
        Set<Map.Entry<String, Long>> allEntrySetRate = calculateAllResultMap.entrySet();
        for (Map.Entry<String, Long> allEntry : allEntrySetRate) {
            ProjectOnlineRateDTO onlineRateDTO = new ProjectOnlineRateDTO();
            String allEntryKey = allEntry.getKey();
            for (Map.Entry<String, Long> onlineEntry : calculateResultMap.entrySet()) {
                String onlineEntryKey = onlineEntry.getKey();
                if (allEntryKey == onlineEntryKey) {
                    Long allEntryValue = allEntry.getValue();
                    Long onlineEntryValue = onlineEntry.getValue();
                    onlineRateDTO.setCounty(allEntryKey);
                    onlineRateDTO.setProjectCount(allEntryValue);
                    // 在线率计算
                    String rate = String.format("%.2f",((onlineEntryValue.doubleValue() / allEntryValue.doubleValue()) * 100)) + "%";
                    onlineRateDTO.setOnlineRate(rate);
                    countList.add(onlineRateDTO);
                }
            }
        }
        // 存储总量以及在线率计算结果
        Map<String, Object> map = new HashMap<>();
        map.put("区县数量", calculateAllResultMap);
        map.put("区县在线率", countList);
        return ResultUtils.success(map);
    }
}
