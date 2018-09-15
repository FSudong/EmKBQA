package tools.es;

import lombok.experimental.var;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.nd4j.linalg.api.ops.impl.controlflow.compat.Exit;
//import utils.w2v.Word2VecESModel;
//import utils.w2v.Word2VecGensimModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.*;
import static tools.es.Configuration.ES_INDEX_CCKS;



public class SearchFAQ {

    /**
     * term query,输入检索的字段范围，返回json格式的字符串
     * @return List<String>
     * @throws UnknownHostException
     */
    public static List<String> query(QueryBuilder queryBuilder, String indexName){
        List<String> hitContent = new ArrayList<String>();
        try {
            TransportClient client = GetClient.getTransportClient();



            SearchResponse scrollResp = client.prepareSearch(indexName)
//                .addSort("num",SortOrder.ASC)
                    .setScroll(new TimeValue(60000))
                    .setQuery(queryBuilder)
                    .setSize(1000).execute().actionGet(); //100 hits per shard will be returned for each scroll

            for(SearchHit hit: scrollResp.getHits().getHits()){
                    hitContent.add(hit.getSourceAsString());
            }
//            while(scrollResp.getHits().getHits().length != 0){
//                for(SearchHit hit: scrollResp.getHits().getHits()){
//                    hitContent.add(hit.getSourceAsString());
//                }
//                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
//
//            }
//        client.close();

        }catch (Exception e){
            System.out.println(e);
        }

        return hitContent;
    }


    //匹配整个短语 不会IK分词
    public  static List<String> matchPhraseQuery_first(String filed,String term,String indexName) throws UnknownHostException {
        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(filed,term);
        return query(queryBuilder, indexName);
    }

    //多字段 多条件匹配 同时包含ev的匹配
    public  static List<String> match_bool_fields(String indexName, String[] terms, String[] fields) throws UnknownHostException {
//        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(filed,term);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for(String term:terms){
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(term,fields));
        }
