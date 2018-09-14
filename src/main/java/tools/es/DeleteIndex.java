package tools.es;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.net.UnknownHostException;
import java.sql.SQLOutput;

public class DeleteIndex {
    public static void deleteIndex(String indexName) throws UnknownHostException {
        TransportClient client = GetClient.getTransportClient();
        //启动系统的时候判断索引是否存在
        IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);
        //如果已经存在的话就把索引删除，然后重新创建索引（待完善）
        IndicesExistsResponse indicesExistsResponse = client.admin().indices().exists(inExistsRequest).actionGet();
        System.out.println(indicesExistsResponse.isExists());
        if (indicesExistsResponse.isExists()) {
            DeleteIndexResponse deleteIndexResponse = client.admin().indices().prepareDelete(indexName).execute().actionGet();
            System.out.println(deleteIndexResponse.isAcknowledged());
        }
    }

    public static void deleteType(String indexName, String type) throws UnknownHostException {
        TransportClient client = GetClient.getTransportClient();
        //启动系统的时候判断索引是否存在
        IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);
        //如果已经存在的话就把索引删除
        IndicesExistsResponse indicesExistsResponse = client.admin().indices().exists(inExistsRequest).actionGet();
        System.out.println(indicesExistsResponse.isExists());
        if (indicesExistsResponse.isExists()) {
//            TypesExistsResponse typesExistsResponse = client.admin().indices().prepareTypesExists(type).get();
            String[] indexNames = {indexName};
            TypesExistsRequest typesExistsRequest = new TypesExistsRequest(indexNames,type);
            TypesExistsResponse typesExistsResponse = client.admin().indices().typesExists(typesExistsRequest).actionGet();
            if(typesExistsResponse.isExists()){

//                DeleteResponse response= client.prepareDelete().setType("").get();

                DeleteResponse response = client.prepareDelete().setIndex(indexName).setType("").setId("").get();
                System.out.println(response.status());
//                System.out.println(deleteIndexTemplateResponse.isAcknowledged());
            }
        }
    }

    public static void deleteIndexW2V() throws UnknownHostException {
        DeleteIndex.deleteIndex(Configuration.ES_INDEX_WDIC);
    }


}
