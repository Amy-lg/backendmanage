package com.power.controller.equipmentcontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.equipment.IndustryVideoEntity;
import com.power.service.equipmentservice.IndustryVideoService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            String str = videoService.importIndustryVideoExcel(file);
            return ResultUtils.success(str);
        }
        return ResultUtils.success();
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
