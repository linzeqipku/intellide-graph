package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.rules.PathsJson;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.*;
import lombok.Getter;
import org.neo4j.graphdb.*;

import java.util.*;

public class ExtractModel {

    private static Map<GraphDatabaseService, ExtractModel> instances = new HashMap<>();

    @Getter
    private GraphDatabaseService db = null;
    @Getter
    private GraphSchema graphSchema = null;
    @Getter
    private Graph graph = null;

    private int n = 0;
    private int dis[][] = new int[100][100];
    private int fa[][] = new int[100][100];
    private GraphEdgeType edgePath[][] = new GraphEdgeType[100][100];
    private Map<Integer, String> id2str = new HashMap<>();
    private Map<String, Integer> str2id = new HashMap<>();

    public static synchronized ExtractModel getInstance(GraphDatabaseService db){
        ExtractModel instance = instances.get(db);
        if (instance == null){
            instance = new ExtractModel(db);
            instances.put(db, instance);
        }
        return instance;
    }

    private ExtractModel(GraphDatabaseService db) {
        this.db = db;
        graphSchema = new GraphSchema();
        graph = extractProgramAbstract();
        floyd();
        addPreDefined();
    }

    private void addPreDefined() {
        PathsJson.getPaths(graphSchema);
    }

    private void floyd() {

        for (String startName : graphSchema.vertexTypes.keySet()) {
            id2str.put(n, startName);
            str2id.put(startName, n);
            n++;
        }
        for (int i = 0; i < n; i++) {
            dis[i][i] = 0;
            for (int j = 0; j < n; j++) {
                dis[i][j] = 100000;
            }
        }
        for (Set<GraphEdgeType> edgeTypes : graphSchema.edgeTypes.values()) {
            for (GraphEdgeType edgeType : edgeTypes) {
                dis[str2id.get(edgeType.start.name)][str2id.get(edgeType.end.name)] = 1;
                dis[str2id.get(edgeType.end.name)][str2id.get(edgeType.start.name)] = 1;
                edgePath[str2id.get(edgeType.start.name)][str2id.get(edgeType.end.name)] = edgeType;
                edgePath[str2id.get(edgeType.end.name)][str2id.get(edgeType.start.name)] = edgeType;
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++)
                    if (i != j) {
                        if (dis[i][k] + dis[k][j] < dis[i][j]) {
                            dis[i][j] = dis[i][k] + dis[k][j];
                            fa[i][j] = k;
                        }
                    }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                if (dis[i][j] < 10000) {
                    getPath(i, j);
                }
        }
        for (GraphVertexType vertexType : graphSchema.vertexTypes.values()) {
            GraphEdgeType edgeType = graphSchema.findGraphEdgeTypeByVertex(vertexType, vertexType);
            GraphPath path = new GraphPath();
            path.edges.add(edgeType);
            vertexType.shortestPaths.put(vertexType.name, path);
        }
    }

    private void getPath(int x, int y) {
        GraphVertexType startType = graphSchema.vertexTypes.get(id2str.get(x));
        GraphVertexType endType = graphSchema.vertexTypes.get(id2str.get(y));


        if (startType.shortestPaths.keySet().contains(endType.name)) return;
        if (x == y) startType.shortestPaths.put(startType.name, new GraphPath());
        if (dis[x][y] == 1) {
            GraphPath path = new GraphPath();
            path.edges.add(edgePath[x][y]);
            startType.shortestPaths.put(endType.name, path);
            return;
        }
        int mid = fa[x][y];
        GraphVertexType midType = graphSchema.vertexTypes.get(id2str.get(mid));
        getPath(x, mid);
        getPath(mid, y);
        List<GraphVertexType> tmpnode = new ArrayList<>();
        List<GraphEdgeType> tmpedge = new ArrayList<>();
        tmpnode.addAll(startType.shortestPaths.get(midType.name).nodes);
        tmpedge.addAll(startType.shortestPaths.get(midType.name).edges);
        tmpnode.add(midType);
        tmpnode.addAll(midType.shortestPaths.get(endType.name).nodes);
        tmpedge.addAll(midType.shortestPaths.get(endType.name).edges);
        GraphPath path = new GraphPath();
        path.nodes = tmpnode;
        path.edges = tmpedge;
        startType.shortestPaths.put(endType.name, path);
    }