//        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(entity,"subject","object"));
        return query(boolQueryBuilder, indexName);
    }


    //筛选模糊搜索的结果 实现精确搜索
    public  static  List<String> accurateQuery_second(String filed,String term,String indexName) throws UnknownHostException {
        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(filed,term);
//        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(term,"subject","object");
        //模糊查询的所有结果
        List<String> accurate_all = new ArrayList<String>();
        List<String> query_all = query(queryBuilder, indexName);
        if(query_all.isEmpty()||query_all.size()==0)
            return accurate_all;

        //遍历筛选与 term一致的 结果，实现精确查询
        for(String s : query_all){
            int begin = s.indexOf("name");
            int end  = s.indexOf("\",\"entity");
            String s_name = s.substring(begin+7,end);
            if(s_name.equalsIgnoreCase(term)){
//                System.out.println(s);
                accurate_all.add(s);
            }
        }
        return accurate_all;
    }


    //筛选模糊搜索的结果 实现精确搜索 词典WDIC
    public  static  double[] accurateQuery_WDIC(String field,String term,String indexName) throws UnknownHostException {
        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(field,term);
        //模糊查询的所有结果
        List<String> query_all = query(queryBuilder, indexName);
        List<String> accurate_all = new ArrayList<String>();
        //遍历筛选与 term一致的 结果，实现精确查询
        if(query_all.size()==0||query_all==null)
        {
            double[] doubles = null;
            return doubles;
        }
        for(String s : query_all){
            int begin = s.indexOf("termName");
            int end  = s.indexOf("\",\"vector");
            String s_name = s.substring(begin+11,end);
            if(s_name.equalsIgnoreCase(term)){
//                System.out.println(s);
                accurate_all.add(s);
            }
        }
//
//        if(accurate_all==null||accurate_all.isEmpty()||accurate_all.size()==0)
//        {
//            int length = 0;
//            try {
//                length = Word2VecESModel.getInstance().getVecLength();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return (new double[length]);
//        }

        //分析搜索结果 split 得到每一维度的值
        int vector_begin = accurate_all.get(0).indexOf("vector")+9;
        int vector_end = accurate_all.get(0).length()-2;
        String vector = accurate_all.get(0).substring(vector_begin,vector_end);
        String[] vector_value = vector.split(" ");
        //将string表示的值 转化为 double类型
        double[] doubles = new double[vector_value.length];
        int len  = 0;
        for(String s : vector_value){
            doubles[len++] = Double.valueOf(s);
        }
//        System.out.println("共计："+accurate_all.size()+"条\r\n搜索结果："+accurate_all.get(0)+"\r\ndouble向量长度："+doubles.length+"\r\n");

        return doubles;
    }
    //筛选模糊搜索的结果 实现精确搜索  实体类别 TYPE
    public  static  String[] accurateQuery_TYPE(String field,String term,String indexName) throws UnknownHostException {
        QueryBuilder queryBuilder1 = QueryBuilders.matchPhraseQuery(field,term);
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(term,"subject","object");
        //模糊查询的所有结果
        System.out.println("模糊搜索中...");
        List<String> query_all = query(queryBuilder, indexName);
        List<String> accurate_all = new ArrayList<String>();
        System.out.println("模糊搜索成功，共："+query_all.size());
        //遍历筛选与 term一致的 结果，实现精确查询
        if(query_all.size()==0||query_all==null)
        {
            String[] strings = null;
            return strings;
        }
        for(String s : query_all){
            int begin  = s.indexOf("\"subject\":\"");
            int end  = s.indexOf("\",\"predicate");
            String s_name = s.substring(begin+11,end);
            if(s_name.equalsIgnoreCase(term) || s_name.indexOf(term)!=-1){
//                System.out.println(s)
                accurate_all.add(s);
            }
        }

        if(accurate_all==null||accurate_all.isEmpty()||accurate_all.size()==0)
        {
            String[] strings = null;
            return strings;
        }
        String[] strings = accurate_all.toArray(new String[accurate_all.size()]);
        System.out.println("成功筛选出精确值，共："+strings.length);
        return strings;
    }


    public static void indexFaqData() throws IOException {

        //默认建立一个名为"qataskccks-test"的索引
        String indexName = ES_INDEX_CCKS;
        TransportClient client = GetClient.getTransportClient();
        //启动系统的时候判断索引是否存在
        IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);
        boolean exist_1 = client.admin().indices().exists(inExistsRequest).actionGet().isExists();
        System.out.println(indexName + " is existed or not（test）: " + exist_1);
        //进行建立
        IndexFile.indexFaqData();
        //再判断是否存在该索引
        IndicesExistsRequest inExistsRequest2 = new IndicesExistsRequest(indexName);
        boolean exist_2 = client.admin().indices().exists(inExistsRequest2).actionGet().isExists();
        System.out.println(indexName + " is existed or not（test）: " + exist_2);
    }

    public static void termVector_Test() throws IOException {
        //初始化 词向量索引 若不存在 则新建
        IndexFile.indexFaqData(DataSource.WDIC);

        //ES精确搜索
        String indexname = Configuration.ES_INDEX_WDIC;
        String s1 = "区号";
        String s2 = "橘子";
        String s3 = "香蕉";
        double[] d1 = SearchFAQ.accurateQuery_WDIC("termName", s1, indexname);
        double[] d2 = SearchFAQ.accurateQuery_WDIC("termName", s2, indexname);
        double[] d3 = SearchFAQ.accurateQuery_WDIC("termName", s3, indexname);
//        System.out.printf("\"%s\"-\"%s\": %f.\r\n", s1, s2, Word2VecGensimModel.calcVecSimilarity(d1,d2));
//        System.out.printf("\"%s\"-\"%s\": %f.\r\n", s2, s3, Word2VecGensimModel.calcVecSimilarity(d2,d3));

//        //使用从内存读取的方法
//        Word2VecGensimModel word2VecGensimModel = Word2VecGensimModel.getInstance();
//        System.out.printf("\"苹果\"-\"橘子\": %f.\r\n",word2VecGensimModel.calcWordSimilarity("苹果","橘子"));
//        System.out.printf("\"苹果\"-\"橘子\": %f.\r\n",word2VecGensimModel.calcWordSimilarity("苹果","香蕉"));

//        System.out.print("delete this index?(Y/N)");
//        Scanner scanner = new Scanner(System.in);
//        String s = scanner.nextLine();
//        if(s.equalsIgnoreCase("Y")){
//            DeleteIndex.deleteIndexW2V();
//        }
//        System.out.println("end");
    }

    public static void main(String[] args) {
        try {
            IndexFile.indexFaqData(DataSource.FreeBase);

            Scanner scanner = new Scanner(System.in);
            while(true){
                System.out.print("请输入要查找的字符串：");
                String term = scanner.nextLine();
                String[] strings = null;
                strings = accurateQuery_TYPE("subject",term,"freebase");
                if(strings==null){
                    System.out.println("无匹配数据");

                }
                else {

                    for(String s:strings){
                        JSONObject json = new JSONObject(s);
                        System.out.println(json.get("subject")+" -->> "+json.get("predicate")+" -->> "+json.get("object"));
                    }
                }
//                System.out.print("请输入要查找的字符串：");
//                inputString = br.readLine();
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
