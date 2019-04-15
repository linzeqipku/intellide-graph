package cn.edu.pku.sei.intellide.graph.qa.nl_query.exec;

import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping.Atom;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.parsing.TreeNode;
import org.neo4j.graphdb.*;

import java.util.*;

public class Executor {

    private GraphDatabaseService db;

    public Executor(GraphDatabaseService db){
        this.db = db;
    }

    public void execute(TreeNode root){
        Set<Node> nodeSet = eval(root);
        if (nodeSet.size() > 0){
            try(Transaction tx = db.beginTx()) {
                for (Node node : nodeSet) {
                    System.out.println(node.getId() + " " + node.getProperty("name"));
                }
                tx.success();
            }
        }
    }

    private Set<Node> getNodes(Atom atom){
        Set<Node> result = new HashSet<>(2);
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> iterator = db.findNodes(Label.label(atom.getType()));
            while (iterator.hasNext()) {
                Node node = iterator.next();
                String name = (String) node.getProperty(JavaExtractor.NAME);
                if (name.equals(atom.getName())){
                    result.add(node);
                }
            }
            tx.success();
        }
        return result;
    }

    private Set<Node> getRelativeNodes(Node node, String rel, boolean incoming){
        Set<Node> result = new HashSet<>();
        RelationshipType relType = RelationshipType.withName(rel);
        try (Transaction tx = db.beginTx()) {
            Iterator<Relationship> iterator;
            if (incoming)
                iterator = node.getRelationships(relType, Direction.INCOMING).iterator();
            else
                iterator = node.getRelationships(relType, Direction.OUTGOING).iterator();
            while (iterator.hasNext()) {
                Relationship relation = iterator.next();
                result.add(relation.getOtherNode(node));
            }
            tx.success();
        }
        return result;
    }

    private Set<Node> eval(TreeNode root){

        if (root.atom.getType().equals(Atom.AND)){
            Set<Node> left = eval(root.leftChild);
            Set<Node> right = eval(root.rightChild);
            // intersection
            // if one side is the abstract node, AND returns the other side
            if (left == null)
                return right;
            else if (right == null)
                return left;
            else {
                left.retainAll(right);
                return left;
            }
        }

        if (root.atom.getType().equals(Atom.OR)){
            Set<Node> left = eval(root.leftChild);
            Set<Node> right = eval(root.rightChild);
            // union, left and right cannot be null
            left.addAll(right);
            return left;
        }

        else if (root.atom.getType().equals(Atom.JOIN)){
            Set<Node> joinSet = new HashSet<>();
            if (root.leftChild.treeOrder == TreeNode.RELATION){
                Set<Node> right = eval(root.rightChild);
                for (Node node : right){ // get its another node by the relation
                    Set<Node> others = getRelativeNodes(node, root.leftChild.atom.getType(), true);
                    joinSet.addAll(others);
                }
            }
            else if (root.rightChild.treeOrder == TreeNode.RELATION){
                Set<Node> left = eval(root.leftChild);
                for (Node node : left){
                    Set<Node> others = getRelativeNodes(node, root.rightChild.atom.getType(), false);
                    joinSet.addAll(others);
                }
            }
            return joinSet;
        }
        else if (root.atom.getOrder() == TreeNode.ENTITY){
            if (root.atom.isAbstractEntity()) { // special case, return ALL nodes
                return null;
            }
            // find this entity node by type and name
            return getNodes(root.atom);
        }
        System.err.println("Error: root atom has unexpected type!");
        return new HashSet<>(1);
    }

}
