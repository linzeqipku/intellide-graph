package cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.infos;

import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.Map;

public class JavaClassInfo {

    @Getter private final String name;
    @Getter private final String fullName;
    @Getter private final boolean isInterface;
    @Getter private final String visibility;
    @Getter private final boolean isAbstract;
    @Getter private final boolean isFinal;
    @Getter private final String comment;
    @Getter private final String content;

    @Getter private final String superClassType;
    @Getter private final String superInterfaceTypes;
    @Getter private long nodeId;

    public JavaClassInfo(BatchInserter inserter, String name, String fullName, boolean isInterface, String visibility, boolean isAbstract, boolean isFinal, String comment, String content, String superClassType, String superInterfaceTypes) {
        Preconditions.checkArgument(name!=null);
        this.name = name;
        Preconditions.checkArgument(fullName!=null);
        this.fullName = fullName;
        this.isInterface = isInterface;
        Preconditions.checkArgument(visibility!=null);
        this.visibility = visibility;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        Preconditions.checkArgument(comment!=null);
        this.comment = comment;
        Preconditions.checkArgument(content!=null);
        this.content = content;
        Preconditions.checkArgument(superClassType!=null);
        this.superClassType = superClassType;
        Preconditions.checkArgument(superInterfaceTypes!=null);
        this.superInterfaceTypes = superInterfaceTypes;
        nodeId=createNode(inserter);
    }

    private long createNode(BatchInserter inserter){
        Map<String,Object> map=new HashMap<>();
        map.put(JavaCodeGraphBuilder.NAME,name);
        map.put(JavaCodeGraphBuilder.FULLNAME,fullName);
        map.put(JavaCodeGraphBuilder.IS_INTERFACE,isInterface);
        map.put(JavaCodeGraphBuilder.VISIBILITY,visibility);
        map.put(JavaCodeGraphBuilder.IS_ABSTRACT,isAbstract);
        map.put(JavaCodeGraphBuilder.IS_FINAL,isFinal);
        map.put(JavaCodeGraphBuilder.COMMENT,comment);
        map.put(JavaCodeGraphBuilder.CONTENT,content);
        return inserter.createNode(map, JavaCodeGraphBuilder.CLASS);
    }

    public String getFullName() {
        return fullName;
    }

    public String getSuperClassType() {
        return superClassType;
    }

    public String getSuperInterfaceTypes() {
        return superInterfaceTypes;
    }

    public long getNodeId() {
        return nodeId;
    }
}