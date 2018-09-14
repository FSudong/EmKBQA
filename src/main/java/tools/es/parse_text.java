package tools.es;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class parse_text {

    private List<String> ListString = new ArrayList<String>();

    public void read_text(TransportClient client, String indexName, String type, String filepath){
//      获取class的目录 进而获得pkubase-mention2ent.txt目录
//        String path = this.getClass().getResource("/").getPath();
//        int end = path.indexOf("/target/classes/");
//        String path_new = path.substring(1,end+1).replace('/','\\');
//        String filePath = path_new+filepath;
        String filePath = filepath;
        try {
//            默认使用UTF-8
            String encoding = "UTF-16";
            if(type.equalsIgnoreCase(Configuration.ES_TYPE_ENTITY)){
                encoding="UTF-8";
            }else if (type.equalsIgnoreCase(Configuration.ES_TYPE_WDIC)){
//                encoding="UTF-16";
                encoding="UTF-8";
            }
            File file = new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                //根据不同数据将其以不同形式索引

                if(type.equalsIgnoreCase(Configuration.ES_TYPE_ENTITY)) {
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        //                    System.out.println(lineTxt);
                        ListString.add(lineTxt);

                    }
                }else if(type.equalsIgnoreCase(Configuration.ES_TYPE_SPO)){
                    //记录捆绑的数目 每达到多少就提交
                    long toBulk = 0,patch = 100000;
                    BulkRequestBuilder bulkRequest = client.prepareBulk();
                    while((lineTxt = bufferedReader.readLine()) != null) {
                        //将词条 与向量分离
                        String[] splits = lineTxt.split("\t");
                        int subject_length = splits[0].length();
                        String subject = splits[0].trim().replace("'","").replace("[","")
                                .replace("]","");
                        String predicate = splits[1].substring(3);
                        String object = splits[2].replace("\n","");
                        bulkRequest.add(client.prepareIndex(indexName, type)
                                .setSource(//这里可以直接使用json字符串
                                        jsonBuilder()
                                                .startObject()
                                                .field("subject", subject)
                                                .field("predicate", predicate)
                                                .field("object", object)
                                                .endObject()
                                )
                        );
                        toBulk++;
                        if (toBulk % patch == 0) {
                            System.out.println(toBulk / patch);
                            bulkRequest.execute().actionGet();
                            bulkRequest = client.prepareBulk();
                            System.out.println("提交了" + toBulk + "条");
                        }
                    }
                    //将最后不足整数的request执行
                    bulkRequest.execute().actionGet();
                }
                else if(type.equalsIgnoreCase(Configuration.ES_TYPE_TYPE)){
//                    //记录捆绑的数目 每达到多少就提交
//                    long toBulk = 0,patch = 20000;
//                    BulkRequestBuilder bulkRequest = client.prepareBulk();
//                    while((lineTxt = bufferedReader.readLine()) != null) {
//                        //将词条 与向量分离
//                        String[] split_result = lineTxt.split(">\t<");
//                        String entityName = split_result[0].replace("<","");
//                        String typeName = split_result[2].replace(">\t.","");
//
//                        bulkRequest.add(client.prepareIndex(indexName, type)
//                                .setSource(//这里可以直接使用json字符串
//                                        jsonBuilder()
//                                                .startObject()
//                                                .field("entityName", entityName)
//                                                .field("typeName", typeName)
//                                                .endObject()
//                                )
//                        );
//                        toBulk++;
//                        if (toBulk % patch == 0) {
//                            System.out.println(toBulk / patch);
//                            bulkRequest.execute().actionGet();
//                            bulkRequest = client.prepareBulk();
//                            System.out.println("提交了" + toBulk + "条");
//                        }
//                    }
//                    //将最后不足整数的request执行
//                    bulkRequest.execute().actionGet();

                    //每从文件中读取 1500w 行时 就进行bulk操作
                    long haveRead = 0, listBulkNum = 10000000;
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        if(haveRead%listBulkNum == 0){
                            System.out.println("数据正在读入内存中...");
                        }
                        ListString.add(lineTxt);
                        haveRead++;
                        if(haveRead%listBulkNum == 0){
                            System.out.println("读入"+haveRead);
                            System.out.println(lineTxt);
                            listTobulk(ListString,client,indexName,type);
                            ListString.clear();
                        }
                    }
                    if(ListString!=null && ListString.size()!=0){
                        listTobulk(ListString,client,indexName,type);
                        ListString.clear();
                    }
                }
                read.close();

            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }

    }
    public List<String> getParseResult(){
        return ListString;
    }

    public void listTobulk(List<String> list ,TransportClient client, String indexName, String type) throws IOException {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        long toBulk = 0,patch_num = 20000;
        for(int i = 0;i < list.size();i++)
        {
            String[] split_result = list.get(i).split(">\t<");
            String entityName = split_result[0].replace("<","");
            String typeName = split_result[2].replace("> .","");

            bulkRequest.add(client.prepareIndex(indexName, type)
                    .setSource(//这里可以直接使用json字符串
                            jsonBuilder()
                                    .startObject()
                                    .field("entityName", entityName)
                                    .field("typeName", typeName)
                                    .endObject()
                    )
            );

            //输出计数
            toBulk++;
            if (toBulk % patch_num == 0) {
                System.out.println(toBulk / patch_num);
                bulkRequest.execute().actionGet();
                bulkRequest = client.prepareBulk();
                System.out.println("提交了" + toBulk + "条");
            }

        }
        //将最后不足整数的request执行
        if(toBulk%patch_num !=0){
            bulkRequest.execute().actionGet();
        }

    }



}
