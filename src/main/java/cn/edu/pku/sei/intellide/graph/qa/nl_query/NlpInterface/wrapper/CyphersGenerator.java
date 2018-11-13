package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.*;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPAttributeMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPAttributeSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPVertexSchemaMapping;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.extractmodel.GraphSchemaKeywords;
import lombok.Getter;

public class CyphersGenerator {

    @Getter
    private Query query;

    public String generate(Query query) {
        /*从tupleLinks翻译到cypher*/
        this.query = query;
        String matchText = getMatchCypher();
        String whereText = getWhereCypher();
        String returnText = getReturnCypher();
        if (matchText.equals("") || returnText.equals("")) return "";
        return matchText + " " + whereText + " " + returnText;
    }

    public String getReturnCypher() {
        String returnText = "RETURN";
        if (query.focusNode == null) return "";
        for (int i = 0; i < query.focusNode.nextRelation.size(); i++) {
            NLPRelation relation = query.focusNode.nextRelation.get(i);
            NLPNode node = query.focusNode.nextNode.get(i);
            if (relation.otherType != null && relation.otherType.equals("has")) {
                if (node.token.mapping instanceof NLPAttributeSchemaMapping && !((NLPAttributeSchemaMapping) node.token.mapping).isbool && !((NLPAttributeSchemaMapping) node.token.mapping).must) {
                    if (node.nextNode.size() == 0) {
                        returnText += String.format(" n%d.%s", query.focusNode.id, ((NLPAttributeSchemaMapping) node.token.mapping).attrType);
                        query.returnType = "string";
                        return returnText;
                    }

                }
            }

        }
        returnText += String.format(" n%d, id(n%d), labels(n%d)", query.focusNode.id, query.focusNode.id, query.focusNode.id);
        return returnText;
    }

    public String getWhereCypher() {
        String whereText = "WHERE";
        boolean first = true;
        for (NLPNode node : query.nodes) {
            if (!(node.token.mapping instanceof NLPVertexSchemaMapping)) continue;
            if (((NLPVertexSchemaMapping) node.token.mapping).must) {
                String haha = "";
                for (Long r : ((NLPVertexSchemaMapping) node.token.mapping).s) {
                    if (haha.equals("")) {
                        haha = String.format("id(n%d)=%s", node.id, r);
                    } else haha += String.format(" OR id(n%d)=%s", node.id, r);
                }
                haha = "(" + haha + ")";
                if (first) whereText += String.format(" %s", haha);
                else
                    whereText += String.format(" AND %s", haha);
                first = false;
            }
            for (int i = 0; i < node.nextNode.size(); i++) {
                boolean firstAttrValue = true;
                NLPRelation relation = node.nextRelation.get(i);
                String attrStr = "";
                NLPNode nodeA = node.nextNode.get(i);
                if (relation.otherType != null && relation.otherType.equals("has")) {
                    if (((NLPAttributeSchemaMapping) nodeA.token.mapping).must) {
                        for (Long r : ((NLPAttributeSchemaMapping) nodeA.token.mapping).s) {
                            if (firstAttrValue) attrStr += String.format("id(n%d)=%s", node.id, r);
                            else attrStr += String.format(" OR id(n%d)=%s", node.id, r);
                            firstAttrValue = false;
                        }
                    } else if (((NLPAttributeSchemaMapping) nodeA.token.mapping).isbool) {
                        if (firstAttrValue)
                            attrStr += String.format("n%d.%s =%s", node.id, ((NLPAttributeSchemaMapping) nodeA.token.mapping).attrType, ((NLPAttributeSchemaMapping) nodeA.token.mapping).boolval);
                        else
                            attrStr += String.format(" OR n%d.%s =%s", node.id, ((NLPAttributeSchemaMapping) nodeA.token.mapping).attrType, ((NLPAttributeSchemaMapping) nodeA.token.mapping).boolval);
                        firstAttrValue = false;
                    } else
                        for (NLPNode nodeB : nodeA.nextNode) {
                            if (firstAttrValue)
                                attrStr += String.format("n%d.%s =\"%s\"", node.id, ((NLPAttributeSchemaMapping) nodeA.token.mapping).attrType, ((NLPAttributeMapping) nodeB.token.mapping).attrValue);
                            else
                                attrStr += String.format(" OR n%d.%s =\"%s\"", node.id, ((NLPAttributeSchemaMapping) nodeA.token.mapping).attrType, ((NLPAttributeMapping) nodeB.token.mapping).attrValue);
                            firstAttrValue = false;
                        }
                }
                if (attrStr.length() <= 0) continue;
                if (first) whereText += String.format(" (%s)", attrStr);
                else whereText += String.format(" AND (%s)", attrStr);
                first = false;
            }
            if (!(node.token.mapping instanceof NLPVertexMapping)) continue;
            if (first) {
                whereText += String.format(" (n%d.%s = \"%s\")", node.id,
                        GraphSchemaKeywords.getSingle().types.get(((NLPVertexMapping) node.token.mapping).vertex.labels).getLeft(),
                        ((NLPVertexMapping) node.token.mapping).vertex.name);
            } else {
                whereText += String.format(" AND (n%d.%s = \"%s\")", node.id,
                        GraphSchemaKeywords.getSingle().types.get(((NLPVertexMapping) node.token.mapping).vertex.labels).getLeft(),
                        ((NLPVertexMapping) node.token.mapping).vertex.name);
            }
            first = false;
        }
        if (whereText.equals("WHERE")) return "";
        return whereText;
    }

    public String getMatchCypher() {
        String matchText = "";
        for (NLPInferenceLink inferenceLink : query.inferenceLinks) {
            NLPInferenceNode start = inferenceLink.start;
            String matchStr = "MATCH ";
            while (start != null) {
                if (start.node.token.mapping instanceof NLPVertexSchemaMapping)
                    matchStr += String.format("(n%d:%s)", start.node.id, ((NLPVertexSchemaMapping) start.node.token.mapping).vertexType.name);
                else
                    matchStr += String.format("(n%d:%s)", start.node.id, ((NLPVertexSchemaMapping) start.node.token.mapping).vertexType.name);
                if (start.isEnd) {
                    break;
                }
                String r = "";
                if (start.nextRelation.edgeType != null) r = "[:" + start.nextRelation.edgeType.name + "]";
                if (start.direct) matchStr += String.format("-%s->", r);
                else matchStr += String.format("<-%s-", r);
                start = start.nextInferNode;
            }
            matchText += matchStr;
        }
        return matchText;
    }
}
