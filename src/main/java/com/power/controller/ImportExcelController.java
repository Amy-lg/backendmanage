package com.power.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.power.entity.Exam;
import com.power.mapper.ExcelMapper;
import com.power.service.ExamService;
import com.power.utils.AnalysisExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ImportExcelController {

    // 默认通过类型注入，配合注解@Qualifier实现名称注入
    @Autowired
    private ExcelMapper excelMapper;

    @Autowired
    private ExamService examService;

    // 解析Excel文件
    @PostMapping("/importExcel")
    public Map importExcel(@RequestParam("file") MultipartFile file) {

        HashMap<Object, Object> excelContentMap = new HashMap<>();
        int insertStatus = 0;
        try {
            if (!file.isEmpty()) {
                List<Exam> examList = AnalysisExcelUtils.analysisExcel(file);
                if (examList.size() != 0) {
                    for (Exam ex : examList) {
                        // 调用方法存储至数据库
//                        insertStatus = excelMapper.insertExam(ex);
                        examService.saveOrUpdate(ex);
                    }
                    excelContentMap.put("insertStatus", insertStatus);
                    excelContentMap.put("msg", "数据导入成功！");
                    return excelContentMap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        excelContentMap.put("insertStatus", insertStatus);
        excelContentMap.put("msg", "数据导入失败！");
        return excelContentMap;
    }

    // 多条件模糊查询
    @GetMapping("/selAnswer")
    public IPage<Exam> selAnswerByLike(@RequestParam Integer pageNum,
                                       @RequestParam Integer pageSize,
                                       @RequestParam(defaultValue = "") String question,
                                       @RequestParam(defaultValue = "") String optionsA) {

        IPage<Exam> examPage = new Page<>(pageNum, pageSize);
        QueryWrapper<Exam> queryWrapper = new QueryWrapper<>();
        if (!"".equals(question)) {
            queryWrapper.like("question", question);
        }
        if (!"".equals(optionsA)) {
            queryWrapper.and(w -> w.like("optionsA", optionsA));
        }
        /*if (!"".equals(optionsB)) {
            queryWrapper.like("optionsB", optionsB);
        }
        if (!"".equals(optionsC)) {
            queryWrapper.like("optionsC", optionsC);
        }*/
        IPage<Exam> examIPage = examService.page(examPage, queryWrapper);
        return examIPage;
    }
}
