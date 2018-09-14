import PreProcess.getConcept;
import tools.StanfordEnglishNER;


import java.util.List;
import java.util.Map;

public class testStream {


    public static void main(String[] args) {

//        example.runAllAnnotators();
        List<String> entitys = StanfordEnglishNER.getEntitys("what is don graham known as ?\tamerican football player\n");

        Map<String,Double> candidateConcepts = getConcept.getEntityConceptDirect(entitys.get(0));
        System.out.println(candidateConcepts.toString());
    }


}
