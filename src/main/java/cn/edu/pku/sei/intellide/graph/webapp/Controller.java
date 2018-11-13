package cn.edu.pku.sei.intellide.graph.webapp;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.qa.doc_search.DocSearch;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine;
import cn.edu.pku.sei.intellide.graph.webapp.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.JSONException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.edu.pku.sei.intellide.graph.webapp.entity.SnowGraphProject.getProjectList;

@CrossOrigin
@RestController
@Slf4j
public class Controller {

    Map<String, CodeSearch> codeSearchMap = new LinkedHashMap<>();
    Map<String, DocSearch> docSearchMap = new LinkedHashMap<>();
    Map<String, NavResult> navResultMap = new LinkedHashMap<>();
    Map<String, GraphDatabaseService> dbMap = new LinkedHashMap<>();
    CodeSearch codeSearch = null;
    @Autowired
    private Context context;

    @RequestMapping(value = "/projects", method = {RequestMethod.GET, RequestMethod.POST})
    synchronized public List<SnowGraphProject> getProjects() throws IOException, JSONException {
        List<SnowGraphProject> projects;
        projects = getProjectList(context.infoDir);
        return projects;
    }


    @RequestMapping(value = "/codeSearch", method = {RequestMethod.GET, RequestMethod.POST})
    synchronized public Neo4jSubGraph codeSearch(String query, String project) {

        log.info("==================================================");
        log.info("启动代码搜索，query: " + query);

        String languageIdentifier = "english";
        if (project.contains("chinese")) {
            languageIdentifier = "chinese";
        }

        NLQueryEngine nlQueryEngine = new NLQueryEngine(getDb(project), context.dataDir + '/' + project, languageIdentifier);
        Neo4jSubGraph r = nlQueryEngine.search(query);

        if (r.getNodes().size() > 0) {
            return r;
        }

        if (!codeSearchMap.containsKey(project)) {
            codeSearchMap.put(project, new CodeSearch(getDb(project), languageIdentifier));
        }

        CodeSearch codeSearch = codeSearchMap.get(project);
        return codeSearch.search(query);
    }

    @RequestMapping(value = "/docSearch", method = {RequestMethod.GET, RequestMethod.POST})
    synchronized public List<Neo4jNode> docSearch(String query, String project) throws IOException, ParseException {

        String languageIdentifier = "english";
        if (project.contains("chinese")) {
            languageIdentifier = "chinese";
        }

        if (!docSearchMap.containsKey(project)) {
            codeSearchMap.put(project, new CodeSearch(getDb(project), languageIdentifier));
            codeSearch = codeSearchMap.get(project);
            docSearchMap.put(project, new DocSearch(getDb(project), context.dataDir + '/' + project + "/doc_search_index", codeSearch));
        }
        DocSearch docSearch = docSearchMap.get(project);
        return docSearch.search(query, project);
    }

    @RequestMapping(value = "/nav", method = {RequestMethod.GET, RequestMethod.POST})
    synchronized public NavResult nav(String project) {
        if (!navResultMap.containsKey(project)) {
            navResultMap.put(project, NavResult.fetch(getDb(project)));
        }
        NavResult navResult = navResultMap.get(project);
        return navResult;
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET, RequestMethod.POST})
    synchronized public List<Neo4jRelation> relationList(long id, String project) {
        return Neo4jRelation.getNeo4jRelationList(id, getDb(project));
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET, RequestMethod.POST})
    synchronized public Neo4jNode node(long id, String project) {
        return Neo4jNode.get(id, getDb(project));
    }

    private GraphDatabaseService getDb(String project) {
        if (!dbMap.containsKey(project)) {
            GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(context.graphDir + '/' + project));
            dbMap.put(project, db);
        }
        return dbMap.get(project);
    }

}

@Component
class Context {
    String graphDir = null;
    String dataDir = null;
    String infoDir = null;

    @Autowired
    public Context(Conf conf) {
        this.graphDir = conf.getGraphDir();
        this.dataDir = conf.getDataDir();
        this.infoDir = conf.getInfoDir();
    }

}