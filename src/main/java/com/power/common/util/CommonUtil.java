package com.power.common.util;

import com.power.annotation.FieldAnnotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装类方法中的公共方法
 * @author cyk
 * @since 2024/1
 */
public class CommonUtil {

    // 获取实体类属性上的注释信息
    public static List<String> getFieldAnnotation(Field[] fields) {
        List<String> fieldList = new ArrayList<>();
        for (Field field : fields) {
            FieldAnnotation fieldAnnotation = field.getAnnotation(FieldAnnotation.class);
            if (fieldAnnotation != null) {
                String annotationVal = fieldAnnotation.value();
                fieldList.add(annotationVal);
            }
        }
        return fieldList;
    }


}
