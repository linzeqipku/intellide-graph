package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPNode;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPRelation;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping.*;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.extractmodel.ExtractModel;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphEdgeType;

public class SchemaMapping {
    public static Query query = null;
    public static void  mapping(Query query){
        SchemaMapping.query = query;
        /**
         * Case 1 : AttributeSchema without Attribute : return a attribute of a vertex
         * Case 2 : Attribute pair()
         */
        init();
        linkAttributeAndAttributeSchema();
        linkAttributeAndVertexSchema();
        linkAttributeSchemaAndVectexSchemaAndVertex();
        //mappingEdgeSchema();
    }
    public static void init(){
        for (NLPToken token : query.tokens)
            if (token.mapping != null && !(token.mapping instanceof NLPEdgeSchemaMapping || token.mapping instanceof NLPPathSchemaMapping))
                query.nodes.add(new NLPNode(token));
        int cnt = 0;
        for (NLPNode node : query.nodes) {
            node.id = cnt;
            cnt++;
        }
    }


    public static void linkAttributeAndAttributeSchema(){
        /*make pair attribute with attrbuteschema*/
        for (NLPNode node : query.nodes){
            if (node.token.mapping instanceof NLPAttributeMapping){
                for (long offset = 1; offset < 20; offset++) {
                    boolean find = false;
                    for (NLPNode faNode : query.nodes) {
                        if (Math.abs(faNode.token.offset - node.token.offset) != offset) continue;
                        if (faNode.token.mapping instanceof NLPAttributeSchemaMapping &&
                                ((NLPAttributeSchemaMapping)faNode.token.mapping).attrType.equals(((NLPAttributeMapping) node.token.mapping).type.attrType)) {
                            if (!faNode.nextNode.isEmpty()) continue;
                            NLPRelation relation1 = new NLPRelation("is");
                            NLPRelation relation2 = new NLPRelation("is");
                            relation1.mirror = relation2;
                            relation2.mirror = relation1;
                            relation2.direct = false;
                            faNode.addNext(node, relation1);
                            node.addLast(faNode, relation2);
                            find = true;
                            break;
                        }
                    }
                    if (find) break;
                }
            }
        }
    }

