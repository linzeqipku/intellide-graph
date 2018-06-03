package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StanfordParser {
    public static StanfordParser single = null;
    public Properties props = null;
    public StanfordCoreNLP pipeline = null;
    public static StanfordParser getSingle(){
        if (single != null) return single;
        single = new StanfordParser();
        return single;
    }
    public StanfordParser(){
//        props = new Properties();
//        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//        props.put("parse.model","edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
       // props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
        //pipeline = new StanfordCoreNLP(props);
         pipeline = new StanfordCoreNLP("StanfordCoreNLP-chinese.properties");
    }
    public List<NLPToken> runAllAnnotators(String text){

        // create an empty Annotation just with the given text
        if (!(text.endsWith(".")||text.endsWith("?"))) text = text+".";
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);
        return parserOutput(document);
    }

    public List<NLPToken> parserOutput(Annotation document){
        List<NLPToken> set = new ArrayList<>();
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        long offset = 0;
        String name = "";
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if (ne.equals("PERSON")){
                    if (!name.equals("")) name += " ";
                    name += word;
                    continue;
                }
                if (!name.equals("")){
                    NLPToken nlptoken = new NLPToken(name,"NN","PERSON");
                    nlptoken.offset = offset++;
                    set.add(nlptoken);
                    name = "";
                }
                NLPToken nlptoken = new NLPToken(word,pos,ne);
                //System.out.println(pos +" " + word);
                nlptoken.offset = offset++;
                set.add(nlptoken);
            }
        }
        if (!name.equals("")){
            NLPToken nlptoken = new NLPToken(name,"NN","PERSON");
            nlptoken.offset = offset++;
            set.add(nlptoken);
        }
        return set;
    }
    public static void main(String[] args){
        String text = "它被打死了。";
        StanfordParser p = new StanfordParser();
        p.runAllAnnotators(text);
    }
}