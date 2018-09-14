package PreProcess;

import jdk.internal.dynalink.beans.StaticClass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class getConcept {



    public static Map<String,Double> getEntityConceptByContext(String question, String entity){
        //利用上下文生成concept
        return getEntityConceptDirect(entity);
    }


    public static Map<String,Double> getEntityConceptDirect(String entity){
        String entityInUrl = entity.replace(" ","%20");
        String urlString = "https://concept.research.microsoft.com/api/Concept/ScoreByProb?instance="+entityInUrl+"&topK=10";
        String jsonCandidateConcepts = "";
        try {

            URL url = new URL(urlString);
            InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is,"utf-8");

            //为字符输入流添加缓冲
            BufferedReader br = new BufferedReader(isr);
            String data = br.readLine();//读取数据

            while (data!=null){//循环读取数据
                System.out.println(data);//输出数据
                jsonCandidateConcepts += data;
                data = br.readLine();

            }
            br.close();
            isr.close();
            is.close();

        }catch (Exception e){
            System.out.println(e);
        }

        Map<String,Double> conceptAndScore  = parseJsonCandidateConcept(jsonCandidateConcepts);
        return conceptAndScore;
    }



    public static Map<String,Double> parseJsonCandidateConcept(String json){

        Map<String,Double> conceptScore = new LinkedHashMap<String, Double>();

        if(json!=null && !json.equalsIgnoreCase("")){
            json = json.trim().replace("{","").replace("}","");
            String[] all_conceptScore = json.split(",");
            for (String cs: all_conceptScore){
                String[] splits = cs.split(":");
                String conceptName = splits[0].replace("\"","");
                Double score = Double.parseDouble(splits[1]);
                conceptScore.put(conceptName, score);
            }
        }

        return conceptScore;
    }


}
