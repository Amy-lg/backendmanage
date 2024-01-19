package com.power.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注实体类属性的中文注释 注解
 * 作用：为解决导入的Excel中列存在新增问题
 * @author cyk
 * @since 2024/1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Component
public @interface FieldAnnotation {
    String value() default "";
}
