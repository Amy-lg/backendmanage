package com.power.service.basicservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.common.util.CommonUtil;
import com.power.entity.User;
import com.power.entity.basic.ProjectDataInfoEntity;
import com.power.entity.basic.filtersearch.DataInfoFilter;
import com.power.mapper.basicmapper.ProjectDataInfoMapper;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.CalculateUtils;
import com.power.utils.TokenUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 项目数据信息业务层
 * @author cyk
 * @since 2024/5
 */
@Service
public class ProjectDataInfoService extends ServiceImpl<ProjectDataInfoMapper, ProjectDataInfoEntity> {


    /**
     * 数据信息导入
     * @param file
     * @return
     */
    public String importProjectDataExcel(MultipartFile file) {

        List<ProjectDataInfoEntity> dataInfoList = importBasicProjectData(file);
        if (dataInfoList != null) {
            boolean saveBatch = this.saveBatch(dataInfoList, 200);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
    }


    /**
     * Excel解析
     * @param file
     * @return
     */
    private List<ProjectDataInfoEntity> importBasicProjectData(MultipartFile file) {

        Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
        List<ProjectDataInfoEntity> saveDataList = new ArrayList<>();
        ProjectDataInfoEntity projectDataInfo = null;
        if (workbook != null) {
            int sheets = workbook.getNumberOfSheets();
            try {
                Class<?> clazz = Class.forName("com.power.entity.basic.ProjectDataInfoEntity");
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        // 数据标题
                        List<String> excelTitle = AnalysisExcelUtils.getExcelTitle(sheet);
                        int lastRowNum = sheet.getLastRowNum();
                        for (int j = 1; j <= lastRowNum; j++) {
                            projectDataInfo = (ProjectDataInfoEntity) clazz.getDeclaredConstructor().newInstance();
                            Field[] projectDataInfoFields = clazz.getDeclaredFields();
                            // 获取属性上的注释信息
                            List<String> fieldAnnotationList = CommonUtil.getFieldAnnotation(projectDataInfoFields);
                            Row contentRow = sheet.getRow(j);
                            String cellValue = null;
                            Iterator<Cell> cellIterator = contentRow.cellIterator();
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                CellType cellType = cell.getCellType();
                                // 利用列索引循环属性赋值（从第3个属性开始）
                                int columnIndex = cell.getColumnIndex() + 2;
                                switch (cellType) {
                                    case STRING:
                                        String title = excelTitle.get(cell.getColumnIndex());
                                        for (int k = 0; k < fieldAnnotationList.size(); k++) {
                                            String fieldAnnotation = fieldAnnotationList.get(k);
                                            if (!"".equals(fieldAnnotation) && fieldAnnotation != null
                                                    && title != null && title.equals(fieldAnnotation)) {
                                                cellValue = cell.getStringCellValue();
                                                projectDataInfoFields[k + 2].setAccessible(true);
                                                projectDataInfoFields[k + 2].set(projectDataInfo, cellValue);
                                            }
                                        }
                                        break;
                                    case NUMERIC:
                                        double date = cell.getNumericCellValue();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        // Java在读取Excel单元格中日期格式的内容时，会自动将日期格式转换为数字格式；
                                        // 这里需要将读取到的Excel单元格中的日期格式的数字，转换成日期格式
                                        Date convertDate = DateUtil.getJavaDate(date);
                                        String formatDate = sdf.format(convertDate);
                                        projectDataInfoFields[columnIndex].setAccessible(true);
                                        projectDataInfoFields[columnIndex].set(projectDataInfo, formatDate);
                                        break;
                                    case BOOLEAN:
                                        projectDataInfoFields[columnIndex].setAccessible(true);
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        projectDataInfoFields[columnIndex].set(projectDataInfo, cellValue);
                                        break;
                                    case FORMULA:
                                        // 创建公式解析器
                                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        // 解析公式
                                        CellValue evaluate = formulaEvaluator.evaluate(cell);
                                        double value = evaluate.getNumberValue();
                                        projectDataInfoFields[columnIndex].setAccessible(true);
                                        projectDataInfoFields[columnIndex].set(projectDataInfo, value);
                                        break;
                                    case BLANK:
                                        projectDataInfoFields[columnIndex].setAccessible(true);
                                        projectDataInfoFields[columnIndex].set(projectDataInfo, cellValue);
                                        break;
                                    case ERROR:
                                        byte errorValue = cell.getErrorCellValue();
                                        projectDataInfoFields[columnIndex].setAccessible(true);
                                        projectDataInfoFields[columnIndex].set(projectDataInfo, errorValue);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                            }
                            saveDataList.add(projectDataInfo);
                        }
                    }
                    continue;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return saveDataList;
        }
        return null;
    }


    /**
     * 查询、筛选功能
     * @param dataInfoFilter
     * @return
     */
    public IPage<ProjectDataInfoEntity> filOrSeaByCondition(DataInfoFilter dataInfoFilter) {

        Integer pageNum = dataInfoFilter.getPageNum();
        Integer pageSize = dataInfoFilter.getPageSize();

        IPage<ProjectDataInfoEntity> dataInfoPage = new Page<>(pageNum, pageSize);
        QueryWrapper<ProjectDataInfoEntity> queryWrapper = new QueryWrapper<>();

        // 模糊搜索条件
        String ictNum = dataInfoFilter.getIctNum();
        // 筛选条件
        String projectName = dataInfoFilter.getProjectName();
        String county = dataInfoFilter.getCounty();
        String constructionMethod = dataInfoFilter.getConstructionMethod();
        String integratedTietong = dataInfoFilter.getIntegratedTietong();
        // 管理员权限
        User currentUser = TokenUtils.getCurrentUser();
        String userRole = currentUser.getRole();
        if (!StrUtil.isBlank(userRole) && ProStaConstant.MANAGER.equals(userRole)) {
            String projectCounty = currentUser.getProjectCounty();
            if (!StrUtil.isEmpty(projectCounty)) {
                String currentUserCounty = projectCounty.substring(0, 2);
                queryWrapper.eq("county", currentUserCounty);
                if (ictNum != null) {
                    queryWrapper.like("ict_num", ictNum);
                    IPage<ProjectDataInfoEntity> authoritySearchPage = page(dataInfoPage, queryWrapper);
                    return authoritySearchPage;
                }
                if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(constructionMethod) ||
                        !StrUtil.isEmpty(integratedTietong)) {
                    if (!StrUtil.isBlank(projectName)) {
                        queryWrapper.eq("project_name", projectName);
                    }
                    if (!StrUtil.isBlank(constructionMethod)) {
                        queryWrapper.eq("construction_method", constructionMethod);
                    }
                    if (!StrUtil.isBlank(integratedTietong)) {
                        queryWrapper.eq("integrated_tietong", integratedTietong);
                    }
                    IPage<ProjectDataInfoEntity> authorityFilterPage = page(dataInfoPage, queryWrapper);
                    return authorityFilterPage;
                }
                IPage<ProjectDataInfoEntity> authorityPage = page(dataInfoPage, queryWrapper);
                return authorityPage;
            }
        }

        // 模糊检索
        if (ictNum != null) {
            queryWrapper.like("ict_num", ictNum);
            IPage<ProjectDataInfoEntity> searchPage = page(dataInfoPage, queryWrapper);
            return searchPage;
        }
        // 筛选
        if (!StrUtil.isEmpty(projectName) || !StrUtil.isEmpty(county) ||
                !StrUtil.isEmpty(constructionMethod) || !StrUtil.isEmpty(integratedTietong)) {
            if (!StrUtil.isBlank(projectName)) {
                queryWrapper.eq("project_name", projectName);
            }
            if (!StrUtil.isBlank(county)) {
                queryWrapper.eq("county", county);
            }
            if (!StrUtil.isBlank(constructionMethod)) {
                queryWrapper.eq("construction_method", constructionMethod);
            }
            if (!StrUtil.isBlank(integratedTietong)) {
                queryWrapper.eq("integrated_tietong", integratedTietong);
            }
            IPage<ProjectDataInfoEntity> filterPage = page(dataInfoPage, queryWrapper);
            return filterPage;
        }
        IPage<ProjectDataInfoEntity> allPage = page(dataInfoPage);
        return allPage;
    }


