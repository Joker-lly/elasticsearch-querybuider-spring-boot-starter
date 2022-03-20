package com.lly.elasticsearch;

/**
 * 描述：组装查询条件时字段查询使用的查询方式
 *
 * @author: liuliye
 * @since: 2022/3/18
 */
public class EsQueryType {

    /**
     * 精确查询指定字段
     */
    public static final String TERM = "term";
    /**
     * 按分词器进行模糊查询
     */
    public static final String MATCH_OR = "matchOr";

    /**
     * 大于等于
     */
    public static final String FROM = "from";
    public static final String GTE = "gte";

    /**
     * 小于等于
     */
    public static final String TO = "to";
    /**
     * 小于等于
     */
    public static final String LTE = "lte";

    /**
     * 小于等于
     */
    public static final String MATCH_AND = "matchAnd";

    /**
     * 小于等于
     */
    public static final String SORT = "sort";

    /**
     * 小于等于
     */
    public static final String ALL_FUZZY = "allFuzzy";

    /**
     * 小于等于
     */
    public static final String PRE_FUZZY = "preFuzzy";

    /**
     * 小于等于
     */
    public static final String SUFFIX_FUZZY = "suffixFuzzy";

}