    private Graph extractProgramAbstract() {

        Graph graph = new Graph();

        try (Transaction tx = db.beginTx()) {
            for (Node node : db.getAllNodes()) {
                for (Iterator<Label> o_iterator = node.getLabels().iterator(); o_iterator.hasNext(); ) {
                    Label label = o_iterator.next();
                    String node_type = label.name();
                    if (!GraphSchemaKeywords.getSingle().types.containsKey(node_type)) continue;
                    Object leftObj = node.getProperty(GraphSchemaKeywords.getSingle().types.get(node_type).getLeft());
                    Object rightObj = node.getProperty(GraphSchemaKeywords.getSingle().types.get(node_type).getRight());
                    String name;
                    String longName;
                    if (leftObj instanceof Integer) name = "" + ((Integer) leftObj).intValue();
                    else name = (String) leftObj;
                    if (rightObj instanceof Integer) longName = "" + ((Integer) rightObj).intValue();
                    else longName = (String) rightObj;
                    Vertex vertex = new Vertex(node.getId(), name, longName, node_type);
                    graph.add(vertex);
                    break;
                }
            }
            for (Relationship rel : db.getAllRelationships()) {
                long srcId = rel.getStartNodeId();
                long dstId = rel.getEndNodeId();
                String type = rel.getType().name();
                if (graph.contains(srcId) && graph.contains(dstId)) {
                    String src_type = graph.vertexes.get(srcId).labels;
                    String dst_type = graph.vertexes.get(dstId).labels;
                    if (!graphSchema.vertexTypes.containsKey(src_type)) {
                        graphSchema.vertexTypes.put(src_type, new GraphVertexType(src_type));
                    }
                    if (!graphSchema.vertexTypes.containsKey(dst_type)) {
                        graphSchema.vertexTypes.put(dst_type, new GraphVertexType(dst_type));
                    }
                    if (!graphSchema.vertexTypes.get(dst_type).incomings.containsKey(type))
                        graphSchema.vertexTypes.get(dst_type).incomings.put(type, new HashSet<>());
                    graphSchema.vertexTypes.get(dst_type).incomings.get(type).add(graphSchema.vertexTypes.get(src_type));
                    if (!graphSchema.vertexTypes.get(src_type).outcomings.containsKey(type))
                        graphSchema.vertexTypes.get(src_type).outcomings.put(type, new HashSet<>());
                    graphSchema.vertexTypes.get(src_type).outcomings.get(type).add(graphSchema.vertexTypes.get(dst_type));
                }
            }
            for (GraphVertexType vertexType : graphSchema.vertexTypes.values()) {
                for (String type : vertexType.outcomings.keySet()) {
                    for (GraphVertexType dstVertex : vertexType.outcomings.get(type)) {
                        if (!graphSchema.edgeTypes.containsKey(type)) graphSchema.edgeTypes.put(type, new HashSet<>());
                        graphSchema.edgeTypes.get(type).add(new GraphEdgeType(type, vertexType, dstVertex, true));
                    }
                }
            }
            for (Set<GraphEdgeType> edgeTypes : graphSchema.edgeTypes.values()) {
                for (GraphEdgeType edgeType : edgeTypes) {
                    edgeType.start.outcomingsEdges.add(edgeType);
                    edgeType.end.incomingsEdges.add(edgeType);
                }
            }
            for (Node node : db.getAllNodes()) {
                for (Iterator<Label> o_iterator = node.getLabels().iterator(); o_iterator.hasNext(); ) {
                    Label label = o_iterator.next();
                    String node_type = label.name();
                    GraphVertexType vertexType = graphSchema.vertexTypes.get(node_type);
                    for (String propertiesName : node.getAllProperties().keySet()) {
                        if (vertexType == null)
                            continue;
                        if (!vertexType.attrs.keySet().contains(propertiesName)) {
                            vertexType.attrs.put(propertiesName, new GraphAttribute(propertiesName, vertexType));
                        }
                    }
                    break;
                }
            }
            tx.success();
        }
        return graph;
    }

}
