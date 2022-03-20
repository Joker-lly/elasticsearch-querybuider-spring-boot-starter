package com.lly.elasticsearch.anno;

import com.lly.elasticsearch.EsQueryType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EsQueryAnnotation {
    /**
     * 默认为全匹配查询
     */
    String type() default EsQueryType.TERM;

    /**
     * 字段名称
     * 当 fieldName 不为 “” 时 type 必须为  EsQueryUtil.FROM 或 EsQueryUtil.TO
     *
     * @return
     */
    String fieldName() default "";
}
