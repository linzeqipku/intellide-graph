package cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.infos;

import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.Map;

public class JavaFieldInfo {

    @Getter private String name;
    @Getter private String fullName;
    @Getter private String type;
    @Getter private String visibility;
    @Getter private boolean isStatic;
    @Getter private boolean isFinal;
    @Getter private String comment;

    @Getter private String belongTo;
    @Getter private String fullType;
    @Getter private long nodeId;

    public JavaFieldInfo(BatchInserter inserter, String name, String fullName, String type, String visibility, boolean isStatic, boolean isFinal, String comment, String belongTo, String fullType) {
        Preconditions.checkArgument(name!=null);
        this.name = name;
        Preconditions.checkArgument(fullName!=null);
        this.fullName = fullName;
        Preconditions.checkArgument(type!=null);
        this.type = type;
        Preconditions.checkArgument(visibility!=null);
        this.visibility = visibility;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        Preconditions.checkArgument(comment!=null);
        this.comment = comment;
        Preconditions.checkArgument(belongTo!=null);
        this.belongTo = belongTo;
        Preconditions.checkArgument(fullType!=null);
        this.fullType = fullType;
        nodeId=createNode(inserter);
    }

    private long createNode(BatchInserter inserter){
        Map<String,Object> map=new HashMap<>();
        map.put(JavaCodeGraphBuilder.NAME,name);
        map.put(JavaCodeGraphBuilder.FULLNAME,fullName);
        map.put(JavaCodeGraphBuilder.TYPE_STR,type);
        map.put(JavaCodeGraphBuilder.VISIBILITY,visibility);
        map.put(JavaCodeGraphBuilder.IS_STATIC,isStatic);
        map.put(JavaCodeGraphBuilder.IS_FINAL,isFinal);
        map.put(JavaCodeGraphBuilder.COMMENT,comment);
        return inserter.createNode(map, JavaCodeGraphBuilder.FIELD);
    }

    public String getFullName() {
        return fullName;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public String getFullType() {
        return fullType;
    }

    public long getNodeId() {
        return nodeId;
    }
}