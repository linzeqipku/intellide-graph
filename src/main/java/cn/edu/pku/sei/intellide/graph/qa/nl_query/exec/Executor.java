package cn.edu.pku.sei.intellide.graph.qa.nl_query.exec;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing.TreeNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

public class Executor {

    private TreeNode focusNode;
    private GraphDatabaseService db;

    public Executor(GraphDatabaseService db){
        this.db = db;
    }

    public void execute(TreeNode root){
        getFocusNode(root);

    }

    private void getFocusNode(TreeNode root){
        if (root.atom.isAbstractEntity())
            focusNode = root;
        else{
            getFocusNode(root.leftChild);
            getFocusNode(root.rightChild);
        }
    }


}
