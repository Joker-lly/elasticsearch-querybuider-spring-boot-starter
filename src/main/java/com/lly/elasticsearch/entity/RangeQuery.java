package com.lly.elasticsearch.entity;

import lombok.Data;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.util.Date;

/**
 * 描述：范围查询信息载体
 *
 * @author: liuliye
 * @since: 2022/2/14
 */
@Data
public class RangeQuery {

    private Object fromValue;

    private Object toValue;

    private RangeQueryBuilder rangeQuery;

    public void setFromValue(Object fromValue) {
        if (fromValue instanceof Date) {
            this.fromValue = ((Date) fromValue).getTime();
        } else {
            this.fromValue = fromValue;
        }

    }

    public void setToValue(Object toValue) {
        if (toValue instanceof Date) {
            this.toValue = ((Date) toValue).getTime();
        } else {
            this.toValue = toValue;
        }
    }
}
