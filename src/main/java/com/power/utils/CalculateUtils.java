package com.power.utils;

import com.power.common.constant.ProStaConstant;
import com.power.entity.dto.ProjectOnlineRateDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 计算行业视频、内网IP、公网IP、公网web四张表区县在线率
 * @author cyk
 * @since 2023/11
 */
public class CalculateUtils {

    public static Map<String, Object> calculateCountyOnlineRate(Map<String, Long> intranetIPCountyCount,
                                                                Map<String, Long> pubNetIPCountyCount,
                                                                Map<String, Long> pubNetWebCountyCount,
                                                                Map<String, Long> industryVideoCountyCount,
                                                                Map<String, Long> intranetIPAllCountMap,
                                                                Map<String, Long> pubNetIPAllCountMap,
                                                                Map<String, Long> pubNetWebAllCountMap,
                                                                Map<String, Long> videoAllCountMap) {

        Map<String, Object> onlineRateMap = new HashMap<>();

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
                    String rate = "0.00%";
                    if (allEntryValue != 0L) {
                        rate = String.format("%.2f",((onlineEntryValue.doubleValue() / allEntryValue.doubleValue()) * 100)) + "%";
                    }
                    onlineRateDTO.setOnlineRate(rate);
                    countList.add(onlineRateDTO);
                }
            }
        }
        // 存储总量以及在线率计算结果
        onlineRateMap.put("区县在线数量", calculateResultMap);
        onlineRateMap.put("区县在线率", countList);

        return onlineRateMap;
    }


    /**
     * 各区县所有数量统计转换；嘉禾-->要客  南湖、秀洲-->嘉禾
     * @param convertMap
     * @return
     */
    public static Map<String, String> convertCounty(Map<String, String> convertMap) {

        String nanHuAndXuiZhouSum = "0";
        Set<Map.Entry<String, String>> allCountyEntryModify = convertMap.entrySet();
        for (Map.Entry<String, String> countyEntry : allCountyEntryModify) {
            String countyKey = countyEntry.getKey();
            if (ProStaConstant.JIA_HE == countyKey) {
                String jiaHeCount = countyEntry.getValue();
                convertMap.replace(ProStaConstant.CUSTOMER, jiaHeCount);
                convertMap.replace(ProStaConstant.JIA_HE, "0");
            }
        }
        for (Map.Entry<String, String> countyEntry : allCountyEntryModify) {
            String countyKey = countyEntry.getKey();
            if (ProStaConstant.NAN_HU == countyKey || ProStaConstant.XIU_ZHOU == countyKey) {
                String nanHuOrXuiZhouCount = countyEntry.getValue();
                nanHuAndXuiZhouSum += nanHuOrXuiZhouCount;
                convertMap.replace(ProStaConstant.JIA_HE, nanHuAndXuiZhouSum);
            }
        }
        return convertMap;
    }
}
