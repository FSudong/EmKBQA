package tools.es;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class IndexFile {


    //判断当前索引 类型 是否存在
    private static boolean _init(String indexName, String type, String filePath) {
        try {
            TransportClient client = GetClient.getTransportClient();

            //启动系统的时候判断索引是否存在
            IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);
            //如果已经存在的话就更新数据；如果不存在就创建索引。
            IndicesExistsResponse indicesExistsResponse = client.admin().indices().exists(inExistsRequest).actionGet();
            System.out.println(indexName + " is existed or not: " + indicesExistsResponse.isExists());
            if (indicesExistsResponse.isExists()) {
                //索引index已经存在，则继续检查是不是存在该type
                TypesExistsResponse typesExistsResponse = client.admin().indices().prepareTypesExists(indexName).setTypes(type).get();
                System.out.println(type + " is existed or not: " + typesExistsResponse.isExists());
                //如果type已经存在，直接返回
                if(typesExistsResponse.isExists()){
                    return true;
                }
                else{ //type不存在，则定义索引结构
                    PutMappingRequest putMappingRequest = null;
                    if(type.equalsIgnoreCase(Configuration.ES_TYPE_ENTITY)){
                        putMappingRequest = Requests.putMappingRequest(indexName).type(type).source(_getModifiedMappingForTemplate(type));
                    }
                    else if(type.equalsIgnoreCase(Configuration.ES_TYPE_WDIC)){
                        putMappingRequest = Requests.putMappingRequest(indexName).type(type).source(_getModifiedMappingForWDIC(type));
                    }
                    else if(type.equalsIgnoreCase(Configuration.ES_TYPE_TYPE)){
                        putMappingRequest = Requests.putMappingRequest(indexName).type(type).source(_getModifiedMappingForTYPE(type));
                    }
                    else if(type.equalsIgnoreCase(Configuration.ES_TYPE_SPO)){
                        putMappingRequest = Requests.putMappingRequest(indexName).type(type).source(_getModifiedMappingForSPO(type));
                    }
                    PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();

                    System.out.println("已定义index:"+indexName+"-"+type);
                    return false;
                }
            }
            else{
                //定义索引结构
                XContentBuilder mapping = null;
                if(type.equalsIgnoreCase(Configuration.ES_TYPE_ENTITY)){
                    mapping = _getDefinitionMappingForTemplate(indexName, type);
                }
                else if (type.equalsIgnoreCase(Configuration.ES_TYPE_WDIC)){
                    mapping = _getDefinitionMappingForWDIC(indexName, type);
                }
                else if (type.equalsIgnoreCase(Configuration.ES_TYPE_TYPE)){
                    mapping = _getDefinitionMappingForTYPE(indexName, type);
                }
                else if (type.equalsIgnoreCase(Configuration.ES_TYPE_SPO)){
                    mapping = _getDefinitionMappingForSPO(indexName,type);
                }
//               这里应该有问题  都用了Configuration.ES_TYPE_ENTITY作为了参数
                IndexResponse createIndexResponse = client.prepareIndex(indexName,Configuration.ES_TYPE_SPO)
                        .setSource(mapping)
                        .get();
                System.out.println("已定义index:"+indexName+"-"+type);
                return false;


//                //定义索引结构
//                XContentBuilder mapping = null;
//                if(type.equalsIgnoreCase(Configuration.ES_TYPE_FAQ)){
//                    mapping = _getDefinitionMappingForTemplate(indexName, type);
//                }
//                System.out.println(mapping.toString());
//                //创建索引
//                CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices()
//                        .prepareCreate(indexName)
//                        .setSource(mapping);
////                System.out.println();
//                CreateIndexResponse createIndexResponse = createIndexRequestBuilder.execute().actionGet();
//                System.out.println("已定义index:"+indexName+"-"+type);
//                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    //已有索引，增加新的template type;
    private static XContentBuilder _getModifiedMappingForTemplate(String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties") //下面是设置文档属性
                    .startObject("name").field("type", "string").field("store", "yes")
                    .field("index","not_analyzed")
                    .endObject()
                    .startObject("entity").field("type", "text")
                    .endObject()
                    .startObject("rank").field("type", "text")
                    .endObject()
                .endObject()
                .endObject();
        return mapping;
    }
    //已有索引，增加新的dicts type;
    private static XContentBuilder _getModifiedMappingForWDIC(String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties") //下面是设置文档属性
                .startObject("termName").field("type", "string").field("store", "yes")
                .field("index","not_analyzed")
                .endObject()
                .startObject("vector").field("type", "text")
                .endObject()

                .endObject()
                .endObject();
        return mapping;
    }
    //已有索引，增加新的entity's TYPE type;
    private static XContentBuilder _getModifiedMappingForTYPE(String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties") //下面是设置文档属性
                .startObject("entityName").field("type", "text").field("store", "yes")
                .field("index","not_analyzed")
                .endObject()
                .startObject("typeName").field("type", "text")
                .endObject()

                .endObject()
                .endObject();
        return mapping;
    }
    //已有索引，增加新的entity's TYPE type;
    private static XContentBuilder _getModifiedMappingForSPO(String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties") //下面是设置文档属性
                .startObject("subject").field("type", "text").field("store", "yes")
                .endObject()
                .startObject("predicate").field("type", "text").field("store", "yes")
                .endObject()
                .startObject("object").field("type", "text").field("store", "yes")
                .endObject()
                .endObject()
                .endObject();
        return mapping;
    }
    //没有索引，建立索引和增加新的template type; for 词条 实体 排名
    private static XContentBuilder _getDefinitionMappingForTemplate(String indexName, String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("settings")
                .field("number_of_shards", 1) //设置分片的数量
                .field("number_of_replicas", 1) //设置副本数量
                .endObject()
                .startObject("mappings")
                .startObject(typeFaq) // type名称
                .startObject("_all")
                .field("enabled", "false")
                .endObject()

                .startObject("properties") //下面是设置文档属性
                .startObject("name").field("type", "string").field("store", "yes")
                .field("index","not_analyzed")
                .endObject()
                .startObject("entity").field("type", "text").field("analyzer", "index_ansj")
                .field("search_analyzer", "query_ansj").field("store", "yes")
                .endObject()
                .startObject("rank").field("type", "text").field("analyzer", "index_ansj")
                .field("search_analyzer", "query_ansj").field("store", "yes")
                .endObject()
                .endObject()

                .endObject()
                .endObject()
                .endObject();

        return mapping;
    }

    //没有索引，建立索引和增加新的template type; for 词向量索引
    private static XContentBuilder _getDefinitionMappingForWDIC(String indexName, String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("settings")
                .field("number_of_shards", 1) //设置分片的数量
                .field("number_of_replicas", 1) //设置副本数量
                .endObject()
                .startObject("mappings")
                .startObject(typeFaq) // type名称
                .startObject("_all")
                .field("enabled", "false")
                .endObject()

                .startObject("properties") //下面是设置文档属性
                .startObject("termName").field("type", "string").field("store", "yes")
                .field("index","not_analyzed")
                .endObject()
                .startObject("vector").field("type", "text").field("analyzer", "index_ansj")
                .field("search_analyzer", "query_ansj").field("store", "yes")
                .endObject()
                .endObject()

                .endObject()
                .endObject()
                .endObject();

        return mapping;
    }

    //没有索引，建立索引和增加新的entity's TYPE type; for 寻找某类实体
    private static XContentBuilder _getDefinitionMappingForTYPE(String indexName, String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("settings")
                .field("number_of_shards", 1) //设置分片的数量
                .field("number_of_replicas", 1) //设置副本数量
                .endObject()
                .startObject("mappings")
                .startObject(typeFaq) // type名称
                .startObject("_all")
                .field("enabled", "false")
                .endObject()

                .startObject("properties") //下面是设置文档属性
                .startObject("entityName").field("type", "string").field("store", "yes")
                .field("index","not_analyzed")
                .endObject()
                .startObject("typeName").field("type", "text").field("analyzer", "index_ansj")
                .field("search_analyzer", "query_ansj").field("store", "yes")
                .endObject()
                .endObject()

                .endObject()
                .endObject()
                .endObject();

        return mapping;
    }

    //没有索引，建立索引和增加新的entity's TYPE type; for 寻找某类实体
    private static XContentBuilder _getDefinitionMappingForSPO(String indexName, String typeFaq) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("settings")
                .field("number_of_shards", 1) //设置分片的数量
                .field("number_of_replicas", 1) //设置副本数量
                .endObject()
                .startObject("mappings")
                .startObject(typeFaq) // type名称
                .startObject("_all")
                .field("enabled", "false")
                .endObject()

                .startObject("properties") //下面是设置文档属性
                .startObject("subject").field("type", "text").field("store", "yes")
                .endObject()
                .startObject("predicate").field("type", "text").field("store", "yes")
                .endObject()
                .startObject("object").field("type", "text").field("store", "yes")
                .endObject()
                .endObject()

                .endObject()
                .endObject()
                .endObject();

        return mapping;
    }
    public static void indexFaqData() throws IOException {
        //默认使用ENTITY
        indexFaqData(DataSource.FreeBase);
    }

    //将数据索引至elasticsearch中
    public static void indexFaqData(DataSource... dataSources) throws IOException {
        for(DataSource dataSource: dataSources){
            long n = 0 ,patch = 50000;//第n个数据 每patch个索引一次
            //选择数据源
            String indexName;
            String filePath;
            String typeFaq;
            //选择索引的faq来源
            switch (dataSource) {
                case ENTITY://词条 实体 排名
                    indexName = Configuration.ES_INDEX_CCKS;
                    filePath = FileConfig.FILE_ENTITY;
                    typeFaq = Configuration.ES_TYPE_ENTITY;
                    break;
                case WDIC://词向量
                    indexName = Configuration.ES_INDEX_WDIC;
                    filePath = FileConfig.FILE_WDIC;
                    typeFaq = Configuration.ES_TYPE_WDIC;
                    break;
                case TYPE://实体类型
                    indexName = Configuration.ES_INDEX_TYPE;
                    filePath = FileConfig.FILE_TYPE;
                    typeFaq = Configuration.ES_TYPE_TYPE;
                    break;
                case FreeBase:
                    indexName = Configuration.ES_INDEX_KB;
                    filePath = FileConfig.FILE_KB;
                    typeFaq = Configuration.ES_TYPE_SPO;
                    break;
                default:
                    indexName = Configuration.ES_INDEX_CCKS;
                    filePath = FileConfig.FILE_ENTITY;
                    typeFaq = Configuration.ES_TYPE_ENTITY;
            }

            //索引数据之前首先对es中的索引结构进行初始化
            if(!_init(indexName, typeFaq, filePath)){

                if(typeFaq.equalsIgnoreCase(Configuration.ES_TYPE_ENTITY))
                {
                    TransportClient client = GetClient.getTransportClient();
                    //读文本获取 待索引的数据集
                    parse_text parseText= new parse_text();
                    parseText.read_text(client,indexName,Configuration.ES_TYPE_ENTITY,filePath);
                    List<String> parseResult = parseText.getParseResult();

                    BulkRequestBuilder bulkRequest = client.prepareBulk();
                    for(int i = 0;i < parseResult.size();i++)
                    {
                        String[] split = parseResult.get(i).split("\t");
                        if(split.length==3) {
                            bulkRequest.add(client.prepareIndex(indexName, typeFaq)
                                    .setSource(//这里可以直接使用json字符串
                                            jsonBuilder()
                                                    .startObject()
                                                    .field("name", split[0])
                                                    .field("entity", split[1])
                                                    .field("rank", Integer.parseInt(split[2]))
                                                    .endObject()
                                    )
                            );
                        }

                        //输出计数
                        n = n + 1;
                        if (n % patch == 0) {
                            bulkRequest.execute().actionGet();
                            bulkRequest = client.prepareBulk();
                            System.out.println("提交了" + n + "条");
                        }

                    }
                    //将最后不足整数的request执行
                    bulkRequest.execute().actionGet();

                }
                else if(typeFaq.equalsIgnoreCase(Configuration.ES_TYPE_WDIC)){
                    TransportClient client = GetClient.getTransportClient();
                    //读文本获取 待索引的数据集
                    parse_text parseText= new parse_text();
                    //buck 索引的操作 添加在 read_text中，每读取文件10000条数据 就buck索引一次
                    parseText.read_text(client,indexName,Configuration.ES_TYPE_WDIC,filePath);

                }
                else if(typeFaq.equalsIgnoreCase(Configuration.ES_TYPE_TYPE)){
                    TransportClient client = GetClient.getTransportClient();
                    //读文本获取 待索引的数据集
                    parse_text parseText= new parse_text();
                    //buck 索引的操作 添加在 read_text中，每读取文件10000条数据 就buck索引一次
                    parseText.read_text(client,indexName,Configuration.ES_TYPE_TYPE,filePath);


                }
                else if(typeFaq.equalsIgnoreCase(Configuration.ES_TYPE_SPO)){
                    TransportClient client = GetClient.getTransportClient();
                    //读文本获取 待索引的数据集
                    parse_text parseText= new parse_text();
                    //buck 索引的操作 添加在 read_text中，每读取文件10000条数据 就buck索引一次
                    parseText.read_text(client,indexName,Configuration.ES_TYPE_SPO,filePath);


                }
            }
        }
    }





}
