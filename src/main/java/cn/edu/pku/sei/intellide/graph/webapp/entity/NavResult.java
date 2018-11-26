package cn.edu.pku.sei.intellide.graph.webapp.entity;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavResult {

    private final List<NavNode> nodes = new ArrayList<>();
    private final List<NavRelation> relationships = new ArrayList<>();
    private final int propertyTypeCount, propertyCount;

    private NavResult(int propertyTypeCount, int propertyCount) {
        this.propertyTypeCount = propertyTypeCount;
        this.propertyCount = propertyCount;
    }

    public static NavResult fetch(GraphDatabaseService db) {
        NavResult res;
        try (Transaction tx = db.beginTx()) {
            int propertyTypeCount = 0;
            for (String i : db.getAllPropertyKeys())
                propertyTypeCount++;
            int propertyCount = 0;
            for (Node node : db.getAllNodes())
                for (String key : node.getPropertyKeys())
                    propertyCount++;
            res = new NavResult(propertyTypeCount, propertyCount);
            Map<String, Integer> map = new HashMap<>();
            for (Node node : db.getAllNodes()) {
                if (!node.getLabels().iterator().hasNext())
                    continue;
                String label = node.getLabels().iterator().next().name();
                if (!map.containsKey(label))
                    map.put(label, 0);
                map.put(label, map.get(label) + 1);
            }
            int c = 0;
            Map<String, Long> labelToId = new HashMap<>();
            for (String label : map.keySet()) {
                res.addNode(c, label, map.get(label));
                labelToId.put(label, (long) c);
                c++;
            }
            Map<Triple, Integer> rMap = new HashMap<>();
            for (Relationship rel : db.getAllRelationships()) {
                String type = rel.getType().name();
                String start = rel.getStartNode().getLabels().iterator().next().name();
                String end = rel.getEndNode().getLabels().iterator().next().name();
                Triple triple = new ImmutableTriple(start, end, type);
                if (!rMap.containsKey(triple))
                    rMap.put(triple, 0);
                rMap.put(triple, rMap.get(triple) + 1);
            }
            c = 0;
            for (Triple triple : rMap.keySet()) {
                res.addRelation(c, labelToId.get(triple.getLeft()), labelToId.get(triple.getMiddle()), rMap.get(triple), (String) triple.getRight());
                c++;
            }
            tx.success();
        }
        return res;
    }

    public int getPropertyTypeCount() {
        return propertyTypeCount;
    }

    public int getPropertyCount() {
        return propertyCount;
    }

    public void addNode(long id, String label, int count) {
        nodes.add(new NavNode(id, label, count));
    }

    public void addRelation(long id, long startNode, long endNode, int count, String type) {
        relationships.add(new NavRelation(id, startNode, endNode, count, type));
    }

    public List<NavNode> getNodes() {
        return nodes;
    }

    public List<NavRelation> getRelationships() {
        return relationships;
    }

    public class NavNode {
        private final long id;
        private final String label;
        private final int count;

        public NavNode(long id, String label, int count) {
            this.id = id;
            this.label = label;
            this.count = count;
        }

        public long getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }
    }

    public class NavRelation {
        private final long id;
        private final long startNode;
        private final long endNode;
        private final int count;
        private final String type;

        public NavRelation(long id, long startNode, long endNode, int count, String type) {
            this.id = id;
            this.startNode = startNode;
            this.endNode = endNode;
            this.count = count;
            this.type = type;
        }

        public long getId() {
            return id;
        }

        public long getStartNode() {
            return startNode;
        }

        public long getEndNode() {
            return endNode;
        }

        public int getCount() {
            return count;
        }

        public String getType() {
            return type;
        }
    }
}