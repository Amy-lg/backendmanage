package com.power.controller.SLAcontroller;

import com.power.common.Result;
import com.power.service.basicservice.ProjectBasicInfoService;
import com.power.service.equipmentservice.IndustryVideoService;
import com.power.service.equipmentservice.IntranetIPService;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.service.equipmentservice.PubNetWebService;
import com.power.service.fileservice.BusinessOrderFileService;
import com.power.service.fileservice.TOrderFileService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/slaCalc")
public class SlaController {

    @Autowired
    private ProjectBasicInfoService basicInfoService;

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
    @Autowired
    private TOrderFileService tOrderService;

    // 纳管率计算
    @GetMapping("/calSlaOnlineRate")
    public Result isAcceptRate() {

        ArrayList<Object> salList = new ArrayList<>();
        // 存储到数组中
        String[] denominatorStr = new String[5];
        String[] numeratorStr = new String[5];
        // 1.纳管的分母分子数据信息
        List<Long> isAcceptRateList = basicInfoService.calculateAcceptRate();

        // 2.设备在线率的分母分子数据信息
        // 行业视频
        List<Long> videoRateList = videoService.calculateVideoRate();
        // 内网IP
        List<Long> intranetIpRateList = intranetIPService.calculateIntranetRate();
        // 公网IP
        List<Long> netIpRateList = pubNetIPService.calculateNetIpRate();
        // 公网web
        List<Long> netWebRateList = pubNetWebService.calculateNetWebRate();

        // 统计四张表分子分母数量信息
        Long equipmentDenominator = 0L;
        Long equipmentNumerator = 0L;
        for (int i = 0; i < videoRateList.size(); i++) {
            // 分母总数
            if (i == 0) {
                equipmentDenominator = videoRateList.get(i);
                equipmentDenominator += intranetIpRateList.get(i);
                equipmentDenominator += netIpRateList.get(i);
                equipmentDenominator += netWebRateList.get(i);
            }
            if (i == 1) {
                equipmentNumerator = videoRateList.get(i);
                equipmentNumerator += intranetIpRateList.get(i);
                equipmentNumerator += netIpRateList.get(i);
                equipmentNumerator += netWebRateList.get(i);
            }
        }

        // 3.工单处理时长（显示当前时间月份的平均时长；根据故障发生时间判断月份信息）
        // 业务工单
        List<String> businessAveDurationList = businessOrderService.calculateAveDuration();
        if ("0".equals(businessAveDurationList.get(0))) {
            denominatorStr[2] = "0";
            numeratorStr[2] = "0";
        }else {
            denominatorStr[2] = businessAveDurationList.get(0);
            numeratorStr[2] = businessAveDurationList.get(1);
        }

        // 小T工单
        List<String> tOrderAveDurationList = tOrderService.calculateAveDuration();
        if ("0".equals(tOrderAveDurationList.get(0))) {
            denominatorStr[3] = "0";
            numeratorStr[3] = "0";
        }else {
            denominatorStr[3] = tOrderAveDurationList.get(0);
            numeratorStr[3] = tOrderAveDurationList.get(1);
        }

        // 分母数组
        denominatorStr[0] = String.valueOf(isAcceptRateList.get(0));
        denominatorStr[1] = String.valueOf(equipmentDenominator);

        denominatorStr[4] = "0";

        // 分子数组
        numeratorStr[0] = String.valueOf(isAcceptRateList.get(1));
        numeratorStr[1] = String.valueOf(equipmentNumerator);

        numeratorStr[4] = "0";

        salList.add(denominatorStr);
        salList.add(numeratorStr);
        if (!salList.isEmpty()) {
            return ResultUtils.success(salList);
        }
        return ResultUtils.success();
    }
}
