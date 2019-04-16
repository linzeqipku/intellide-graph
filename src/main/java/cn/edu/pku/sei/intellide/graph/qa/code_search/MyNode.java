package cn.edu.pku.sei.intellide.graph.qa.code_search;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyNode {
    public String fullName;
    public Set<String> cnWordSet = new HashSet<>();
    public List<MyNode> neighbors = new ArrayList<>();
    public long id;
    MyNode father;
    double weight; // importance weight = 1/tokensize + whether has realtion with docs(0/1)

    public MyNode(long id) {
        this.id = id;
        this.father = null;
        weight = 0;
    }
}
