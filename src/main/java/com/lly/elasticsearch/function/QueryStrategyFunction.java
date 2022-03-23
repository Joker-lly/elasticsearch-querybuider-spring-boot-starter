package com.lly.elasticsearch.function;

/**
 * 描述：函数式接口
 *
 * @author: liuliye
 * @since: 2022/2/25
 */
@FunctionalInterface
public interface QueryStrategyFunction<V> {

    void build(V v);
}
