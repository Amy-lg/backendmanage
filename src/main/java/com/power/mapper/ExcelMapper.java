package com.power.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.power.entity.Exam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExcelMapper extends BaseMapper<Exam> {

    @Insert("insert into newtable(question,answer,question_type) values(#{question},#{answer},#{question_type})")
    int insertExam(Exam ex);

    @Select("select id,question,answer,question_type from newtable where question like concat('%', #{quesText}, '%') and " +
            "optionsA like concat('%', #{optionsA}, '%') and optionsB like concat('%', #{optionsB}, '%') and " +
            "optionsC like concat('%', #{optionsC}, '%')")
    List<Exam> selAnswerByLike(String quesText, String optionsA, String optionsB, String optionsC);
}
