package cn.edu.pku.sei.intellide.graph.qa.code_search;

import javafx.util.Pair;

import java.util.*;

public class APILocater {
    GraphReader graphReader;
    List<MyNode> graph;
    List<Set<MyNode>> rootNodeSet = new ArrayList<>();

    public APILocater(GraphReader reader){
        this.graphReader = reader;
        this.graph = graphReader.getAjacentGraph();
    }

    public MySubgraph query(Set<String> queryList){
        rootNodeSet.clear();
        for (String word: queryList){
            Set<MyNode> cur = new HashSet<>();
            for (MyNode node: graph){
                if (node.cnWordSet.contains(word))
                    cur.add(node);
            }
            if (cur.size() > 0) {
                rootNodeSet.add(cur);
                System.out.println(word + " node set size: " + cur.size());
            } else {
                System.out.println(word + " has relevant no node");
            }
        }
        if (rootNodeSet.size() == 0){
            System.out.println("no matched nodes found");
            return null;
        }
        int minSize = Integer.MAX_VALUE;
        int startSetIndex = 0;
        for (int i = 0; i < rootNodeSet.size(); ++i){ // find the smallest root set
            if (rootNodeSet.get(i).size() < minSize){
                minSize = rootNodeSet.get(i).size();
                startSetIndex = i;
            }
        }

        List<Pair<Integer,MySubgraph>> candidateList = new ArrayList<>();
        minSize = Integer.MAX_VALUE;
        Set<MyNode> startSet = rootNodeSet.get(startSetIndex);
        for (MyNode node : startSet){
            MySubgraph subgraph = BFS(node);
            if (subgraph == null)
                continue;
            candidateList.add(new Pair<>(subgraph.nodes.size(), subgraph));
            if (subgraph.nodes.size() < minSize){
                minSize = subgraph.nodes.size();
            }
        }
        if (candidateList.size() == 0) // cannot find a subgraph
            return null;

        List<MySubgraph> optimal = new ArrayList<>();
        for (Pair<Integer, MySubgraph> pair : candidateList){
            if (pair.getKey() == minSize)
                optimal.add(pair.getValue());
        } // if size equals, return a random one
        return optimal.get((int)(Math.random()*optimal.size()));
    }

    public MySubgraph BFS(MyNode start){
        boolean[] coveredRoot = new boolean[rootNodeSet.size()];
        int coveredCnt = 0;
        for (int i = 0; i < coveredRoot.length; ++i){ // start node may cover several roots
            if (rootNodeSet.get(i).contains(start)){
                coveredRoot[i] = true;
                coveredCnt++;
            }
        }

        Set<MyNode> selected = new HashSet<>(); // selected nodes
        selected.add(start);
        List<Pair<MyNode, MyNode>> paths = new ArrayList<>();
        Queue<MyNode> Q = new LinkedList<>();
        Set<MyNode> visited = new HashSet<>();

        while(coveredCnt < coveredRoot.length) { // until all roots are coverd
            Q.clear();
            visited.clear();
            for (MyNode node : selected) {
                node.father = null;
                Q.offer(node);
                visited.add(node);
            }
            boolean found = false;
            while (!Q.isEmpty()) {
                MyNode head = Q.poll();
                for (int i = 0; i < coveredRoot.length; ++i) { //head may cover several roots
                    if (!coveredRoot[i] && rootNodeSet.get(i).contains(head)) { // find a new root
                        coveredRoot[i] = true;
                        coveredCnt++;
                        found = true;
                    }
                }
                if (found){
                    selected.add(head);
                    MyNode tmp = head;
                    while (tmp.father != null) {
                        paths.add(new Pair<>(tmp, tmp.father));
                        tmp = tmp.father;
                    }
                    break;
                }
                for (MyNode next : head.neighbors) {
                    if (!visited.contains(next)) {
                        visited.add(next);
                        next.father = head;
                        Q.offer(next);
                    }
                }
            }
            if (!found) { // the selected cannot expand to other root, fail to construct a subgraph
                return null;
            }
        }
        MySubgraph subgraph = new MySubgraph();
        for (MyNode node : selected)
            subgraph.nodes.add(node.id);
        for (Pair<MyNode, MyNode> pair : paths){
            long n1 = pair.getKey().id;
            long n2 = pair.getValue().id;
            subgraph.nodes.add(n1);
            subgraph.nodes.add(n2);
            long edgeId = graphReader.getEdgeIdByNodes(n1, n2);
            subgraph.edges.add(edgeId);
        }
        return subgraph;
    }
}
