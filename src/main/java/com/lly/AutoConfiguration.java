package com.lly;

import com.lly.elasticsearch.core.EsSearchQueryBuilder;
import com.lly.elasticsearch.core.SearchQueryStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 描述：配置文件
 *
 * @author: liuliye
 * @since: 2022/3/18
 */
@Configuration
@Import({EsSearchQueryBuilder.class, SearchQueryStrategy.class})
public class AutoConfiguration {
}
