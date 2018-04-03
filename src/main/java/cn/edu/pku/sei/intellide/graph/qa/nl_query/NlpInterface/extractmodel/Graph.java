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

	public void remove(Vertex vertex){
		vertexes.remove(vertex.id);
		for (String type:vertex.incomingEdges.keySet())
			for (Vertex vertex2:vertex.incomingEdges.get(type)){
				vertex2.outgoingEdges.get(type).remove(vertex);
				if (vertex2.outgoingEdges.get(type).size()==0)
					vertex2.outgoingEdges.remove(type);
			}
		for (String type:vertex.outgoingEdges.keySet())
			for (Vertex vertex2:vertex.outgoingEdges.get(type)){
				vertex2.incomingEdges.get(type).remove(vertex);
				if (vertex2.incomingEdges.get(type).size()==0)
					vertex2.incomingEdges.remove(type);
			}
		vertex.incomingEdges.clear();
		vertex.outgoingEdges.clear();
	}

	public void rename(String oType, String type){
		for (Vertex vertex:vertexes.values()){
			if (vertex.incomingEdges.containsKey(oType)){
				if (!vertex.incomingEdges.containsKey(type))
					vertex.incomingEdges.put(type, new HashSet<>());
				vertex.incomingEdges.get(type).addAll(vertex.incomingEdges.get(oType));
				vertex.incomingEdges.remove(oType);
			}
			if (vertex.outgoingEdges.containsKey(oType)){
				if (!vertex.outgoingEdges.containsKey(type))
					vertex.outgoingEdges.put(type, new HashSet<>());
				vertex.outgoingEdges.get(type).addAll(vertex.outgoingEdges.get(oType));
				vertex.outgoingEdges.remove(oType);
			}
		}
	}

	public void addEdge(Vertex v1, Vertex v2, String type){
		if (!v2.incomingEdges.containsKey(type))
			v2.incomingEdges.put(type, new HashSet<>());
		v2.incomingEdges.get(type).add(v1);
		if (!v1.outgoingEdges.containsKey(type))
			v1.outgoingEdges.put(type, new HashSet<>());
		v1.outgoingEdges.get(type).add(v2);
	}
	
}
