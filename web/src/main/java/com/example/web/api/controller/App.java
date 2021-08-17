package com.example.web.api.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

//import org.elasticsearch.action.bulk.BulkRequestBuilder;
//import org.elasticsearch.action.bulk.BulkResponse;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.action.delete.DeleteResponse;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.get.MultiGetItemResponse;
//import org.elasticsearch.action.get.MultiGetResponse;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.action.index.IndexResponse;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.action.update.UpdateRequest;
//import org.elasticsearch.action.update.UpdateResponse;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.cluster.node.DiscoveryNode;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.text.Text;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.XContentFactory;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;

//import org.elasticsearch.search.aggregations.metrics.sum.Sum;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.sort.SortOrder;

/********
 * 一定时间内没有崩溃数据的应用、版本、build信息
 *
 *
 *
 * */

public class App
{
    private static TransportClient esClient;
    //"172.28.159.37:40101,172.28.141.39:40101,172.28.141.11:40101"
//    private static final String host = "11.3.25.3:40100,11.3.25.234:40100,11.3.25.37:40100";
//
//    private static final String name = "jiesi-avatar-yingyan";
//    private static final String user = "jiesi-avatar-yingyan";
//    private static final String password = "0503777B248A1065";

    private static final String esHosts = "192.168.1.7:9300";

    private static final String name = "elasticsearch";
    private static final String user = "";
    private static final String password = "";

    static {
        System.out.println("初始化链接。。。");

        Settings settings = Settings.builder()
                .put("cluster.name", name)
                .put("client.transport.sniff", false)
//                .put("request.headers.Authorization", basicAuthHeaderValue(user.trim(), password.trim()))
                .build();

        System.out.println(basicAuthHeaderValue(user.trim(), password.trim()));

        try {
            esClient = new PreBuiltTransportClient(settings);

            String[] esHostArr = esHosts.split(",");
            List<String> esHostPortList = CollectionUtils.arrayAsArrayList(esHostArr);
            for (String eachHostPort : esHostPortList) {
                System.out.println("adding [{" + eachHostPort + "}] to TransportClient ... ");
                String[] hostPortTokens = eachHostPort.split(":");
                if (hostPortTokens.length < 2) {
                    throw new Exception("ERROR: bad ElasticSearch host:port configuration - wrong format: " +
                            eachHostPort);
                }
                // default ES port
                int port = 9300;
                try {
                    port = Integer.parseInt(hostPortTokens[1].trim());
                } catch (Throwable e) {
                    System.out.println("ERROR parsing port from the ES config [{"  + eachHostPort + " }]- using default port 9300");
                }
                esClient.addTransportAddress(new TransportAddress(InetAddress.getByName(hostPortTokens[0]), port));
            }

            List<DiscoveryNode> nodes = esClient.connectedNodes();

            for (DiscoveryNode node : nodes) {
                System.out.println(node.getHostAddress());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("client", "apple"))
                .filter(QueryBuilders.rangeQuery("createTime").gte("now-" + Integer.toString(3) +  "M"));
        Map<String, Object> result1 = testSearch(queryBuilder1);

        BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("client", "apple"))
                .filter(QueryBuilders.rangeQuery("createTime").lt("now-" + Integer.toString(3) +  "M"));
        Map<String, Object> result2 = testSearch(queryBuilder2);

        System.out.println(result1);
        System.out.println(result2);

        for (Map.Entry<String, Object> appEntry : result2.entrySet()) {
            for (Map.Entry<String, Object> versionEntry : ((Map<String, Object>)result2.get(appEntry.getKey())).entrySet()) {

            }
        }
    }

