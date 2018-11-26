package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPNode;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPRelation;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.*;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.ExtractModel;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.schema.GraphEdgeType;

public class SchemaMapping {

    private Query query = null;

    public void mapping(Query query) {
        this.query = query;
        /**
         * Case 1 : AttributeSchema without Attribute : return a attribute of a vertex
         * Case 2 : Attribute pair()
         */
        init();
        linkAttributeAndAttributeSchema();
        linkAttributeAndVertexSchema();
        linkAttributeSchemaAndVectexSchemaAndVertex();
    }

    private void init() {
        for (NLPToken token : query.tokens)
            if (token.mapping != null && !(token.mapping instanceof NLPEdgeSchemaMapping || token.mapping instanceof NLPPathSchemaMapping))
                query.nodes.add(new NLPNode(token));
        int cnt = 0;
        for (NLPNode node : query.nodes) {
            node.id = cnt;
            cnt++;
        }
    }


    private void linkAttributeAndAttributeSchema() {
        /*make pair attribute with attrbuteschema*/
        for (NLPNode node : query.nodes) {
            if (node.token.mapping instanceof NLPAttributeMapping) {
                for (long offset = 1; offset < 20; offset++) {
                    boolean find = false;
                    for (NLPNode faNode : query.nodes) {
                        if (Math.abs(faNode.token.offset - node.token.offset) != offset) continue;
                        if (faNode.token.mapping instanceof NLPAttributeSchemaMapping &&
                                ((NLPAttributeSchemaMapping) faNode.token.mapping).attrType.equals(((NLPAttributeMapping) node.token.mapping).type.attrType)) {
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

    private void linkAttributeAndVertexSchema() {
        for (NLPNode node : query.nodes) {
            if (node.token.mapping instanceof NLPAttributeSchemaMapping && !node.nextNode.isEmpty()) {
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

    private void linkAttributeSchemaAndVectexSchemaAndVertex() {
        for (NLPNode node : query.nodes) {
            if (node.token.mapping instanceof NLPAttributeSchemaMapping && node.nextNode.isEmpty()) {
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
                            if (!((NLPAttributeSchemaMapping) node.token.mapping).must && !((NLPAttributeSchemaMapping) node.token.mapping).isbool)
                                faNode.focus = true;
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

}