    public static void linkAttributeAndVertexSchema(){
        for (NLPNode node : query.nodes){
            if (node.token.mapping instanceof NLPAttributeSchemaMapping && !node.nextNode.isEmpty()){
                for (long offset = 1; offset < 20; offset++) {
                    boolean find = false;
                    for (NLPNode faNode : query.nodes) {
                        if (Math.abs(faNode.token.offset - node.token.offset) != offset) continue;
                        if (faNode.token.mapping instanceof NLPVertexSchemaMapping && !(faNode.token.mapping instanceof NLPVertexMapping) &&
                                ((NLPVertexSchemaMapping) faNode.token.mapping).vertexType.equals(((NLPAttributeSchemaMapping) node.token.mapping).vertexType)) {
                            NLPRelation relation1 = new NLPRelation("has");
                            NLPRelation relation2 = new NLPRelation("has");
                            relation1.mirror = relation2;
                            relation2.mirror = relation1;
                            relation2.direct = false;
                            faNode.addNext(node, relation1);
                            node.addLast(faNode, relation2);
                            faNode.hasattr = true;
                            find = true;
                            break;
                        }
                    }
                    if (find) break;
                }
            }
        }
    }
    public static void linkAttributeSchemaAndVectexSchemaAndVertex(){
        for (NLPNode node : query.nodes) {
            if (node.token.mapping instanceof NLPAttributeSchemaMapping && node.nextNode.isEmpty()) { //has no value
                boolean flag = false;
                for (long offset = 1; offset < 20; offset++) {
                    for (NLPNode faNode : query.nodes) {
                        if (Math.abs(faNode.token.offset - node.token.offset) != offset) continue;
                        if (faNode.token.mapping instanceof NLPVertexSchemaMapping && !(faNode.token.mapping instanceof NLPVertexMapping) &&
                                ((NLPVertexSchemaMapping) faNode.token.mapping).vertexType.equals(((NLPAttributeSchemaMapping) node.token.mapping).vertexType)) {
                            NLPRelation relation1 = new NLPRelation("has");
                            NLPRelation relation2 = new NLPRelation("has");
                            relation1.mirror = relation2;
                            relation2.mirror = relation1;
                            relation2.direct = false;
                            faNode.addNext(node, relation1);
                            node.addLast(faNode, relation2);
                            if (!((NLPAttributeSchemaMapping) node.token.mapping).must && !((NLPAttributeSchemaMapping) node.token.mapping).isbool)faNode.focus = true;
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                }
                if (flag) continue;
                for (long offset = 1; offset < 20; offset++) {
                    for (NLPNode faNode : query.nodes) {
                        if (Math.abs(faNode.token.offset - node.token.offset) != offset) continue;
                        if (faNode.token.mapping instanceof NLPVertexMapping &&
                                ((NLPVertexSchemaMapping) faNode.token.mapping).vertexType.equals(((NLPAttributeSchemaMapping) node.token.mapping).vertexType)) {
                            NLPRelation relation1 = new NLPRelation("has");
                            NLPRelation relation2 = new NLPRelation("has");
                            relation1.mirror = relation2;
                            relation2.mirror = relation1;
                            relation2.direct = false;
                            faNode.addNext(node, relation1);
                            node.addLast(faNode, relation2);
                            faNode.focus = true;
                            if (flag) break;
                            break;
                        }
                    }
                    if (flag) break;
                }
            }
        }
    }
    public static void mappingEdgeSchema(){
        for (NLPToken token : query.tokens) if (token.mapping instanceof NLPEdgeSchemaMapping){
            double direct = 1;
            if (token.POS.equals("VBD")) direct = -1;
            boolean flagFind = false;
            String edgeTypeName = ((NLPEdgeSchemaMapping) token.mapping).type;
            GraphEdgeType edgeType = ((NLPEdgeSchemaMapping) token.mapping).edgeType;
            NLPNode nodeStartFinal = null;
            NLPNode nodeEndFinal = null;
            double score = 10000;
            for (NLPNode nodeStart : query.nodes) if (nodeStart.token.mapping instanceof NLPVertexSchemaMapping){
                if (edgeType.start.name.equals(((NLPVertexSchemaMapping) nodeStart.token.mapping).vertexType.name)) {
                    for (NLPNode nodeEnd : query.nodes)
                        if (nodeEnd.token.mapping instanceof NLPVertexSchemaMapping) {
                            if (nodeStart == nodeEnd) continue;
                            if (edgeType.end.name.equals(((NLPVertexSchemaMapping) nodeEnd.token.mapping).vertexType.name)) {
                                flagFind = true;
                                double newscore = Math.abs(token.offsetVal + direct * 1 - nodeEnd.token.offsetVal) + Math.abs(token.offsetVal - direct * 1 - nodeStart.token.offsetVal);
                                if (newscore < score) {
                                    score = newscore;
                                    nodeStartFinal = nodeStart;
                                    nodeEndFinal = nodeEnd;
                                }
                                //break;
                            }
                        }
                    //if (flagFind) break;
                }
            }
            if (flagFind) {
                NLPRelation relation1 = new NLPRelation(edgeType,token);
                NLPRelation relation2 = new NLPRelation(edgeType,token);
                relation1.mirror = relation2;
                relation2.mirror = relation1;
                nodeStartFinal.addNext(nodeEndFinal, relation1);
                nodeEndFinal.addLast(nodeStartFinal, relation2);
                continue;
            }

            NLPNode newNodeLast = null;
            for (NLPNode node : query.nodes) if (node.token.mapping instanceof NLPVertexSchemaMapping){
                if (edgeType.start.name.equals(((NLPVertexSchemaMapping) node.token.mapping).vertexType.name)) {
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                            graphSchema.vertexTypes.get(edgeType.end.name),newNode.token,1);
                    newNode.token.mapping = mapping;
                    newNode.focus = true;

                    double newscore = Math.abs(token.offsetVal - direct*1 -node.token.offsetVal);
                    if (newscore < score){
                        flagFind = true;
                        score = newscore ;
                        nodeStartFinal = node;
                        nodeEndFinal = newNode;
                        newNodeLast = newNode;
                    }
                }
                if (edgeType.end.name.equals(((NLPVertexSchemaMapping) node.token.mapping).vertexType.name)) {
                    NLPNode newNode = new NLPNode(new NLPToken("what"));
                    NLPMapping mapping = new NLPVertexSchemaMapping(ExtractModel.getSingle().
                            graphSchema.vertexTypes.get(edgeType.start.name),newNode.token,1);
                    newNode.token.mapping = mapping;
                    newNode.focus = true;

                    double newscore = Math.abs(token.offsetVal + direct*1 -node.token.offsetVal);
                    if (newscore < score){
                        flagFind = true;
                        score = newscore ;
                        nodeStartFinal = newNode;
                        nodeEndFinal = node;
                        newNodeLast = newNode;
                    }
                }
            }
            if (flagFind) {
                NLPRelation relation1 = new NLPRelation(edgeType,token);
                NLPRelation relation2 = new NLPRelation(edgeType,token);
                relation1.mirror = relation2;
                relation2.mirror = relation1;
                nodeStartFinal.addNext(nodeEndFinal, relation1);
                nodeEndFinal.addLast(nodeStartFinal, relation2);
                query.nodes.add(newNodeLast);
            }
        }
    }
}
