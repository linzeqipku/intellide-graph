package cn.edu.pku.sei.intellide.graph.webapp;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.qa.doc_search.DocSearch;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine_en;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.config.Config;
import cn.edu.pku.sei.intellide.graph.webapp.entity.*;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.csv.reader.SourceTraceability;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.edu.pku.sei.intellide.graph.webapp.entity.SnowGraphProject.getDbMap;
import static cn.edu.pku.sei.intellide.graph.webapp.entity.SnowGraphProject.getProjectList;

@CrossOrigin
@RestController
public class Controller {

    @Autowired
    private Context context;

    Map<String,CodeSearch> codeSearchMap = new HashMap<>();
    Map<String,DocSearch> docSearchMap = new HashMap<>();
    //Map<String,NLQueryEngine> nlQueryEngineMap = new HashMap<>();
    Map<String,NavResult> navResultMap = new HashMap<>();
    Map<String,GraphDatabaseService> dbMap = new HashMap<>();

    CodeSearch codeSearch=null;
   /* DocSearch docSearch=null;
    NLQueryEngine nlQueryEngine=null;
    NavResult navResult=null;*/

    @PostConstruct
    public void init() throws IOException, JSONException {
        dbMap = getDbMap(context.graphDir);
        //System.out.println(dbMap.size());
        //dbMap = context.dbMap;
        for (Map.Entry<String, GraphDatabaseService> entry : dbMap.entrySet()) {
            //nlQueryEngineMap.put(entry.getKey(),new NLQueryEngine(entry.getValue(),context.dataDir));
            codeSearchMap.put(entry.getKey(),new CodeSearch(entry.getValue()));
            codeSearch = codeSearchMap.get(entry.getKey());
            //codeSearch("list all class call a method",entry.getKey());
            docSearchMap.put(entry.getKey(),new DocSearch(entry.getValue(),context.dataDir+entry.getKey()+"/doc_search_index",codeSearch));
            navResultMap.put(entry.getKey(),NavResult.fetch(entry.getValue()));

        }
        //System.out.println(navResultMap.size());
        /*nlQueryEngine=new NLQueryEngine(context.db,context.dataDir);
        nlQueryEngine.createIndex();
        codeSearch=new CodeSearch(context.db);
        codeSearch("", "lucene");
        docSearch=new DocSearch(context.db,context.dataDir+"/doc_search_index", codeSearch);
        navResult=NavResult.fetch(context.db);*/
    }

    @RequestMapping(value = "/projects", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<SnowGraphProject> getProjects() throws IOException, JSONException {
        List<SnowGraphProject> projects ;
        //projects.add(new SnowGraphProject())
        projects = getProjectList();
        return projects;
    }


    @RequestMapping(value = "/codeSearch", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public Neo4jSubGraph codeSearch(String query, String project){
        //System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxx" + query + " " + project);
        if(project.contains("chinese")){
            NLQueryEngine nlQueryEngine = new NLQueryEngine(dbMap.get(project),context.dataDir+project);
            Neo4jSubGraph r=nlQueryEngine.search(query);
            //System.out.println("success 11");
            if (r.getNodes().size()>0)
                System.out.println("success");
                return r;
        }
        else{

            NLQueryEngine_en nlQueryEngine = new NLQueryEngine_en(dbMap.get(project),context.dataDir+project);
            Neo4jSubGraph r=nlQueryEngine.search(query);
            if (r.getNodes().size()>0){
                //System.out.println("success");
                return r;
            }

        }
        CodeSearch codeSearch = codeSearchMap.get(project);
        return codeSearch.search(query);
    }

    @RequestMapping(value = "/docSearch", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<Neo4jNode> docSearch(String query, String project) throws IOException, ParseException {

        DocSearch docSearch = docSearchMap.get(project);
        //System.out.println(docSearch.search(query));
        return docSearch.search(query,project);
    }

    @RequestMapping(value = "/nav", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public NavResult nav(String project) {
        //System.out.println(project);
        NavResult navResult = navResultMap.get(project);
        return navResult;
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<Neo4jRelation> relationList(long id, String project){

        return Neo4jRelation.getNeo4jRelationList(id,dbMap.get(project));
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public Neo4jNode node(long id, String project){
        return Neo4jNode.get(id,dbMap.get(project));
    }

}

@Component
class Context{

    //Map<String,GraphDatabaseService> dbMap;
    String graphDir = null;
    String dataDir = null;

    @Autowired
    public Context(Conf conf){
        this.dataDir=conf.getDataDir();
        this.graphDir = conf.getGraphDir();
    }

}