    /**
     * 基础的base64生成 JDK使用1.8以下的，可自行找相应的base64工具
     *
     * @param username 用户名
     * @param passwd   密码
     * @return
     */
    private static String basicAuthHeaderValue(String username, String passwd) {
        CharBuffer chars = CharBuffer.allocate(username.length() + passwd.length() + 1);
        byte[] charBytes = null;
        try {
            chars.put(username).put(':').put(passwd.toCharArray());
            charBytes = toUtf8Bytes(chars.array());
            String basicToken = new String(Base64.encodeBase64(charBytes));
            return "Basic " + basicToken;
        } finally {
            Arrays.fill(chars.array(), (char) 0);
            if (charBytes != null) {
                Arrays.fill(charBytes, (byte) 0);
            }
        }
    }

    private static byte[] toUtf8Bytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    /**
     * 查询近months个月的上报崩溃数据的应用信息
     *
     * months 查询月数：1~12
     * client 客户端类型：android、client
     * @author suliri1
     */
    public static Map<String, Object> testSearch(BoolQueryBuilder queryBuilder) {

        SearchResponse searchResponse = esClient
                .prepareSearch("app_crash_yingyan_simplify_data_*")
                .setTypes("simplify_data")
                .setSize(0)
                .setQuery(queryBuilder)
                //TODO
                //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addAggregation(AggregationBuilders.terms("app").field("appId").size(10000)
                        .subAggregation(AggregationBuilders.terms("version").field("clientVersion").size(10000)
                                .subAggregation(AggregationBuilders.terms("build").field("buildCode").size(10000))))
                .execute()
                //TODO 需要timeout？
                .actionGet();

        // 转为Map
        Terms appTerms = searchResponse.getAggregations().get("app");
        Map<String, Object> result = new LinkedHashMap<>();
        for (Terms.Bucket appBucket : appTerms.getBuckets()) {
            String appKey = appBucket.getKey().toString();
            Terms versionTerms = appBucket.getAggregations().get("version");
            Map<String, Object> versionList = new LinkedHashMap<>();
            for (Terms.Bucket versionBucket : versionTerms.getBuckets()) {
                String versionKey = versionBucket.getKey().toString();
                Terms buildTerms = versionBucket.getAggregations().get("build");
                List<String> buildList = new LinkedList<>();
                for (Terms.Bucket buildBucket : buildTerms.getBuckets()) {
                    buildList.add(buildBucket.getKey().toString());
                }
                versionList.put(versionKey, buildList);
            }
            result.put(appKey, versionList);
        }
        return result;
    }