    /**
     * 发送邮件支付提醒
     * @param javaMailSender
     * @param from 发送者
     */
    public void sendPayReminderEmail(JavaMailSenderImpl javaMailSender, String from, List<User> userList) {

        try {
            // 用于比较当前时间和对下付款时间节点两字符串大小
            int compare = -1;
            // 当前时间字符串
            // 当前年份月份的前一个月的时间（例：当前月份为2024-05 ，获取到的为 2024-04）
            String beforeMonth = CalculateUtils.calcBeforeMonth(-1);
            // 年份截取
            String systemCurrentYear = beforeMonth.substring(0,5);
            // 获取数据库数据信息
            List<ProjectDataInfoEntity> allDataInfoList = list();
            if (allDataInfoList != null && allDataInfoList.size() >= 1) {
                // 创建简单邮件实例对象
                SimpleMailMessage message = new SimpleMailMessage();
                // 设置邮件主题
                message.setSubject(ProStaConstant.PAYMENT_REMINDER);
                // 设置发送人
                message.setFrom(from);
                // 设置邮件接收人
//                message.setTo(ProStaConstant.TO_EMAIL);
                // 设置邮件发送日期
                message.setSentDate(new Date());
                for (ProjectDataInfoEntity projectDataInfo : allDataInfoList) {
                    // 获取区县
                    String dataInfoCounty = projectDataInfo.getCounty();
                    if (!StrUtil.isEmpty(dataInfoCounty)) {
                        for (User user : userList) {
                            String userProjectCounty = user.getProjectCounty();
                            if (null != userProjectCounty && !"".equals(userProjectCounty)
                                    && dataInfoCounty.equals(userProjectCounty)) {
                                String userEmail = user.getEmail();
                                message.setTo(userEmail);
                            }
                        }
                    }
                    // 获取对下付款时间节点字段值(此值需分割转换成一个数组)
                    String paymentTimeline = projectDataInfo.getPaymentTimeline();
                    if (!StrUtil.isBlank(paymentTimeline)) {
                        String[] dateArr = paymentTimeline.split("、");
                        // 遍历时间字符串
                        for (String dateStr : dateArr) {
                            String dateYear = dateStr.substring(0, 5);
                            if (dateYear.equals(systemCurrentYear)) {
                                String dateInfoMonth = dateStr.substring(0, 7);
                                // 对下付款时间节点月份 vs 前一月份
                                compare = dateInfoMonth.compareToIgnoreCase(beforeMonth);
                                // 对下付款时间节点 == 前一月份时间
                                if (compare == 0) {
                                    // 设置邮件内容
                                    message.setText("【DICT售后服务智能中枢-支付提醒】\n" +
                                            "项目名称:" + projectDataInfo.getProjectName() + "\n" +
                                            "当前对下付款时间节点:" + dateStr + "\n" +
                                            "目前即将到期，请及时支付续费，以便于正常使用。");
                                    javaMailSender.send(message);
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



}
