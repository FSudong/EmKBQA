package tools;


import org.json.JSONObject;
import tools.es.Configuration;
import tools.es.SearchFAQ;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static tools.es.SearchFAQ.match_bool_fields;


public class KnowledgeBase {

    //需要在斯坦福命名识别器找到实体后 在判断是否知识图谱中 存在该实体
    public static Boolean findEntity(String entity){

        try {
            String[] terms = {entity};
            String[] fields = {"subject","object"};
            List<String> findResults = match_bool_fields(Configuration.ES_INDEX_KB,terms,fields);

            if(findResults!=null && findResults.size()!=0){
                System.out.println("["+KnowledgeBase.class+"]"+"实体："+entity+"存在"+findResults.size());
                return true;
            }else {
                System.out.println("["+KnowledgeBase.class+"]"+"实体："+entity+"不存在");
                return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return true;
    }

    //找到了 qa  中的 e v  后，再寻找中间的谓词。最后判断谓词是否与问题的类型一致
    public static List<String> getPredicateByEV(String entity,String value){
        List<String> predicates = new ArrayList<String>();
        try {

            String[] terms = {entity, value};
            String[] fields = {"subject","object"};
            List<String> list_strings= match_bool_fields(Configuration.ES_INDEX_KB, terms,fields);
            if(list_strings==null){
                return null;
            }
            //若不为空，但只有一条数据
            if(list_strings.size()==1){
                JSONObject json = new JSONObject(list_strings.get(0));
                predicates.add((String) json.get("predicate"));
                return predicates;
            }else{
                //有多条信息
                for(String s:list_strings){
                    JSONObject json = new JSONObject(s);
                    predicates.add((String) json.get("predicate"));
                }
                return predicates;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return predicates;
    }

    public static String getAllPreciateByEntity(String entity){
        return "";
    }
}
