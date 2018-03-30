package cn.edu.pku.sei.intellide.graph.webapp;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.qa.doc_search.DocSearch;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Conf;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jNode;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jRelation;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class Controller {

    @Autowired
    private Context context;

    CodeSearch codeSearch=null;
    DocSearch docSearch=null;
    NLQueryEngine nlQueryEngine=null;

    @RequestMapping(value = "/codeSearch", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jSubGraph codeSearch(@RequestParam(value="query", defaultValue="") String query){
        if (codeSearch==null)
            codeSearch=new CodeSearch(context.driver);
        return codeSearch.search(query);
    }

    @RequestMapping(value = "/docSearch", method = {RequestMethod.GET,RequestMethod.POST})
    public List<Neo4jNode> docSearch(@RequestParam(value="query", defaultValue="") String query){
        if (docSearch==null)
            docSearch=new DocSearch(context.driver);
        return docSearch.search(query);
    }

    @RequestMapping(value = "/nlQuery", method = {RequestMethod.GET,RequestMethod.POST})
    public List<Neo4jNode> nlQuery(@RequestParam(value="query", defaultValue="") String query){
        if (nlQueryEngine==null)
            nlQueryEngine=new NLQueryEngine(context.driver);
        return docSearch.search(query);
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET,RequestMethod.POST})
    public List<Neo4jRelation> relationList(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jRelation.getNeo4jRelationList(id,context.driver);
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jNode node(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jNode.get(id,context.driver);
    }

}

@Component
class Context{

    Driver driver=null;
    String dataDir=null;

    @Autowired
    public Context(Conf conf){
        this.dataDir=conf.getDataDir();
        this.driver= GraphDatabase.driver(conf.getBoltUrl(), AuthTokens.basic("neo4j", "password"));
    }

}