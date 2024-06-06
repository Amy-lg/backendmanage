package com.power.controller.mapcontroller;

import com.power.common.Result;
import com.power.common.constant.ProStaConstant;
import com.power.service.basicservice.ProjectDataInfoService;
import com.power.service.equipmentservice.IndustryVideoService;
import com.power.service.equipmentservice.IntranetIPService;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.service.equipmentservice.PubNetWebService;
import com.power.service.evaluationservice.EvaluationService;
import com.power.service.fileservice.BusinessOrderFileService;
import com.power.utils.CalculateUtils;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中间地图模块数据信息计算
 * @author cyk
 * @since 2023/11
 */
@RestController
@RequestMapping("/api/map")
public class MapController {

    // 纳管率
    // @Autowired
    // private ProjectBasicInfoService basicInfoService;

    @Autowired
    private ProjectDataInfoService projectDataInfoService;

    // 行业视频
    @Autowired
    private IndustryVideoService videoService;
    // 内网IP
    @Autowired
    private IntranetIPService intranetIPService;
    // 公网IP
    @Autowired
    private PubNetIPService pubNetIPService;
    // 公网web
    @Autowired
    private PubNetWebService pubNetWebService;

    // 业务工单
    @Autowired
    private BusinessOrderFileService businessOrderService;
    // 小T工单
    /*@Autowired
    private TOrderFileService tOrderService;*/

    // 客户满意度
    @Autowired
    private EvaluationService evaluationService;

    // 中间地图模块数据信息计算
    @GetMapping("/calcData")
    public Result calculateMap() {

        List<Object> mapDataList = new ArrayList<>();
        // 内网IP拨测表 区县在线总数量
        Map<String, Long> intranetIPCountyCount = intranetIPService.queryAllOnlineCount();
        // 公网IP拨测表 区县在线总数量
        Map<String, Long> pubNetIPCountyCount = pubNetIPService.queryAllOnlineCount();
        // 公网web拨测表 区县在线总数量
        Map<String, Long> pubNetWebCountyCount = pubNetWebService.queryAllOnlineCount();
        // 行业视频表 区县在线总数量
        Map<String, Long> industryVideoCountyCount = videoService.queryAllOnlineCount();

        // 查询各个区县所有数量
        Map<String, Long> intranetIPAllCountMap = intranetIPService.queryAllCount();
        Map<String, Long> pubNetIPAllCountMap = pubNetIPService.queryAllCount();
        Map<String, Long> pubNetWebAllCountMap = pubNetWebService.queryAllCount();
        Map<String, Long> videoAllCountMap = videoService.queryAllCount();

        // 区县设备在线率
        Map<String, Object> countyOnlineRateResultMap = CalculateUtils.calculateCountyOnlineRate(intranetIPCountyCount, pubNetIPCountyCount,
                pubNetWebCountyCount, industryVideoCountyCount,
                intranetIPAllCountMap, pubNetIPAllCountMap,
                pubNetWebAllCountMap, videoAllCountMap);
        mapDataList.add(countyOnlineRateResultMap.get("区县在线率"));
        // 满意度平均分
        Double rateOfNanHuOrXuiZhou = 0d;
        Map<String, String> satisfiedScoreMap = new HashMap<>();
        List<Map<String, String>> averageScoreOfCounty = evaluationService.calcAverScoreAndCount();
        if (averageScoreOfCounty != null && averageScoreOfCounty.size() != 0) {
            for (Map<String, String> customerComment : averageScoreOfCounty) {
                // 获取区县、平均分
                String county = customerComment.get("区县");
                if (ProStaConstant.JIA_HE == county) {
                    String rateOfJiaHe = customerComment.get("平均分");
                    satisfiedScoreMap.put(ProStaConstant.CUSTOMER, rateOfJiaHe);
                } else if (ProStaConstant.NAN_HU == county || ProStaConstant.XIU_ZHOU == county) {
                    rateOfNanHuOrXuiZhou = Double.valueOf(customerComment.get("平均分"));
                    rateOfNanHuOrXuiZhou += rateOfNanHuOrXuiZhou;
                    satisfiedScoreMap.put(ProStaConstant.JIA_HE, String.valueOf(rateOfNanHuOrXuiZhou / 2));
                }else {
                    String rateOfCounty = customerComment.get("平均分");
                    satisfiedScoreMap.put(county, rateOfCounty);
                }
            }
        }
//        Map<String, String> convertCountyMap = CalculateUtils.convertCounty(satisfiedScoreMap);
        mapDataList.add(satisfiedScoreMap);

        // 根据区县区分
        Map<String, String> isAcceptRateMap = new HashMap<>();
        Map<String, String> averageDurationOfCountyMap = new HashMap<>();
        // 南湖秀洲纳管率分子、分母统计
        Long nanHuXuiZhouDenominator = 0L; // 分母
        Long nanHuXuiZhouNumerator = 0L; // 分子
        for (String county : ProStaConstant.counties_rate) {
            // 纳管率
            //List<Long> isAcceptRateOfCounty = basicInfoService.calculateAcceptRate(county);
            List<Long> isAcceptRateOfCounty = projectDataInfoService.calculateAcceptRate(county);
            String rate = "0.00";
            if (isAcceptRateOfCounty != null && isAcceptRateOfCounty.size() == 2) {
                // 南湖、秀洲算一起 --> 计算结果给到嘉禾
                if (ProStaConstant.NAN_HU == county || ProStaConstant.XIU_ZHOU == county) {
                    nanHuXuiZhouDenominator += isAcceptRateOfCounty.get(0);
                    nanHuXuiZhouNumerator += isAcceptRateOfCounty.get(1);
                }
                if (ProStaConstant.JIA_HE == county && nanHuXuiZhouDenominator != 0) {
                    rate = String.format("%.2f",(nanHuXuiZhouNumerator.doubleValue() / nanHuXuiZhouDenominator.doubleValue()) * 100)  + "%";
                } else {
                    Long denominator = isAcceptRateOfCounty.get(0); // 分母
                    Long numerator = isAcceptRateOfCounty.get(1); // 分子
                    if (denominator != 0) {
                        rate = String.format("%.2f",(numerator.doubleValue() / denominator.doubleValue()) * 100)  + "%";
                    }
                }
                isAcceptRateMap.put(county, rate);
            }else {
                isAcceptRateMap.put(county, rate);
            }
            // 工单处理平均时长
            // 业务工单
            List<String> busAveDuration = businessOrderService.calculateAveDuration(county);
            String averageDuration = "0";
            if (busAveDuration != null && busAveDuration.size() == 2) {
                Double sumOfCounty = Double.valueOf(busAveDuration.get(0));
                Double sumDuration = Double.valueOf(busAveDuration.get(1));
                if (sumOfCounty != 0) {
                    averageDuration = String.format("%.2f", sumDuration / sumOfCounty);
                }
                averageDurationOfCountyMap.put(county, averageDuration);
            } else {
                averageDurationOfCountyMap.put(county, averageDuration);
            }
            // 小T工单
//            tOrderService.tAverDurationOfCounty(county);
        }
//        Map<String, String> convertCountyIsAcceptMap = CalculateUtils.convertCounty(isAcceptRateMap);
        mapDataList.add(isAcceptRateMap);
//        Map<String, String> convertCountyAveDurMap = CalculateUtils.convertCounty(averageDurationOfCountyMap);
        mapDataList.add(averageDurationOfCountyMap);
        return ResultUtils.success(mapDataList);
    }
}