//    /**
//     * 功能描述: 增加索引
//     *
//     * @throws Exception void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void addIndex() throws Exception{
//        System.out.println("============分=====割=====线=============");
//
//        XContentBuilder source = createJson4();
//        // 存json入索引中
//        IndexResponse response = client.prepareIndex("twitter", "tweet", "4").setSource(source).get();
//        // 结果获取
//        String index = response.getIndex();
//        String type = response.getType();
//        String id = response.getId();
//        long version = response.getVersion();
//        boolean created = response.isCreated();
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("index", index);
//        result.put("type", type);
//        result.put("id", id);
//        result.put("version", version);
//        result.put("created", created);
//
//        System.out.println(JSON.toJSONString(result));
//    }
//
//
//    /**
//     * 功能描述:删除索引
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void delIndex() {
//        System.out.println("======================删==除===分===割==线=================");
//
//        DeleteResponse deleteResponse = client.prepareDelete("twitter", "tweet", "1").get();
//        String index = deleteResponse.getIndex();
//        String type = deleteResponse.getType();
//        String id = deleteResponse.getId();
//        Long version = deleteResponse.getVersion();
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("index", index);
//        result.put("type", type);
//        result.put("id", id);
//        result.put("version", version);
//
//        System.out.println(JSON.toJSONString(result));
//    }
//
//
//
//    /**
//     * 功能描述: 获取数据
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void getData() {
//        System.out.println("=================获==取==数==据==============");
//        GetResponse getResponse = client.prepareGet("tom", "cat", "2").execute().actionGet();
//        String id = getResponse.getId();
//        String resultStr = getResponse.getSourceAsString();
//        System.out.println("id" + id);
//        System.out.println("resultStr:" + resultStr);
//    }
//
//
//    /**
//     *
//     * */
//    public static void testGetThread() {
//        //设置线程安全
//        GetResponse  getresponse = client.prepareGet("tom", "cat", "2").setOperationThreaded(false).get();
//        System.out.println(getresponse.getSourceAsString());
//    }
//
//
//
//    /**
//     * 功能描述: 更新ES数据
//     *
//     * @throws IOException
//     * @throws InterruptedException
//     * @throws ExecutionException void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testUpdate() throws IOException, InterruptedException, ExecutionException {
//        UpdateRequest updateRequest = new UpdateRequest();
//        updateRequest.index("tom");
//        updateRequest.type("cat");
//        updateRequest.id("2");
//        updateRequest.doc(XContentFactory.jsonBuilder()
//                .startObject()
//                .field("phone", "17686886688")
//                .field("color", "blue")
//                .endObject());
//
//        UpdateResponse updateResponse = client.update(updateRequest).get();
//        String index = updateResponse.getIndex();
//        String type = updateResponse.getType();
//        String id = updateResponse.getId();
//        long version = updateResponse.getVersion();
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("index", index);
//        result.put("type", type);
//        result.put("id", id);
//        result.put("version", version);
//
//        System.out.println(JSON.toJSONString(result));
//    }
//
//
//    /**
//     * 功能描述: 更新数据
//     *
//     * @throws IOException
//     * @throws InterruptedException
//     * @throws ExecutionException void
//     * @version 1.0.6
//     * @author yaoyaowang
//     */
//    public static void testUpdate1() throws IOException, InterruptedException, ExecutionException {
//        UpdateRequest updateRequest = new UpdateRequest("tom", "cat", "2");
//        updateRequest.doc(XContentFactory.jsonBuilder()
//                .startObject()
//                .field("phone", "1366688866")
//                .endObject());
//
//        UpdateResponse updateResponse = client.update(updateRequest).get();
//        String index = updateResponse.getIndex();
//        String type = updateResponse.getType();
//        String id = updateResponse.getId();
//        long version = updateResponse.getVersion();
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("index", index);
//        result.put("type", type);
//        result.put("id", id);
//        result.put("version", version);
//
//        System.out.println(JSON.toJSONString(result));
//    }
//
//
//    /**
//     * 功能描述: 新增数据，如果没有插入，如果有更新
//     *
//     * @throws IOException
//     * @throws InterruptedException
//     * @throws ExecutionException void
//     * @version 1.0.6
//     * @author yaoyaowang
//     */
//    public static void testUpSert() throws IOException, InterruptedException, ExecutionException {
//        //查询该数据是否存在，如果不存在则插入
//        IndexRequest indexRequest = new IndexRequest("tom", "cat", "1");
//        indexRequest.source(XContentFactory.jsonBuilder()
//                .startObject()
//                .field("name", "staven")
//                .field("id", "2")
//                .field("age", "20")
//                .field("sex", "F")
//                .field("address", "America")
//                .field("phone", "110")
//                .field("color", "yello"));
//
//        //更新该数据，看该数据是否存在，如果不存在，则把上面的数据插入，如果存在则把数据更新（将"address", "America" 更新为 "address", "America"）
//        UpdateRequest updateRequest = new UpdateRequest("tom", "cat", "1");
//        updateRequest.doc(XContentFactory.jsonBuilder()
//                .startObject()
//                .field("address", "America")
//                .endObject());
//        updateRequest.upsert(indexRequest);
//
//        UpdateResponse updateResponse = client.update(updateRequest).get();
//
//        String index = updateResponse.getIndex();
//        String type = updateResponse.getType();
//        String id = updateResponse.getId();
//        long version = updateResponse.getVersion();
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("index", index);
//        result.put("type", type);
//        result.put("id", id);
//        result.put("version", version);
//
//        System.out.println(JSON.toJSONString(result));
//    }
//
//
//    /**
//     * 功能描述: 增加索引
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testMultiGet() {
//        MultiGetResponse multiGetResponse = client.prepareMultiGet()
//                .add("tom", "cat", "1")
//                .add("tom", "cat", "2")
//                .add("twitter", "tweet", "1")
//                .get();
//
//        for(MultiGetItemResponse itemReponse : multiGetResponse){
//            GetResponse getResponse = itemReponse.getResponse();
//            if(getResponse.isExists()){
//                String sourceAsString = getResponse.getSourceAsString();
//                System.out.println(sourceAsString);
//            }
//        }
//
//    }
//
//
//    /**
//     * 功能描述: 批处理
//     *
//     * @throws Exception void
//     * @version 1.0.6
//     * @author yaoyaowang
//     */
//    public static void testBulk() throws Exception {
//        BulkRequestBuilder bulkBuilder = client.prepareBulk();
//        bulkBuilder.add(client.prepareIndex("tom", "cat", "3")
//                .setSource(XContentFactory.jsonBuilder()
//                        .startObject()
//                        .field("id", "2")
//                        .field("name", "rose")
//                        .field("age", "18")
//                        .field("sex", "F")
//                        .field("address", "tianjin")
//                        .field("color", "black")
//                        .field("phone", "120")
//                        .endObject()));
//
//        bulkBuilder.add(client.prepareIndex("tom", "cat", "1")
//                .setSource(XContentFactory.jsonBuilder()
//                        .startObject()
//                        .field("address", "England")
//                        .endObject()));
//
//        BulkResponse bulkResponse = bulkBuilder.get();
//        System.out.println(bulkResponse.getHeaders());
//    }
//
//
//    /**
//     * 功能描述: 统计索引下的数据
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testCount(){
//        long num = client.prepareCount("twitter").get().getCount();
//        System.out.println(String.format("twitter的总数为：%d", num));
//    }
//
//


//    /**
//     * 功能描述: 筛选
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testFilter() {
//        SearchResponse searchResponse = client.prepareSearch("twitter").setTypes("tweet")
//                .setQuery(QueryBuilders.matchAllQuery())
//                .setPostFilter(QueryBuilders.rangeQuery("age").gte(17))
//                .setFrom(0).addSort("id", SortOrder.ASC)
//                .get();
//
//        SearchHits searchHits = searchResponse.getHits();
//        Long total = searchHits.getTotalHits();
//        System.out.println("年龄大于17的数据总数" + total);
//        SearchHit[] searchHitArr = searchHits.getHits();
//        for(SearchHit searchHit : searchHitArr){
//            System.out.println(searchHit.getSourceAsString());
//        }
//    }
//
//
//    /**
//     * 功能描述: 高亮
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testHighLight() {
//        SearchResponse searchResponse = client.prepareSearch("twitter").setTypes("tweet")
//                .setQuery(QueryBuilders.matchQuery("name", "tom"))
//                .setSearchType(SearchType.QUERY_THEN_FETCH)
//                .addHighlightedField("name")
//                .setHighlighterPreTags("<font color='red'>")
//                .setHighlighterPostTags("</font>")
//                .get();
//
//        SearchHits searchHits = searchResponse.getHits();
//        long total = searchHits.getTotalHits();
//        System.out.println("索引twitter下的所有数据的总数：" + total);
//        SearchHit[] searchHitArr = searchHits.getHits();
//        for(SearchHit searchHit : searchHitArr){
//            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
//            HighlightField highlightField = highlightFields.get("name");
//            if(null != highlightField){
//                Text[] fragments = highlightField.fragments();
//                System.out.println("这是个什么鬼呢？ ---> " + fragments[0]);
//            }
//
//            System.out.println(searchHit.sourceAsString());
//        }
//    }
//
//
//
//    /**
//     * 功能描述: 分组统计
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testGroupBy() {
//        SearchResponse searchResponse = client.prepareSearch("twitter").setTypes("tweet")
//                .setQuery(QueryBuilders.matchAllQuery())
//                .setSearchType(SearchType.QUERY_THEN_FETCH)
//                .addAggregation(AggregationBuilders.terms("group_age").field("age").size(0))
//                .get();
//
//        Terms terms = searchResponse.getAggregations().get("group_age");
//        List<Bucket> buckets = terms.getBuckets();
//        for(Bucket bucket : buckets){
//            System.out.println(bucket.getKey() + ":" + bucket.getDocCount());
//        }
//    }
//
//
//
//
//    /**
//     * 功能描述: 聚合
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testAggregationFunction() {
//        SearchResponse searchResponse = client.prepareSearch("twitter").setTypes("tweet")
//                .setQuery(QueryBuilders.matchAllQuery())
//                .setSearchType(SearchType.QUERY_THEN_FETCH)
//                .addAggregation(AggregationBuilders.terms("group_name").field("name")
//                        .subAggregation(AggregationBuilders.sum("age_count").field("age")))
//                .get();
//
//        Terms terms = searchResponse.getAggregations().get("group_name");
//        List<Bucket> buckets = terms.getBuckets();
//        for(Bucket bucket : buckets){
//            Sum sum = bucket.getAggregations().get("age_count");
//            System.out.println("{" + bucket.getKey() + ":" + bucket.getDocCount() + ":" + sum.getValue() + "}");
//        }
//    }
//
//
//
//    /**
//     * 功能描述: 造数据
//     *
//     * @throws IOException void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void generateOtherIndexData() throws IOException {
//        for(int i = 1; i <= 60; i++){
//            XContentBuilder source = XContentFactory.jsonBuilder()
//                    .startObject()
//                    .field("id", i)
//                    .field("name", createName())
//                    .field("sex", (i < 30 ? "F" : "M"))
//                    .field("age", createAge())
//                    .field("address", "china")
//                    .endObject();
//
//            client.prepareIndex("ycu", "computer", String.valueOf(i-1)).setSource(source).get();
//        }
//    }
//
//
//
//    /**
//     * 功能描述: 创建人名
//     *
//     * @return String
//     * @version 1.0.6
//     * @author king
//     */
//    public static String createName(){
//        Random random = new Random();
//        String[] str = {"q","w","e","r","t","y","u","i","o","p","a","s","f","d","g","h","j","k","l","z","x","c","v","b","n","m"};
//        random.nextInt(23);
//        String name = new StringBuffer(str[random.nextInt(23)]).append(str[random.nextInt(23)])
//                .append(str[random.nextInt(23)]).toString();
//        return name;
//    }
//
//    /**
//     * 功能描述: 创建年龄
//     *
//     * @return int
//     * @version 1.0.6
//     * @author king
//     */
//    public static int createAge(){
//        Random random = new Random();
//        String[] num = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
//        String ageStr = new StringBuffer(num[random.nextInt(10)]).append(num[random.nextInt(10)]).toString();
//        return Integer.parseInt(ageStr);
//    }
//
//
//    /**
//     * 功能描述: 指定分片区查询数据
//     *  void
//     * @version 1.0.6
//     * @author king
//     */
//    public static void testPreference(){
//        SearchResponse searchResponse = client.prepareSearch("ycu").setTypes("computer").setPreference("_shards:0,1")
//                .setQuery(QueryBuilders.matchAllQuery()).setExplain(true).setFrom(0).setSize(60).get();
//
//        SearchHits searchHits = searchResponse.getHits();
//        long total = searchHits.getTotalHits();
//        SearchHit[] searchHitArr = searchHits.getHits();
//        System.out.println("总数：" + total);
//        for(SearchHit searchHit : searchHitArr){
//            System.out.println(searchHit.getSourceAsString());
//        }
//    }
//
//
//
//
//
//    /**
//     * 使用es的帮助类
//     */
//    public static XContentBuilder createJson4() throws Exception {
//        // 创建json对象, 其中一个创建json的方式
//        XContentBuilder source = XContentFactory.jsonBuilder()
//                .startObject()
//                .field("id", "5")
//                .field("name", "tom")
//                .field("sex", "F")
//                .field("age", 19)
//                .field("address", "china")
//                .endObject();
//        return source;
//    }
}
