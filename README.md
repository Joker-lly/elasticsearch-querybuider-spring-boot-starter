### 1、项目初衷

​		本项目的初衷是简化 Spring Data Elasticsearch（spring-es，后续简称）  的使用，简化查询开发过程。

### 2、项目简介

​		当我们在写大量es查询业务的时候，会经常根据传参重复性的写一些 spring-es 的 NativeSearchQuery 的条件拼装，这在一定程度上拖慢了开发的速度。当我重复写了一些条件拼装后就在想是否能够在参数的实体类上加上注解，然后将这个参数实体类交给一个工具类，然后返回给我一个拼装好的 NativeSearchQuery，于是便有了这个小项目的开始。



### 3、使用方法

​		（1）在参数实体类中的字段上加上查询类型的注解	

```java
@Data
public class QueryData {
    @EsQueryAnnotation(type = EsQueryUtil.MATCH_OR)// 字段分词后，满足一个
    private String softName;

    @EsQueryAnnotation(type = EsQueryUtil.MATCH_AND)// 字段分词后，满足全部
    private String programName;


    @ApiComment("发表时间 开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @EsQueryAnnotation(type = EsQueryUtil.FROM,fieldName = "publishTime") // 针对某个字段范围查询
    private Date publishStartTime;

    @ApiComment("发表时间 结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @EsQueryAnnotation(type = EsQueryUtil.TO,fieldName = "publishTime") // 针对某个字段范围查询
    private Date publishEndTime;

    @EsQueryAnnotation(type = EsQueryUtil.SORT) // 要查询的字段 集合
    private List<ESSortField> isOrder;	
}
```

​		（2）在业务类里注入 EsSearchQueryBuilder 然后 调用 方法 initNativeSearchQuery（），如下代码所示

```java
@Autowired
private EsSearchQueryBuilder esSearchQueryBuilder;

public IPage<Data> queryDataList(QueryData param) {
    Page<Data> page = new Page<>();
    try {
        NativeSearchQuery nativeSearchQuery = esSearchQueryBuilder.initNativeSearchQuery(param);
        SearchHits<Data> search = elasticsearchRestTemplate.search(nativeSearchQuery, Data.class);
        List<Data> resultList = new ArrayList<>();
        search.getSearchHits().forEach(hit -> resultList.add(hit.getContent()));
        page.setRecords(resultList).setTotal(search.getTotalHits());
    } catch (Exception e) {
      	// TODO 
    }
    return page;
}

```

### 4、扩展

​	因为项目中并没有用到太多的复杂查询， 所以EsQueryType 中的查询类型，若是没有自己想要用的，可以在 EsQueryType 添加自己的查询类型，在 com.lly.elasticsearch.core.SearchQueryStrategy 类中自行扩展查询条件的组装策略

 