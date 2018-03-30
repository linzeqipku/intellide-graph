package cn.edu.pku.sei.intellide.graph.qa.code_search;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyNode{
        long id;
        MyNode father;
        public String fullName;
        public Set<String> cnWordSet = new HashSet<>();
        public List<MyNode> neighbors =  new ArrayList<>();
        public MyNode(long id){
            this.id = id;
            this.father = null;
        }
}
