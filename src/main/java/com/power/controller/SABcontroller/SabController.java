package com.power.controller.SABcontroller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.sabentity.SABAdministrationEntity;
import com.power.entity.sabentity.filterseacher.SabParamEntity;
import com.power.service.SABservice.SabAdministrationService;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.ResultUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * SAB项目控制层
 * @author cyk
 * @since 2024/4
 */
@RestController
@RequestMapping("/api/sabAdministration")
public class SabController {

    @Autowired
    private SabAdministrationService sabAdministrationService;


    /**
     * 数据导入
     * @param file 导入数据Excel文件
     * @return 返回导入结果信息
     */
    @PostMapping("/import")
    public Result importSabFile(@RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            String importResult = sabAdministrationService.importSabAdministrationFile(file);
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }


    /**
     * 数据展示
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/getSabData")
    public Result getAllSabData(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {

        IPage<SABAdministrationEntity> sabDataPage = sabAdministrationService.getSabData(pageNum, pageSize);
        if (sabDataPage != null) {
            return ResultUtils.success(sabDataPage);
        }
        return ResultUtils.success();
    }


    /**
     * 检索、筛选(根据ict项目编号/名称)
     * @param equipmentParam
     * @return
     */
    @PostMapping("/filOrSeaByCond")
    public Result searchOrFilterByNumName(@RequestBody SabParamEntity equipmentParam) {

        if (equipmentParam != null) {
            IPage<SABAdministrationEntity> pages = sabAdministrationService.getSabDataInfoByCondition(equipmentParam);
            return ResultUtils.success(pages);
        }else {
            return ResultUtils.success();
        }
    }


    /**
     * 新增、修改接口
     * @param sabAdministration
     * @return
     */
    @PostMapping("/updateSabData")
    public Result updateSabData(@RequestBody SABAdministrationEntity sabAdministration) {

        if (sabAdministration != null) {
            String updateStr = sabAdministrationService.updateSabDataByIctNum(sabAdministration);
            return ResultUtils.success(updateStr);
        }
        return ResultUtils.success();
    }


    /**
     * 删除接口
     * @param ictProjectNum
     * @return
     */
    @PostMapping("/removeSabData")
    public Result delSabData(@RequestParam String ictProjectNum) {

        if (!StrUtil.isBlank(ictProjectNum)) {
            String delResult = sabAdministrationService.delSabDataByIctNum(ictProjectNum);
            return ResultUtils.success(delResult);
        }
        return ResultUtils.success();
    }


    /**
     * 导出功能
     * @param response
     * @param equipmentParam
     */
    @PostMapping("/exportSabData")
    public void exportData(HttpServletResponse response, @RequestBody SabParamEntity equipmentParam) {

        List<SABAdministrationEntity> exportDataList = sabAdministrationService.exportFilterResult(equipmentParam);
        try {
            // 通过工具类创建writer
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题别名
            writer.addHeaderAlias("ictProjectNum", "ICT项目编号");
            writer.addHeaderAlias("ictProjectName", "ICT项目名称");
            writer.addHeaderAlias("city", "归属地市");
            writer.addHeaderAlias("county", "归属区县");
            writer.addHeaderAlias("projectType", "项目大类");
            writer.addHeaderAlias("projectStatus", "项目状态");
            writer.addHeaderAlias("projectStartTime", "项目开始时间");
            writer.addHeaderAlias("projectEndTime", "项目结束时间");
            writer.addHeaderAlias("maintenanceType", "维护类型（售后）");
            writer.addHeaderAlias("customerNum", "客户编号");
            writer.addHeaderAlias("customerName", "客户名称");
            writer.addHeaderAlias("isKeyProject", "是否重点保障项目");
            writer.addHeaderAlias("projectLevel", "售后阶段项目等级");

            writer.write(exportDataList, true);
            String fileName = "SAB项目表";
            // 设置导出Excel的文件格式信息
            AnalysisExcelUtils.settingExcelFileFormat(response, fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            writer.flush(outputStream, true);
            // 关闭writer，释放内存
            writer.close();
            IoUtil.close(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
