package com.lly.elasticsearch.core;


import com.lly.elasticsearch.entity.RangeQuery;
import com.lly.elasticsearch.EsQueryType;
import com.lly.elasticsearch.anno.EsQueryAnnotation;
import com.lly.elasticsearch.entity.SortField;
import com.lly.elasticsearch.entity.SearchQuery;
import com.lly.elasticsearch.function.QueryStrategyFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author LM
 * @version V1.0
 * @description 组装ES的查询条件
 * @date 2021-12-23
 */
/**
 * 描述：根据查询条件组装查询 es 查询语句
 *
 * @author: liuliye
 * @since: 2022/3/18
 */
@Slf4j
@Component
public class EsSearchQueryBuilder {

    /**
     * ES查询自动填充条件与值
     * 使用时请注意： 1、参数实体类中需要进行查询的字段，使用@EsQueryAnnotation(type = "")注解进行注释
     * 例：@EsQueryAnnotation(type = EsQueryUtil.FROM)
     * type值参考EsQueryUtil类中的值即可，不够可自己扩展
     * 扩展时请在<getWechatProgramNativeSearchQuery>方法中进行填充
     * 2、查询参数实体中日期区间字段请使用Start和End标注
     * 3、参数实体类中加入pageNo(第几页)和pageSize(每页条数)字段
     *
     * @param obj
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     * @throws ParseException
     */
    public  NativeSearchQuery initNativeSearchQuery(Object obj) throws Exception {
        long start = System.currentTimeMillis();
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        installSearchQuery(queryBuilder, obj);
        log.debug("---查询条件构造器初始化完成,耗时:" + (System.currentTimeMillis() - start) + "毫秒----");
        return queryBuilder.build();
    }

    /**
     * 组装查询条件对象
     *
     * @param nativeSearchQueryBuilder
     * @param obj
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     */
    public  void installSearchQuery(NativeSearchQueryBuilder nativeSearchQueryBuilder, Object obj) throws Exception {
        Class clazz = obj.getClass();
        PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(obj);
        //多查询条件  must 可不断添加条件
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        Map<String, Object> pageMap = new HashMap();
        Object sort = null;
        Map<String, RangeQuery> rangeMap = new HashMap();

        // 三类 排序、范围、分页、 关键字匹配

        for (PropertyDescriptor propertyDescriptor : origDescriptors) {
            //获取属性名
            String name = propertyDescriptor.getName();
            /** 获取class属性直接跳过，无需处理 */
            if ("class".equals(name)) {
                continue;
            }
            /** 获取属性类型 */
            String type = propertyDescriptor.getPropertyType().toString();
            /** 获取方法名 */
            Method methodName = clazz.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1), null);
            /**获取属性值*/
            Object value = methodName.invoke(obj, null) /*== null ? "" : methodName.invoke(obj, null).toString()*/;
            /** 将分页需要的pageNo和pageSize放入Map集合后续处理 */
            if (("pageNo".equals(name) || "pageSize".equals(name)) && !Objects.isNull(value)) {
                pageMap.put(name, value);
                continue;
            }

            /** 获取查询条件注解 */
            EsQueryAnnotation annotation = clazz.getDeclaredField(name).getAnnotation(EsQueryAnnotation.class);

            if (annotation != null && annotation.type().equals(EsQueryType.SORT)) {
                sort = value;
                continue;
            }
            // fieldName , TO OR FROM , value
            String queryType = annotation == null ? EsQueryType.TERM : annotation.type();
            if (!Objects.isNull(value)) {
                /** 确认是否需要进行区间查询 */
                if (annotation != null) {
                    packRangeQueryConditions(rangeMap, value, annotation, queryType);
                }
                // 处理普通查询
                getWechatProgramNativeSearchQuery(queryBuilder, name, queryType, value);
            }
        }

        dealWithRangeQuery(queryBuilder, rangeMap);
        /** 组装查询条件 */
        nativeSearchQueryBuilder.withQuery(queryBuilder);
        // 需要组装排序条件 多个排序条件
        sortQuery(nativeSearchQueryBuilder, sort);

        /** 组装分页查询条件 */
        if (pageMap.get("pageNo") != null && pageMap.get("pageSize") != null) {
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageMap.get("pageNo").toString()) - 1,
                    Integer.parseInt(pageMap.get("pageSize").toString())));
        }
    }

    private  void dealWithRangeQuery(BoolQueryBuilder queryBuilder, Map<String, RangeQuery> rangeMap) {

        rangeMap.forEach((fieldName, rangeQuery) -> {
            RangeQueryBuilder rangeQuery1 = rangeQuery.getRangeQuery();
            rangeQuery1.from(rangeQuery.getFromValue()).to(rangeQuery.getToValue());
            queryBuilder.must(rangeQuery1);
        });
    }

    private  void packRangeQueryConditions(Map<String, RangeQuery> rangeMap, Object value, EsQueryAnnotation annotation, String queryType) {
        if (isRangeQueryField(annotation)) {
            String fieldName = annotation.fieldName();
            if (rangeMap.containsKey(fieldName)) {
                RangeQuery esRangeQuery = rangeMap.get(fieldName);
                if (queryType.equals(EsQueryType.FROM)) {
                    esRangeQuery.setFromValue(value);
                } else {
                    esRangeQuery.setToValue(value);
                }
            } else {
                RangeQuery esRangeQuery = new RangeQuery();
                esRangeQuery.setRangeQuery(QueryBuilders.rangeQuery(fieldName));
                if (queryType.equals(EsQueryType.FROM)) {
                    esRangeQuery.setFromValue(value);
                } else {
                    esRangeQuery.setToValue(value);
                }
                rangeMap.put(fieldName, esRangeQuery);
            }
        }
    }

    private  boolean isRangeQueryField(EsQueryAnnotation annotation) {
        boolean b = false;
        if (annotation.type().equals(EsQueryType.FROM))
            b = true;
        if (annotation.type().equals(EsQueryType.TO))
            b = true;
        if (StringUtils.isEmpty(annotation.fieldName()))
            b = false;
        return b;

    }

    private  void sortQuery(NativeSearchQueryBuilder nativeSearchQueryBuilder, Object sort) {
        if (sort != null) {
            try {
                SortField sorts = (SortField) sort;
                if (sorts.getOrder().equals("desc")) {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sorts.getColumn()).order(SortOrder.DESC));
                } else {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sorts.getColumn()).order(SortOrder.ASC));
                }

            } catch (Exception e) {
            }
        }
    }

    /**
     * 根据传入参数组装NativeSearchQuery查询条件对象
     *
     * @param name
     * @param rule
     * @param value
     * @return
     */
    private  void getWechatProgramNativeSearchQuery(BoolQueryBuilder queryBuilder,
                                                          String name, String rule, Object value) {

        SearchQuery searchQuery = new SearchQuery(queryBuilder, name, rule, value);
        QueryStrategyFunction<BoolQueryBuilder, SearchQuery> function = SearchQueryStrategy.map.get(rule);

        if (function == null) {
            log.error("没有对应的查询策略,rule = " + rule);
        } else {
            function.build(searchQuery);
        }
    }
}