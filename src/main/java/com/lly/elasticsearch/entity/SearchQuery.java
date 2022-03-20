package com.lly.elasticsearch.entity;

import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;

/**
 * 描述：build
 *
 * @author: liuliye
 * @since: 2022/2/25
 */
@Data
public class SearchQuery {

    private BoolQueryBuilder queryBuilder;
    private String name;
    private String rule;
    private Object value;

    public SearchQuery(BoolQueryBuilder queryBuilder, String name, String rule, Object value) {
        this.queryBuilder = queryBuilder;
        this.name = name;
        this.rule = rule;
        this.value = value;
    }
}
