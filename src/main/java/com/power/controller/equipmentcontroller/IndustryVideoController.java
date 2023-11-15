package com.power.controller.equipmentcontroller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.IndustryVideoEntity;
import com.power.service.equipmentservice.IndustryVideoService;
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
 * 行业视频控制层（数据的导入，信息查询操作）
 * @since 2023/9
 * @author cyk
 */
@RestController
@RequestMapping("/api/video")
public class IndustryVideoController {

    @Autowired
    private IndustryVideoService videoService;

    /**
     * 导入行业视频接口
     * @param file excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importIndustryVideoFile(@RequestParam("file") MultipartFile file) {

        if (file != null) {
            String importResult = videoService.importIndustryVideoExcel(file);
            if (importResult != null && !importResult.equals(ResultStatusCode.ERROR_IMPORT.getMsg())) {
                return ResultUtils.success(importResult);
            }
            return ResultUtils.error(5003, ResultStatusCode.ERROR_IMPORT_001.getMsg());
        }
        return ResultUtils.success();
    }


    /**
     * 数据导出接口
     * @param response 响应体
     */
    @PostMapping("/export")
    public void exportDataFile(HttpServletResponse response) {

        // 查询到所有数据信息
        List<IndustryVideoEntity> industryVideoList = videoService.searchOrFilterByExport();
        try {
            // 通过工具类创建writer
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题别名
//        writer.addHeaderAlias("projectName", "拨测对象所属项目");
            writer.write(industryVideoList, true);
            String fileName = "行业视频";
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


    /**
     * 查询所有行业视频信息
     * @return
     */
    @GetMapping("/queryInfo")
    public Result queryIndustryVideoInfo(@RequestParam Integer pageNum,
                                         @RequestParam Integer pageSize) {
        IPage<IndustryVideoEntity> videoInfoPages = videoService.queryIndustryVideoInfo(pageNum, pageSize);
        if (videoInfoPages != null) {
            return ResultUtils.success(videoInfoPages);
        }
        return ResultUtils.success();
    }


}
