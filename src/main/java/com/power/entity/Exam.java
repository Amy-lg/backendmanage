package com.power.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("newtable")
public class Exam {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String question;
    @TableField("optionsA")
    private String optionsA;
    @TableField("optionsB")
    private String optionsB;
    @TableField("optionsC")
    private String optionsC;
    @TableField("optionsD")
    private String optionsD;
    @TableField("question_type")
    private Integer questionType;
    private String answer;
}
