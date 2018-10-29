package cn.edu.pku.sei.intellide.graph.webapp;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.qa.doc_search.DocSearch;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine_en;
import cn.edu.pku.sei.intellide.graph.webapp.entity.*;
import lombok.Getter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.JSONException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static cn.edu.pku.sei.intellide.graph.webapp.entity.SnowGraphProject.getDbMap;
import static cn.edu.pku.sei.intellide.graph.webapp.entity.SnowGraphProject.getProjectList;
import static cn.edu.pku.sei.intellide.graph.webapp.entity.SnowGraphProject.isNlpSolver;

@CrossOrigin
@RestController
public class Controller {

    @Autowired
    private Context Context;

    Map<String,CodeSearch> codeSearchMap = new LinkedHashMap<>();
    Map<String,DocSearch> docSearchMap = new LinkedHashMap<>();
    Map<String,NLQueryEngine_en> nlQueryEngineMap = new LinkedHashMap<>();
    Map<String,NavResult> navResultMap = new LinkedHashMap<>();
    Map<String,GraphDatabaseService> dbMap = new LinkedHashMap<>();
    CodeSearch codeSearch=null;

    @PostConstruct
    public void init() throws IOException, JSONException {

        dbMap = getDbMap(Context.graphDir, Context.infoDir);
    }

    @RequestMapping(value = "/projects", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<SnowGraphProject> getProjects() throws IOException, JSONException {
        List<SnowGraphProject> projects ;
        //projects.add(new SnowGraphProject())
        projects = getProjectList(Context.infoDir);

        return projects;
    }


    @RequestMapping(value = "/codeSearch", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public Neo4jSubGraph codeSearch(String query, String project){

        if(project.contains("chinese")){
            NLQueryEngine nlQueryEngine = new NLQueryEngine(dbMap.get(project), Context.dataDir+'/'+project);
            nlQueryEngine.createIndex();
            Neo4jSubGraph r=nlQueryEngine.search(query);

            if (r.getNodes().size()>0) {
                System.out.println("success");
                System.out.println(r.getNodes().toString());
                return r;
            }
        }
        if(isNlpSolver(query)){
            if(!project.contains("chinese")){
                if(!nlQueryEngineMap.containsKey(project)){
                    NLQueryEngine_en nlQueryEngine = new NLQueryEngine_en(dbMap.get(project), Context.dataDir+'/'+project);
                    nlQueryEngine.createIndex();
                }
                NLQueryEngine_en nlQueryEngine = nlQueryEngineMap.get(project);
                Neo4jSubGraph r=nlQueryEngine.search(query);
                System.out.println(r.getNodes().size());
                if (r.getNodes().size()>0){
                    System.out.println("success ni");
                    return r;
                }
            }
        }

        if(!dbMap.containsKey(project)) {
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(Context.graphDir+'/'+project));
            dbMap.put(project,db);
        }
        if(!codeSearchMap.containsKey(project)){
            codeSearchMap.put(project,new CodeSearch(dbMap.get(project)));

        }
        CodeSearch codeSearch = codeSearchMap.get(project);
        System.out.println("开始CodeSearch...");
        return codeSearch.search(query);
    }

    @RequestMapping(value = "/docSearch", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<Neo4jNode> docSearch(String query, String project) throws IOException, ParseException {
        if(!dbMap.containsKey(project)) {
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(Context.graphDir+'/'+project));
            dbMap.put(project,db);
        }
        if(!docSearchMap.containsKey(project)){
            codeSearchMap.put(project,new CodeSearch(dbMap.get(project)));
            codeSearch = codeSearchMap.get(project);
            docSearchMap.put(project,new DocSearch(dbMap.get(project), Context.dataDir+'/'+project+"/doc_search_index",codeSearch));
        }

        DocSearch docSearch = docSearchMap.get(project);
        //System.out.println(docSearch.search(query,project).toString());
        return docSearch.search(query,project);
    }

    @RequestMapping(value = "/nav", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public NavResult nav(String project) {
        //System.out.println(project);
        if(!dbMap.containsKey(project)) {
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(Context.graphDir+'/'+project));
            dbMap.put(project,db);
        }
        if(!navResultMap.containsKey(project)){
            navResultMap.put(project,NavResult.fetch(dbMap.get(project)));
        }
        //System.out.println();
        NavResult navResult = navResultMap.get(project);
        //System.out.println(navResult.toString());
        return navResult;
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<Neo4jRelation> relationList(long id, String project){
        if(!dbMap.containsKey(project)){
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(Context.graphDir+'/'+project));
            dbMap.put(project,db);
        }
        return Neo4jRelation.getNeo4jRelationList(id,dbMap.get(project));
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public Neo4jNode node(long id, String project){
        if(!dbMap.containsKey(project)){
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(Context.graphDir+'/'+project));
            dbMap.put(project,db);
        }
        return Neo4jNode.get(id,dbMap.get(project));
    }

}

@Component
class Context {
    String graphDir = null;
    String dataDir = null;
    String infoDir =null;

    @Autowired
    public Context(Conf conf){
        this.graphDir = conf.getGraphDir();
        this.dataDir = conf.getDataDir();
        this.infoDir = conf.getInfoDir();
    }

}