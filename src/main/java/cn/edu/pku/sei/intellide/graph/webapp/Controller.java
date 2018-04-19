package cn.edu.pku.sei.intellide.graph.webapp;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.qa.doc_search.DocSearch;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine;
import cn.edu.pku.sei.intellide.graph.webapp.entity.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.neo4j.cypher.export.SubGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
public class Controller {

    @Autowired
    private Context context;

    CodeSearch codeSearch=null;
    DocSearch docSearch=null;
    NLQueryEngine nlQueryEngine=null;
    NavResult navResult=null;

    @PostConstruct
    public void init() {
        nlQueryEngine=new NLQueryEngine(context.db,context.dataDir);
        nlQueryEngine.createIndex();
        codeSearch=new CodeSearch(context.db);
        codeSearch("");
        docSearch=new DocSearch(context.db,context.dataDir+"/doc_search_index", codeSearch);
        navResult=NavResult.fetch(context.db);
    }

    @RequestMapping(value = "/codeSearch", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public Neo4jSubGraph codeSearch(@RequestParam(value="query", defaultValue="") String query){
        Neo4jSubGraph r=nlQueryEngine.search(query);
        if (r.getNodes().size()>0)
            return r;
        return codeSearch.search(query);
    }

    @RequestMapping(value = "/docSearch", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<Neo4jNode> docSearch(@RequestParam(value="query", defaultValue="") String query) throws IOException, ParseException {
        return docSearch.search(query);
    }

    @RequestMapping(value = "/nav", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public NavResult nav() {
        return navResult;
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public List<Neo4jRelation> relationList(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jRelation.getNeo4jRelationList(id,context.db);
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public Neo4jNode node(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jNode.get(id,context.db);
    }

}

@Component
class Context{

    GraphDatabaseService db=null;
    String dataDir=null;

    @Autowired
    public Context(Conf conf){
        this.dataDir=conf.getDataDir();
        this.db= new GraphDatabaseFactory().newEmbeddedDatabase(new File(conf.getGraphDir()));
    }

}