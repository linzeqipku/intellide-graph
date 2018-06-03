package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.extractmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Vertex {
	
	public long id;
	public String name;
	public String longName;
	public String labels;
	public double score = 0;
	//public Map<String, Set<Vertex>> incomingEdges = new HashMap<>();
	//public Map<String, Set<Vertex>> outgoingEdges = new HashMap<>();
	public Vertex(long id, String name, String longName, String label){
		this.id = id;
		this.name = name;
		this.longName = longName;
		this.labels = label;
	}
	
	@Override
	public boolean equals(Object v) {
		return v instanceof Vertex && id == ((Vertex) v).id;
	}
	
}
