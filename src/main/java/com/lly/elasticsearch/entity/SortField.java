package com.lly.elasticsearch.entity;

import lombok.Data;

/**
 * 描述：es 排序字段以及排序方式
 *
 * @author: liuliye
 * @since: 2022/2/14
 */
@Data
public class SortField {
    /**排序字段名称*/
    private String column;
    /** 排序方式 0 DESC， 1 ASC*/
    private String order;
}
