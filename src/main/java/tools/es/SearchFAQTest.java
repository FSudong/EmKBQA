//package tools.es;
//
//import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
//import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
//import org.elasticsearch.client.transport.TransportClient;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static utils.es.Configuration.ES_INDEX_CCKS;
//
//class SearchFAQTest {
//    @Test
//    void indexFaqData() {
//
//        try {
//            SearchFAQ.indexFaqData();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Establishing ES failed!");
//        }
//
//    }
//
//    @Test
//    void matchPhraseQuery_first() throws UnknownHostException {
////        String[] term = {"纸飞机","房子","天空","apple"};
//        String[] term = {"纸飞"};
//        int j = 0;
//        while(j<term.length) {
////            Scanner sc = new Scanner(System.in);
////            System.out.print("ScannerTest, Please Enter Name:");
////            String term = sc.nextLine();
//
//            List<String> str = new ArrayList<>();
//            String indexname = ES_INDEX_CCKS;
//            //模糊搜索
//            System.out.println("term："+term[j]);
//            str = SearchFAQ.matchPhraseQuery_first("name", term[j++], indexname);
//            int i = 0;
//            for (i = 0; i < str.size(); i++) {
//                System.out.println(str.get(i)+"\r\n"+str.get(i).split("\"")[3].trim());
//            }
//            System.out.println("共计"+i+"条");
//        }
//    }
//
//    @Test
//    void accurateQuery_second() throws UnknownHostException {
////        String[] term = {"纸飞机","房子","天空","apple"};
//        String[] term = {"纸飞机"};
//        int j = 0;
//        while(j<term.length) {
////            Scanner sc = new Scanner(System.in);
////            System.out.print("ScannerTest, Please Enter Name:");
////            String term = sc.nextLine();
//
//            List<String> str = new ArrayList<>();
//            String indexname = ES_INDEX_CCKS;
//            //精确搜索
//            System.out.println("term："+term[j]);
//            str = SearchFAQ.accurateQuery_second("name", term[j++], indexname);
//            int i = 0;
//            for (i = 0; i < str.size(); i++) {
//                System.out.println(str.get(i)+"\r\n"+str.get(i).split("\"")[7].trim());
//            }
//            System.out.println("共计"+i+"条");
//        }
////            str = accurateQuery_second("name", term, indexname);
//    }
//
//    @Test
//    void deleteIndex() throws UnknownHostException {
//        DeleteIndex.deleteIndex(ES_INDEX_CCKS);
//    }
//
//    @Test
//    void deleteIndexW2V() throws UnknownHostException {
//        DeleteIndex.deleteIndexW2V();
//    }
//
//    @Test
//    void deleteType() throws UnknownHostException {
//        DeleteIndex.deleteType("test_index","entitys");
//    }
//
//    @Test
//    void termVector() throws UnknownHostException {
//        try {
//            SearchFAQ.termVector_Test();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    void termVector_Test() {
//
//        try {
//            SearchFAQ.termVector_Test();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//}