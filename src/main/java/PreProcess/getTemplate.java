package PreProcess;

import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class getTemplate {

    public static Map<Pair<String,String>,List<String>> questionTemplates(Map<Pair<String,String>,Map<String,Double>> contextConcept,
                                                                                Map<Pair<String,String>,List<String>> predicateNum){

        Map<Pair<String,String>,List<String>> qeTemplate = new HashMap<Pair<String, String>, List<String>>();
        Map<String,Double> templeScore = new HashMap<String, Double>();
        for(Pair<String,String> qe : contextConcept.keySet()){
            Map<String,Double> concepts = contextConcept.get(qe);
            List<String> templates = new ArrayList<String>();
            for(String s:concepts.keySet()){
                String template = qe.first.replace(qe.second,"$$"+s);

                templates.add(template);

            }
            qeTemplate.put(qe,templates);

        }
        return qeTemplate;
    }

}
