package PreProcess;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Quadruple;
import edu.stanford.nlp.util.Triple;
import org.apache.jena.atlas.lib.Tuple;
import org.ejml.alg.dense.decomposition.qr.QrUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.Fun;
import tools.KnowledgeBase;
import tools.StanfordEnglishNER;

import javax.xml.transform.sax.SAXTransformerFactory;
import java.util.*;


public class extractEPV {


    /**
     * create by: fsd
     * description: 将训练集中的问答对转换为 (E,P,V)三元组
     * create time: 19:32 2018/9/5
     */
    public static List<Quadruple<String,String,String,String>> parseQA2qepv(String question,String answer){
        List<Quadruple<String,String,String,String>> QEPVs = new ArrayList<Quadruple<String, String, String, String>>();
        List<String> questionEntity = extractQuestionEntity(question);
        List<String> answerValue = getAnswerValues(answer);
        Map<String,String> entityAndValue = new LinkedHashMap<String, String>();
        if(questionEntity==null||answerValue==null||questionEntity.size()==0||answerValue.size()==0){
            System.out.println("该question或者answer中无实体");
            return null;
        }

        for(String e:questionEntity){
            for(String v:answerValue){
                List<String> predicates = KnowledgeBase.getPredicateByEV(e,v);
                //首先，需要存在连接两者的谓词
                if(predicates!=null){

                    for(String predicate:predicates){
                        if(predicate!=null && !predicate.equals("")){
                            //随后，谓词类型与问题类型 需要一致
                            if(QPredicateTypeEqual(question,predicate) ){
                                //保存使得问题与谓词类型匹配的 所有 e p v
                                System.out.println(e+"-->>"+v+"-->>"+predicates.size());
                                Quadruple<String,String,String,String> qepv =
                                        new Quadruple(question,e,predicate,v);
                                if(!QEPVs.contains(qepv)){QEPVs.add(qepv);}
                                entityAndValue.put(e,v);
                            }
                        }
                    }
                }


            }
        }
//        返回qev
//        Map<String,Map<String,String>> QEV = new HashMap<String, Map<String, String>>();
//        QEV.put(question,entityAndValue);
//        return QEV;

//        返回qepv
        return QEPVs;
    }


    public static void getConstantProbability(List<Quadruple<String,String,String,String>> QEPVs){
        if(QEPVs==null || QEPVs.size()==0){
            System.out.println("该问答对无qepv记录");
        }

        //存储该问题最终有多少实体 用于计算P(e|q)
        Map<String,List<String>> qAllEntity = new HashMap<String, List<String>>();
        //存储P(c|q,e)
        Map<Pair<String,String>,Map<String,Double>> contextConcept = new HashMap<Pair<String, String>, Map<String, Double>>();
        //存储 某个实体在该问题中 可以有多少谓词 q e p用于P(p|t)初始化
        Map<Pair<String,String>,List<String>> predicateNum = new HashMap<Pair<String, String>, List<String>>();
        //存储有多少个values 用于计算P(v|p,e)
        Map<Triple<String,String,String>,List<String>> QEP = new HashMap<Triple<String, String, String>, List<String>>();
        for(Quadruple<String,String,String,String> qepv:QEPVs){
            //设置实体数目
            if(!qAllEntity.containsKey(qepv.first)){
                List<String> listEntity = new ArrayList<String>();
                listEntity.add(qepv.second);
                qAllEntity.put(qepv.first,listEntity);
            }else {
                List<String> listEntity = qAllEntity.get(qepv.first);
                if(!listEntity.contains(qepv.second)){
                    listEntity.add(qepv.second);
                    qAllEntity.put(qepv.first,listEntity);
                }
            }
            //设置

            Pair<String,String> pairc = new Pair<String, String>(qepv.first,qepv.second);
            if(!contextConcept.containsKey(pairc)){
                Map<String,Double> conceptAndScore = getConcept.getEntityConceptDirect(qepv.second);
                contextConcept.put(pairc,conceptAndScore);
            }

            //设置P(p|t)
            Pair<String,String> pair = new Pair<String, String>(qepv.first,qepv.second);
            if(!predicateNum.containsKey(pair)){
                List<String> listPredicate = new ArrayList<String>();
                listPredicate.add(qepv.third);
                predicateNum.put(pair,listPredicate);
            }else {
                List<String> listPredicate = predicateNum.get(pair);
                if(!listPredicate.contains(qepv.third)){
                    listPredicate.add(qepv.third);
                    predicateNum.put(pair,listPredicate);
                }
            }
            //设置P(v|q,e)
            Triple<String,String,String> triple = new Triple<String, String, String>(qepv.first,qepv.second,qepv.third);
            if(!QEP.containsKey(triple)){
                List<String> listValue = new ArrayList<String>();
                listValue.add(qepv.fourth);
                QEP.put(triple,listValue);
            }else {
                List<String> listValue= QEP.get(triple);
                if(!listValue.contains(qepv.fourth)){
                    listValue.add(qepv.fourth);
                    QEP.put(triple,listValue);
                }
            }
        }



    }


    public static Boolean QPredicateTypeEqual(String question,String predicate){
        String queType = getQuestionType(question);
        String preType = getPredicateType(predicate);
        if (queType.equalsIgnoreCase(preType)){
            return true;
        }else {
            return false;
        }
    }


    /**
     * create by: fsd
     * description: 返回问题的候选实体
     * create time: 19:25 2018/9/5
     */
    public static List<String> extractQuestionEntity(String question){
        List<String> entitys = StanfordEnglishNER.getEntitys(question);
        //验证知识图谱中是否存在该实体，以进行过滤
        if(entitys==null||entitys.size()==0){
            System.out.println("斯坦福ner未发现"+question+"中的实体");
            return null;
        }
        for (String e:entitys){
            if(!KnowledgeBase.findEntity(e)){
                entitys.remove(e);
            }
        }
        return entitys;
    }


    public static List<String> getAnswerValues(String answer){
        List<String> values = StanfordEnglishNER.getEntitys(answer);
        if(values==null||values.size()==0){
            System.out.println("斯坦福ner未发现"+answer+"中的实体");
            return null;
        }
        //验证知识图谱中是否存在该实体，以进行过滤
        for (String v:values){
            if(!KnowledgeBase.findEntity(v)){
                values.remove(v);
            }
        }
        return values;
    }



    /**
     * create by: fsd
     * description: 返回问题的类型
     *
     * create time: 19:18 2018/9/5
     */
    public static String getQuestionType(String question){
        String questionType = "";
        return "";
    }

    public static String getPredicateType(String predicate){
        //从知识图谱中 选取连接实体和答案的 谓词
        //得到谓词类型
        String preType = "";
        return "";
    }

}
