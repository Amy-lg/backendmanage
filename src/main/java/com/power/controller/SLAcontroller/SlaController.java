package com.power.controller.SLAcontroller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.slaentity.SLAFaultyEntity;
import com.power.service.SLAservice.SLAFaultyService;
import com.power.service.basicservice.ProjectBasicInfoService;
import com.power.service.equipmentservice.IndustryVideoService;
import com.power.service.equipmentservice.IntranetIPService;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.service.equipmentservice.PubNetWebService;
import com.power.service.fileservice.BusinessOrderFileService;
import com.power.service.fileservice.TOrderFileService;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.ResultUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/slaCalc")
public class SlaController {

    @Autowired
    private SLAFaultyService slaFaultyService;

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


    /**
     * 数据信息导入
     * @param file
     * @return
     */
    @PostMapping("/import")
    public Result importSLAFaulty(@RequestParam MultipartFile file) {

        if (!file.isEmpty()) {
            String importResult = slaFaultyService.importSLAFaultyExcel(file);
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、页面显示查询
     * @param pageNum
     * @param pageSize
     * @param ictNum 搜索功能参数，ict编号
     * @return
     */
    @GetMapping("/querySLAInfo")
    public Result queryAll(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                           @RequestParam(required = false) String ictNum) {

        IPage<SLAFaultyEntity> slaFaultyPages = slaFaultyService.querySLAFaulty(pageNum, pageSize, ictNum);
        if (slaFaultyPages != null) {
            return ResultUtils.success(slaFaultyPages);
        }
        return ResultUtils.success();
    }


    /**
     * 更新数据
     * @param slaFaulty
     * @return
     */
    @PostMapping("/editSLA")
    public Result updateSLAFaulty(@RequestBody SLAFaultyEntity slaFaulty) {

        List<Object> updStaList = slaFaultyService.updSLAInfo(slaFaulty);
        if (updStaList != null && updStaList.size() != 0) {
            return ResultUtils.success(updStaList);
        }
        return ResultUtils.success();
    }


    /**
     * 新增数据
     * @param slaFaulty
     * @return
     */
    @PostMapping("/addSLA")
    public Result addSLAFaulty(@RequestBody SLAFaultyEntity slaFaulty) {

        List<Object> addStaList = slaFaultyService.insertSLAInfo(slaFaulty);
        if (addStaList != null && addStaList.size() != 0) {
            return ResultUtils.success(addStaList);
        }
        return ResultUtils.success();
    }


    /**
     * 删除数据
     * @param ids 删除编号集合
     * @return
     */
    @PostMapping("/delBatchSLA")
    public Result deleteSLAFaultyById(@RequestBody List<Integer> ids) {

        List<Object> delStaList = slaFaultyService.delBatchSLAById(ids);
        if (delStaList != null && delStaList.size() != 0) {
            return ResultUtils.success(delStaList);
        }
        return ResultUtils.success();
    }


    //医院的墙壁比教堂听过更多虔诚的祈祷！
    /**
     * 导出
     * @param request
     * @param response
     */
    @PostMapping("/exportXlsx")
    public void exportSlaAsXlsx(HttpServletRequest request, HttpServletResponse response) {

        // 查询到所有信息
        List<SLAFaultyEntity> slaFaultyList = slaFaultyService.list();
        try {
            // 通过工具类创建writer；true,表示导出excel文件类型为 .xlsx
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题名
            writer.addHeaderAlias("county", "区县");
            writer.addHeaderAlias("ictNum", "ICT编号");
            writer.addHeaderAlias("projectName", "项目名称");
            writer.addHeaderAlias("customerName", "客户名称");
            writer.addHeaderAlias("maintenanceEndDate", "维护结束时间");
            writer.addHeaderAlias("visitFrequency", "走访频率");
            writer.addHeaderAlias("inspectionFrequency", "巡检频率");
            writer.addHeaderAlias("complaintSla", "投诉SLA");
            writer.addHeaderAlias("faultySla", "故障SLA");

            writer.write(slaFaultyList,true);
            // 导出文件名设置
            String fileName = "在维项目走访巡检投诉故障SLA梳理";
            // 设置导出Excel的文件格式信息
            AnalysisExcelUtils.settingExcelFileFormat(response, fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            writer.flush(outputStream, true);
            // 关闭writer，释放内存
            writer.close();
            IoUtil.close(outputStream);
        } catch (Exception e){
            new RuntimeException();
        }
    }

}
