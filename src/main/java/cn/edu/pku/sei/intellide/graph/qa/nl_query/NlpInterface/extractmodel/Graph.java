package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

	public Map<Long, Vertex> vertexes=new HashMap<>();
	
	public void add(Vertex v){
		vertexes.put(v.id, v);
	}
	
	public Vertex get(long id){
		return vertexes.get(id);
	}
	public boolean contains(long id){
		return vertexes.containsKey(id);
	}
	
	public Set<Vertex> getAllVertexes(){
		return new HashSet<>(vertexes.values());
	}


	public void addEdge(Vertex v1, Vertex v2, String type){
//		if (!v2.incomingEdges.containsKey(type))
//			v2.incomingEdges.put(type, new HashSet<>());
//		v2.incomingEdges.get(type).add(v1);
//		if (!v1.outgoingEdges.containsKey(type))
//			v1.outgoingEdges.put(type, new HashSet<>());
//		v1.outgoingEdges.get(type).add(v2);
	}
	
}
