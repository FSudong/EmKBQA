package tools;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import PreProcess.*;
/**
 * Created by KrseLee on 2016/11/5.
 */
public class StanfordEnglishNER {

    /**
     * create by: fsd
     * description:由文本  寻找到其中实体  由list存储
     * create time: 20:23 2018/9/6
     */
    public static  List<String> getEntitys(String text){
        // 存储实体
        List<String> entitys  = new ArrayList<String>();

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // create an empty Annotation just with the given text q
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        //
        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            int entityLength = 0;
            String  entityTerm = "";
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                System.out.println("["+StanfordEnglishNER.class+"]"+"ne: "+ne+" word: "+word+" pos: "+pos);
                if(!ne.toString().equalsIgnoreCase("O")){
                    entityLength += 1;
                    if(entityTerm!=null && entityLength > 1){
                        entityTerm += " ";
                    }
                    entityTerm += word;
                    System.out.println("["+StanfordEnglishNER.class+"]"+"saved ne: "+ne+" word: "+word+" pos: "+pos);
                }else if(!entityTerm.equalsIgnoreCase("")){
                    entitys.add(entityTerm);
                    entityTerm = "";
                    entityLength = 0;
                }

            }
            if(entityLength > 0 && !entityTerm.equalsIgnoreCase("")){
                entitys.add(entityTerm);
            }
        }
        for (String entity:entitys){
            System.out.println("["+StanfordEnglishNER.class+"]"+entity);
        }
        return entitys;
    }




}
