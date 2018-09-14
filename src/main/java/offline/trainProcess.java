package offline;


import PreProcess.extractEPV;
import PreProcess.getConcept;
import conf.Configuration;
import edu.stanford.nlp.hcoref.Preprocessor;
import edu.stanford.nlp.util.Quadruple;
import tools.StanfordEnglishNER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class trainProcess {


    public static final String filePath = Configuration.QATrainFile;

    public static void train(){

        Map<String,String> QAs = getTrainQAs();
        Set<String> keyset = QAs.keySet();
        for(Iterator<String> it = keyset.iterator();it.hasNext();){
            String question = it.next();
            String answer = QAs.get(question);
            System.out.println(question+" ::  "+answer);

            //对每个问答对进行抽离得到 q e v
            List<Quadruple<String,String,String,String>> QEV = extractEPV.parseQA2qepv(question,answer);
            System.out.println(trainProcess.class+"获得qepv");
            //获取该问题的template
//            getConcept.getEntityConceptByContext(question,entity);

        }

        //需要

    }

    public static void main(String[] args) {
        train();
    }





    public static Map<String,String> getTrainQAs(){

        Map<String,String> QAs = new LinkedHashMap<String, String>();

        try{
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;

            while((line=reader.readLine())!=null){
                // 分解每一行获得问题和答案的map
                String[] splits = line.split("\t");
                QAs.put(splits[0],splits[1]);
            }

        }catch (Exception e){
            System.out.println(e);
        }
        return QAs;
    }







}
