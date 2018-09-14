package tools.es;



public class Configuration {

    /* ElasticSearch相关设置*/

    //集群名称
    public static final String ES_CLUSTER_NAME = "elasticsearch";
    //集群地址: 默认本地地址
    public static final String ES_HOST = "127.0.0.1";


    //索引名称: :词条 实体 排名-FAQ
    public static final String ES_INDEX_CCKS = "qataskccks-test";
    //类型名称：互联网获取的常用问答对
    public static final String ES_TYPE_ENTITY = "entitys";


    //索引名称:词条 向量-WDIC
    public static final String ES_INDEX_WDIC = "wordvector";
    //类型名称：互联网获取的常用问答对
    public static final String ES_TYPE_WDIC = "dicts";


    //索引名称:
    public static final String ES_INDEX_TYPE = "entitytype";
    //类型名称：互联网获取的常用问答对
    public static final String ES_TYPE_TYPE = "types";

    //索引名称:知识图谱 FB5M
    public static final String ES_INDEX_KB = "freebase";
    //类型名称：
    public static final String ES_TYPE_SPO = "spo";
}
