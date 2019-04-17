package cn.edu.pku.sei.intellide.graph.extraction.code_embedding;

import java.util.*;

public class Graph {

    private final Map<String,Vertex> vertexMap=new HashMap<>();
    private final Set<Edge> edges=new HashSet<>();

    public Iterator<Vertex> getVertexes(){
        return vertexMap.values().iterator();
    }

    public Iterator<Edge> getEdges(){
        return edges.iterator();
    }

    void createVertex(String id){
        vertexMap.put(id, new Vertex(id));
    }

    Vertex getVertexById(String id){
        return vertexMap.get(id);
    }

    void createEdge(String id1, String id2, String type){
        Vertex v1=getVertexById(id1);
        Vertex v2=getVertexById(id2);
        createEdge(v1,v2,type);
    }

    void weight(){
        for (Edge edge:edges){
            long forwardCount=edge.source.outgoingEdges.stream().map(Edge::getTarget).count();
            long backwardCount=edge.target.incomingEdges.stream().map(Edge::getSource).count();
            edge.weight=1.0/forwardCount/backwardCount;
        }
    }

    private void createEdge(Vertex v1, Vertex v2, String type){
        if (v1==null||v2==null)
            return;
        if (v1.equals(v2))
            return;
        Edge e=new Edge(v1,v2,type,0);
        v1.outgoingEdges.add(e);
        v2.incomingEdges.add(e);
        edges.add(e);
    }

    public class Vertex{

        private String id;
        private Set<Edge> outgoingEdges=new HashSet<>();
        private Set<Edge> incomingEdges=new HashSet<>();
        private Vertex(String id) {
            this.id = id;
        }
        public String getId(){
            return id;
        }
        public Set<Vertex> getNeighbors(boolean outgoing, boolean incoming){
            Set<Vertex> r=new HashSet<>();
            if (outgoing)
                outgoingEdges.stream().map(Edge::getTarget).forEach(n->r.add(n));
            if (incoming)
                incomingEdges.stream().map(Edge::getSource).forEach(n->r.add(n));
            return r;
        }
        public double weightTo(Vertex v, boolean outgoing, boolean incoming){
            double r=0;
            if (outgoing)
                for (Edge e:outgoingEdges)
                    if (e.target==v)
                        r+=e.weight;
            if (incoming)
                for (Edge e:incomingEdges)
                    if (e.source==v)
                        r+=e.weight;
            return r;
        }
        @Override
        public boolean equals(Object v){
            if (v instanceof Vertex)
                return id.equals(((Vertex) v).id);
            else
                return false;
        }
        @Override
        public int hashCode(){
            return id.hashCode();
        }
    }

    public class Edge{
        private Vertex source,target;
        private String type;
        private double weight;
        private Edge(Vertex source, Vertex target, String type, double weight){
            this.source=source;
            this.target=target;
            this.type=type;
            this.weight=weight;
        }

        public Vertex getSource() {
            return source;
        }

        public Vertex getTarget() {
            return target;
        }

        public String getType() {
            return type;
        }

        public double getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object e){
            if (e instanceof Edge)
                return hashCode()==e.hashCode();
            else
                return false;
        }
        @Override
        public int hashCode(){
            return (""+source.hashCode()+" "+target.hashCode()+" "+type.hashCode()).hashCode();
        }
    }

}