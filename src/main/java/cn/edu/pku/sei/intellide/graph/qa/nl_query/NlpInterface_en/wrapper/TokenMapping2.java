package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.wrapper;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.NLPToken;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.Query;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.extractmodel.ExtractModel;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.extractmodel.Graph;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.extractmodel.Vertex;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.ir.LuceneSearchResult;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.rules.SynonymJson;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphEdgeType;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphPath;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.schema.GraphSchema;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface_en.entity.TokenMapping.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TokenMapping2 {
    public static double threshold = 0.5;
    public static double thresholdEdge = 0.5;
    public static void process(Query query){
        /* 全文匹配，疑问词匹配 ，之后改成模糊匹配，*
        需要记录下AST/
         */
        GraphSchema graphSchema = ExtractModel.getSingle().graphSchema;
        Graph graph = ExtractModel.getSingle().graph;
        for (NLPToken token : query.tokens){
            /*先schema匹配，后实体匹配*/
            if (token.mapping != null) continue;
            for (String edgeTypeName : graphSchema.edgeTypes.keySet()){
                double similar = isSimilar(token.text, edgeTypeName, "relation");
                if (similar > thresholdEdge  && ((token.mapping == null)||(token.mapping != null /*&& similar > token.mapping.score*/) )){
                    NLPMapping mapping = new NLPEdgeSchemaMapping(edgeTypeName,null, token, similar);
                    if (token.mapping == null){
                        token.mappingList.add(mapping);
                        token.mapping = mapping;
                        continue;
                    }
                    if (similar < token.mapping.score + 0.01){
                        token.mappingList.add(mapping);
                        continue;
                    }
                    //token.mappingList.clear();
                    token.mapping = mapping;
                    token.mappingList.add(mapping);
                    //break;
                }
            }
            //if (token.mapping != null) continue;
            for (String pathName : graphSchema.paths.keySet()){
                double similar = isSimilar(token.text, pathName,"relation");
                if (similar > thresholdEdge  && ((token.mapping == null)||(token.mapping != null /*&& similar > token.mapping.score*/) )){
                    NLPMapping mapping = new NLPPathSchemaMapping(pathName,null, token, similar);
                    if (token.mapping == null){
                        token.mappingList.add(mapping);
                        token.mapping = mapping;
                        continue;
                    }
                    if (similar < token.mapping.score + 0.01){
                        token.mappingList.add(mapping);
                        continue;
                    }
                    //token.mappingList.clear();
                    token.mapping = mapping;
                    token.mappingList.add(mapping);
                    //break;
                }
            }
            //if (token.mapping != null) continue;
            for (String vertexTypeName : graphSchema.vertexTypes.keySet()){
                double similar = isSimilar(token.text, vertexTypeName,"node");
                if (similar > thresholdEdge  && ((token.mapping == null)||(token.mapping != null /*&& similar > token.mapping.score*/) )){
                    NLPMapping mapping = new NLPVertexSchemaMapping(graphSchema.vertexTypes.get(vertexTypeName),token, similar);
                    if (token.mapping == null){
                        token.mappingList.add(mapping);
                        token.mapping = mapping;
                        continue;
                    }
                    if (similar < token.mapping.score + 0.01){
                        token.mappingList.add(mapping);
                        continue;
                    }
                    //token.mappingList.clear();
                    token.mapping = mapping;
                    token.mappingList.add(mapping);
                    //break;
                }
            }
        }
//        for (NLPToken token : query.tokens){
//            if (token.mapping != null) continue;
//            for (Vertex vertex : graph.getAllVertexes()){
//                double similar = isSimilar(token.text, vertex.name);
//                if (similar > threshold  && ((token.mapping == null)||(token.mapping != null /*&& similar > token.mapping.score*/) )){
//                    boolean flag = false;
//                    for (NLPToken tok : query.tokens){
//                        if (tok.mapping != null && tok.mapping instanceof NLPEdgeSchemaMapping){
//                            for (GraphEdgeType edgeType : ExtractModel.getSingle().graphSchema.edgeTypes.get(((NLPEdgeSchemaMapping) tok.mapping).type)){
//                                if  (edgeType.start.name.equals(vertex.labels) || edgeType.end.name.equals(vertex.labels) ){
//                                    flag = true; break;
//                                }
//                            }
//                            if (flag) break;
//                        }
//                    }
//                    if (!flag) continue;
//                    NLPMapping mapping = new NLPVertexMapping(vertex,graphSchema.vertexTypes.get(vertex.labels),token, similar);
//                    //token.mapping = mapping;
//                    if (token.mapping == null){
//                        token.mappingList.add(mapping);
//                        token.mapping = mapping;
//                        continue;
//                    }
//                    if (similar < token.mapping.score + 0.01){
//                        token.mappingList.add(mapping);
//                        continue;
//                    }
//                    //token.mappingList.clear();
//                    token.mapping = mapping;
//                    token.mappingList.add(mapping);
//                    //break;
//                }
//            }
//        }
        for (NLPToken token : query.tokens){
            if (token.mapping != null) continue;
            //if (token.POS.startsWith("V")) continue;
            if (!token.POS.startsWith("N")) continue;
            for (Vertex vertex : graph.getAllVertexes()){
                double similar = isSimilar(token.text, vertex.name, "");
                String nn = vertex.name;
                if (vertex.labels.equals("Method") && nn.charAt(0) >= 'A' && nn.charAt(0) <= 'Z') continue;

                if (similar > threshold  && ((token.mapping == null)||(token.mapping != null /*&& similar > token.mapping.score - 0.01*/) )){
                    NLPMapping mapping = new NLPVertexMapping(vertex,graphSchema.vertexTypes.get(vertex.labels),token, similar);
                    if (token.mapping == null){
                        token.mappingList.add(mapping);
                        token.mapping = mapping;
                        continue;
                    }
                    if (similar < token.mapping.score + 0.01){
                        token.mappingList.add(mapping);
                        continue;
                    }
                    //token.mappingList.clear();
                    token.mapping = mapping;
                    token.mappingList.add(mapping);
                    //break;
                }
            }
        }
        for (NLPToken token : query.tokens){
            //if (token.mapping != null) continue;
            if ((token.mapping != null) && !(token.mapping instanceof NLPVertexMapping)) continue;
            for (String vertexTypeName : graphSchema.vertexTypes.keySet()){
                boolean flag = false;
                for (NLPToken tok : query.tokens){
                    if (tok.mapping != null){
                        if (tok.mapping instanceof NLPVertexSchemaMapping){ flag = true; break;}
                        if (tok.mapping instanceof NLPVertexMapping && ((NLPVertexMapping) tok.mapping).vertexType.name.equals(vertexTypeName)){ flag = true; break;}
                    }
                }
                if (!flag) continue;
                for (String attrName : graphSchema.vertexTypes.get(vertexTypeName).attrs.keySet()){
                    double similar = isSimilar(token.text, attrName,"attribute");
                    if (similar > threshold  && ((token.mapping == null)||(token.mapping != null /*&& similar > token.mapping.score*/) )){
                        NLPMapping mapping = new NLPAttributeSchemaMapping(graphSchema.vertexTypes.get(vertexTypeName),attrName,token, similar);
                        //token.mapping = mapping;
                        if (token.mapping != null && token.mapping instanceof NLPVertexMapping) token.mappingList.clear();
                        if (token.mapping == null){
                            token.mappingList.add(mapping);
                            token.mapping = mapping;
                            continue;
                        }

                        /*if (similar < token.mapping.score - 0.01){
                            token.mapping = mapping;
                            token.mappingList.add(mapping);
                            continue;
                        }*/
                        token.mapping = mapping;
                        token.mappingList.add(mapping);
                        //break;
                    }
                }
            }
        }
        GraphDatabaseService db = ExtractModel.getSingle().db;
        try (Transaction tx = db.beginTx()) {
            for (NLPToken token : query.tokens) {
                //if (token.mapping != null) continue;
                if ((token.mapping != null) && !(token.mapping instanceof NLPVertexMapping)) continue;
                //if (token.POS.startsWith("V")) continue;
                if (!token.POS.startsWith("N")) continue;
                for (Vertex vertex : graph.getAllVertexes()) {
                    boolean flag = false;
                    for (NLPToken tok : query.tokens) {
                        if (tok.mapping != null) {
                            if (tok.mapping instanceof NLPVertexSchemaMapping && ((NLPVertexSchemaMapping) tok.mapping).vertexType.name.equals(vertex.labels)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (!flag) continue;
                    Node node = db.getNodeById(vertex.id);
                    for (String attrTypeName : graphSchema.vertexTypes.get(vertex.labels).attrs.keySet()) {
                        boolean flag2 = false;
                        for (NLPToken tok : query.tokens) {
                            if (tok.mapping != null) {
                                if (tok.mapping instanceof NLPAttributeSchemaMapping && ((NLPAttributeSchemaMapping) tok.mapping).attrType.equals(attrTypeName)) {
                                    flag2 = true;
                                    break;
                                }
                            }
                        }
                        if (!flag2) continue;
                        Object obj = node.getAllProperties().get(attrTypeName);
                        if (!(obj instanceof String)) continue;
                        String attrValue = (String)obj;
                        double similar = isSimilar(token.text, attrValue, "");
                        if (similar > threshold  && ((token.mapping == null)||(token.mapping != null /*&& similar > token.mapping.score*/) )){
                            NLPMapping mapping = new NLPAttributeMapping(vertex, graphSchema.vertexTypes.get(vertex.labels), attrTypeName, attrValue, token, similar);
                            if (token.mapping == null){
                                token.mappingList.add(mapping);
                                token.mapping = mapping;
                                continue;
                            }
                            if (similar < token.mapping.score + 0.01){
                                token.mappingList.add(mapping);
                                continue;
                            }
                            //token.mappingList.clear();
                            token.mapping = mapping;
                            token.mappingList.add(mapping);
                        }
                    }
                }
            }
            tx.success();
        }
        for (NLPToken token : query.tokens){
            /*先schema匹配，后实体匹配*/

            List<NLPMapping> newlist = new ArrayList<>();
            for (NLPMapping mapping : token.mappingList){
                if (!(mapping instanceof NLPEdgeSchemaMapping)) {
                    newlist.add(mapping);
                    continue;
                }
                String edgeTypeName = ((NLPEdgeSchemaMapping)mapping).type;
                double similar = isSimilar(token.text, edgeTypeName, "relation");
                for (GraphEdgeType edgeType : graphSchema.edgeTypes.get(edgeTypeName)){
                    NLPMapping newmapping = new NLPEdgeSchemaMapping(edgeTypeName,edgeType, token, similar);
                    newlist.add(newmapping);
                    token.mapping = newmapping;
                }
            }
            token.mappingList = newlist;
        }
        for (NLPToken token : query.tokens){
            /*先schema匹配，后实体匹配*/
            List<NLPMapping> newlist = new ArrayList<>();
            for (NLPMapping mapping : token.mappingList){
                if (!(mapping instanceof NLPPathSchemaMapping)) {
                    newlist.add(mapping);
                    continue;
                }
                String edgeTypeName = ((NLPPathSchemaMapping)mapping).type;
                double similar = isSimilar(token.text, edgeTypeName, "relation");
                for (GraphPath edgeType : graphSchema.paths.get(edgeTypeName)){
                    NLPMapping newmapping = new NLPPathSchemaMapping(edgeTypeName,edgeType, token, similar);
                    newlist.add(newmapping);
                    token.mapping = newmapping;
                }
            }
            token.mappingList = newlist;
        }
        for (NLPToken token : query.tokens){
            if (token.mapping instanceof NLPVertexSchemaMapping && !(token.mapping instanceof NLPVertexMapping)){
                boolean flag = false;
                for (NLPToken token2 : query.tokens){
                    if (token2.roffset == token.roffset + 1){
                        List<NLPMapping> mappings = new ArrayList<>();
                        for (NLPMapping mapping : token2.mappingList){
                            if (mapping instanceof NLPVertexMapping){
                                if (((NLPVertexMapping)mapping).vertexType.name.equals(((NLPVertexSchemaMapping) token.mapping).vertexType.name)){
                                    flag = true;
                                    mappings.add(mapping);
                                    token2.mapping = mapping;
                                    //token2.mappingList = mappings;
                                }
                            }
                        }
                        if (flag) token2.mappingList = mappings;
                        break;
                    }
                }
                if (flag){
                    token.mappingList.clear();
                    token.mapping = null;
                    token.nomapping = true;
                }
            }
        }
        for (NLPToken token : query.tokens){
            if (token.mapping instanceof NLPNoticeMapping){
                for (NLPToken token2 : query.tokens){
                    boolean flag = false;
                    for (int off = 1; off <= 3; off++) {

                        if (token2.offset == token.offset - off) {
                            for (NLPMapping m : token2.mappingList) {
                                if (m instanceof NLPVertexSchemaMapping && !(m instanceof NLPVertexMapping)) {
                                    for (LuceneSearchResult r : ((NLPNoticeMapping) token.mapping).list) {
                                        if (((NLPVertexSchemaMapping) m).vertexType.name.equals(r.vertex_type)) {
                                            ((NLPVertexSchemaMapping) m).must = true;
                                            if (((NLPVertexSchemaMapping) m).l == null) {
                                                ((NLPVertexSchemaMapping) m).l = new ArrayList<>();
                                            }
                                            ((NLPVertexSchemaMapping) m).l.add(r);
                                        }
                                    }
                                    if (((NLPVertexSchemaMapping) m).must) {
                                        token2.mapping = m;
                                        break;
                                    }
                                }
                            }
                            if (token2.mapping != null && token2.mapping instanceof NLPVertexSchemaMapping && ((NLPVertexSchemaMapping) token2.mapping).must) {
                                token2.mappingList.clear();
                                token2.mappingList.add(token2.mapping);
                                flag = true;
                            }
                            if (flag)break;
                        }
                    }
                }
            }
        }
        /*计算score & rank*/
        for (NLPToken token : query.tokens){
            for (NLPMapping mapping : token.mappingList){
                String name = null;
                String name1 = null;
                String name2 = null;
                if (mapping instanceof NLPPathSchemaMapping){
                    name1 = ((NLPPathSchemaMapping)mapping).path.start.name;
                    name2 = ((NLPPathSchemaMapping)mapping).path.end.name;
                    name = ((NLPPathSchemaMapping)mapping).type;
                }else if (mapping instanceof NLPEdgeSchemaMapping){
                    name1 = ((NLPEdgeSchemaMapping)mapping).edgeType.start.name;
                    name2 = ((NLPEdgeSchemaMapping)mapping).edgeType.end.name;
                    name = ((NLPEdgeSchemaMapping)mapping).type;
                }else if (mapping instanceof NLPVertexMapping){
                    name = ((NLPVertexMapping)mapping).vertex.name;
                }
                else if (mapping instanceof NLPVertexSchemaMapping){
                    name = ((NLPVertexSchemaMapping)mapping).vertexType.name;
                }else if (mapping instanceof NLPAttributeSchemaMapping){
                    name = ((NLPAttributeSchemaMapping)mapping).attrType;
                }else if (mapping instanceof NLPAttributeMapping){
                    name = ((NLPAttributeMapping)mapping).attrValue;
                }else if (mapping instanceof NLPNoticeMapping) continue;
                if (name1 != null){
                    mapping.score = mapping.score + disText(name,query) * 0.4 + disText(name1,query) * 0.3 + disText(name2,query) * 0.3;
                }else
                mapping.score = mapping.score + disText(name,query);

            }

            token.mappingList.sort(Comparator.comparing(p->p.score*-1));
            //token.mappingList.sort(new NLPMapping.ComparatorUser());
            int ppp = 0;
            for (int pp = 0; pp < query.tokens.size(); pp++){
                if (query.tokens.get(pp).mappingList.size() > 0) ppp++;
            }
            int t = 200/(ppp*ppp);
            if (token.mappingList.size() > t){
                token.mappingList = token.mappingList.subList(0,t);
            }
            for (int i = 0;  i < token.mappingList.size(); i++) {
                token.mappingList.get(i).rank = i;
                if (i > 0){
                    if (Math.abs(token.mappingList.get(i).score - token.mappingList.get(i-1).score) < 0.01){
                        token.mappingList.get(i).rank = token.mappingList.get(i-1).rank;
                    }
                }
            }
        }
        for (NLPToken token : query.tokens){
            if (token.mappingList.size() == 0) token.nomapping = true;else
            if (token.mappingList.get(0) instanceof NLPNoticeMapping) token.nomapping=true;
//            else
//            if ((token.mappingList.get(0) instanceof NLPVertexSchemaMapping && !(token.mappingList.get(0) instanceof NLPVertexMapping))|| token.mappingList.get(0) instanceof NLPPathSchemaMapping ||
//                    token.mappingList.get(0) instanceof NLPAttributeSchemaMapping ||token.mappingList.get(0) instanceof NLPEdgeSchemaMapping){
//                token.mappingList = token.mappingList.subList(0,1);
//            }
        }
    }
    public static double disTextm(String str, Query query){
        double ret = 0;
        for (NLPToken token : query.tokens){
            Math.max(ret,subSimilar(str,token.text));
        }
        ret /= query.tokens.size();
        return ret;
    }
    /*计算一个token和文本的相似度*/
    public static double disText(String str, Query query){
        double ret = 0;
        for (NLPToken token : query.tokens){
            ret += isSimilar(str,token.text,"");
        }
        ret /= query.tokens.size();
        return ret;
    }
    /*计算一个token和文本的相似度*/
    public static double disTextf(String str, Query query){
        double ret = 0;
        for (NLPToken token : query.tokens){
            double x = isSimilar(str,token.text,"");
            ret+=x;
            if (token.mapping instanceof NLPVertexSchemaMapping){
                x = isSimilar(str,((NLPVertexSchemaMapping)token.mapping).vertexType.name,"");
                ret+=x;
            }
            if (token.mapping instanceof NLPEdgeSchemaMapping){
                x = isSimilar(str,((NLPEdgeSchemaMapping)token.mapping).edgeType.name,"");
                ret+=x;
            }
        }
        ret /= query.tokens.size();
        return ret;
    }
    /*计算两个token的相似度，后者为NN*/
    public static double isSimilar(String str1, String str2, String type){
        if (type.equals("relation")) {
            if (SynonymJson.edgedict.containsKey(str1)) {
                if (SynonymJson.edgedict.get(str1).contains(str2)) return 1;
            }
            if (SynonymJson.nodedict.containsKey(str1)) {
                if (SynonymJson.nodedict.get(str1).contains(str2)) return 1;
            }
            for (String str : SynonymJson.edgedict.keySet()) {
                if (SynonymJson.edgedict.get(str).contains(str2)) {
                    double tmp = subSimilar(str1, str);
                    if (tmp > thresholdEdge) {
                        return tmp;
                    }
                }
            }
        }
        if (type.equals("node")) {
            for (String str : SynonymJson.nodedict.keySet()) {
                if (SynonymJson.nodedict.get(str).contains(str2)) {
                    double tmp = subSimilar(str1, str);
                    if (tmp > threshold) {
                        return tmp;
                    }
                }
            }
        }
        if (type.equals("attribute")) {
            for (String str : SynonymJson.attributedict.keySet()) {
                if (SynonymJson.attributedict.get(str).contains(str2)) {
                    double tmp = subSimilar(str1, str);
                    if (tmp > threshold) {
                        return tmp;
                    }
                }
            }
        }
        double x =  subSimilar(str1,str2);
        return x;
    }
    public static double subSimilar (String str1, String str2){

        if (str1.equals(str2)) return 1;
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        if (str1.equals(str2)) return 0.8;
        str1 = str1.replaceAll("_","");
        str2 = str2.replaceAll("_","");
        if (str1.contains(str2) && (2 * str2.length() >  str1.length())) return ((double)str2.length()) / str1.length() * 0.8;
        if (str2.contains(str1) && (2 * str1.length() >  str2.length())) return ((double)str1.length()) / str2.length() * 0.8;
        int f[][] = new int[str1.length()+1][str2.length()+1];
        for (int i = 0; i <= str1.length(); i++) f[i][0] = 0;
        for (int i = 0; i <= str2.length(); i++) f[0][i] = 0;

        for (int i = 1; i <= str1.length(); i++){
            for (int j = 1; j <= str2.length(); j++){
                if (str1.charAt(i-1) == str2.charAt(j-1)){
                    f[i][j] = Math.max(f[i-1][j],f[i][j-1]);
                    f[i][j] = Math.max(f[i-1][j-1]+1,f[i][j]);
                }else f[i][j] = Math.max(f[i-1][j],f[i][j-1]);
            }
        }
        if (f[str1.length()][str2.length()]*3 < Math.min(str1.length()+1,str2.length()+1)) return 0;
        return 0.7 * f[str1.length()][str2.length()] / Math.max(str1.length()+1,str2.length()+1) ;
    }
    public static boolean isUnDirect(String str){
        if (str.endsWith("ed")){
            return true;
        }
        return false;
    }
}
