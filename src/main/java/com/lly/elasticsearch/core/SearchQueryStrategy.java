package com.lly.elasticsearch.core;

import com.lly.elasticsearch.EsQueryType;
import com.lly.elasticsearch.entity.SearchQuery;
import com.lly.elasticsearch.function.QueryStrategyFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述：根据 queryType 执行不同策略 组装不同的查询方式语句
 *
 * @author: liuliye
 * @since: 2022/2/25
 */
public class SearchQueryStrategy {

    public static Map<String, QueryStrategyFunction<BoolQueryBuilder, SearchQuery>> map = new HashMap<>();

    @PostConstruct
    private void initMap() {
        map.put(EsQueryType.MATCH_OR, searchQuery -> this.buildMatchOr(searchQuery));
        map.put(EsQueryType.TERM, searchQuery -> this.buildMatch(searchQuery));
        map.put(EsQueryType.MATCH_AND, searchQuery -> this.buildMatchAnd(searchQuery));
        map.put(EsQueryType.SUFFIX_FUZZY, searchQuery -> this.buildMatchSuffixFuzzy(searchQuery));
        map.put(EsQueryType.ALL_FUZZY, searchQuery -> this.buildMatchAllFuzzy(searchQuery));
        map.put(EsQueryType.PRE_FUZZY, searchQuery -> this.buildMatchPreFuzzy(searchQuery));
    }

    void buildMatchOr(SearchQuery query) {
        query.getQueryBuilder().must(QueryBuilders.matchQuery(query.getName(), query.getValue())
                .operator(Operator.OR).minimumShouldMatch("50%"));
    }

    void buildMatchAnd(SearchQuery query) {
        query.getQueryBuilder().must(QueryBuilders.matchQuery(query.getName(), query.getValue())
                .operator(Operator.AND));
    }

    void buildMatch(SearchQuery query) {
        query.getQueryBuilder().must(QueryBuilders.termQuery(query.getName(), query.getValue()));
    }

    void buildMatchAllFuzzy(SearchQuery query) {
        String value = "*" + query.getValue().toString() + "*";
        query.getQueryBuilder().must(QueryBuilders.wildcardQuery(query.getName(), value));
    }

    void buildMatchPreFuzzy(SearchQuery query) {
        String value = "*" + query.getValue().toString();
        query.getQueryBuilder().must(QueryBuilders.wildcardQuery(query.getName(), value));
    }

    void buildMatchSuffixFuzzy(SearchQuery query) {
        String value = query.getValue().toString() + "*";
        query.getQueryBuilder().must(QueryBuilders.wildcardQuery(query.getName(), value));
    }
}
