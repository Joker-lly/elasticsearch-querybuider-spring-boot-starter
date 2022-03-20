package com.lly.elasticsearch.function;

/**
 * 描述：
 *
 * @author: liuliye
 * @since: 2022/2/25
 */
@FunctionalInterface
public interface QueryStrategyFunction<R,V> {

    default R apply(V v) {
        return null;
    }

    void build(V v);
}
