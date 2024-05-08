package com.power.controller.basiccontroller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.entity.User;
import com.power.entity.basic.ProjectDataInfoEntity;
import com.power.entity.basic.filtersearch.DataInfoFilter;
import com.power.service.UserService;
import com.power.service.basicservice.ProjectDataInfoService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 项目数据信息控制层
 * @author cyk
 * @since 2024/5
 */
@RestController
@RequestMapping("/api/dataInfo")
public class ProjectDataInfoCtl {

    @Autowired
    private ProjectDataInfoService projectDataInfoService;

    // Spring框架提供的服务实现类来实现邮件推送
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    // 获取区县售后服务人员邮箱
    @Autowired
    private UserService userService;


    /**
     * 数据信息导入
     * @param file
     * @return
     */
    @PostMapping("/import")
    public Result importProjectDataFile(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            String importResult = projectDataInfoService.importProjectDataExcel(file);
            return ResultUtils.success(importResult);
        }
        return ResultUtils.success();
    }


    /**
     * 查询、筛选功能
     * @param dataInfoFilter 筛选model
     * @return
     */
    @PostMapping("/dataInfoSel")
    public Result filterByCondition(@RequestBody DataInfoFilter dataInfoFilter) {

        // 判断请求体是否为空
        if (dataInfoFilter != null) {
            IPage<ProjectDataInfoEntity> pages = projectDataInfoService.filOrSeaByCondition(dataInfoFilter);
            return ResultUtils.success(pages);
        } else {
            // 返回空数据
            return ResultUtils.success();
        }
    }


    // 导出



    /**
     * 工单的支付提醒
     * 根据“对下付款时间节点”字段值中的时间节点发邮件提醒
     */
    @Scheduled(cron = "0 0 8 * * ? ")
    public void paymentReminderByEmail() {

        List<User> userList = userService.list();
        // 支付邮件设置
        projectDataInfoService.sendPayReminderEmail(javaMailSender, from, userList);
    }


}
