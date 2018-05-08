package cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.infos;

import cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JavaMethodInfo {

    @Getter private String name;
    @Getter private String fullName;
    @Getter private String returnType;
    @Getter private String visibility;
    @Getter private boolean isConstruct;
    @Getter private boolean isAbstract;
    @Getter private boolean isFinal;
    @Getter private boolean isStatic;
    @Getter private boolean isSynchronized;
    @Getter private String content;
    @Getter private String comment;
    @Getter private String params;

    @Getter private IMethodBinding methodBinding;
    @Getter private String fullReturnType;
    @Getter private String belongTo;
    @Getter private String fullParams;
    @Getter private String fullVariables;
    @Getter private Set<IMethodBinding> methodCalls;
    @Getter private String fieldAccesses;
    @Getter private String throwTypes;
    @Getter private long nodeId;

    public JavaMethodInfo(BatchInserter inserter, String name, String fullName, String returnType, String visibility, boolean isConstruct, boolean isAbstract,
                          boolean isFinal, boolean isStatic, boolean isSynchronized, String content, String comment, String params, IMethodBinding methodBinding,
                          String fullReturnType, String belongTo, String fullParams, String fullVariables, Set<IMethodBinding> methodCalls, String fieldAccesses, String throwTypes) {
        Preconditions.checkArgument(name!=null);
        this.name = name;
        Preconditions.checkArgument(fullName!=null);
        this.fullName = fullName;
        Preconditions.checkArgument(returnType!=null);
        this.returnType = returnType;
        Preconditions.checkArgument(visibility!=null);
        this.visibility = visibility;
        this.isConstruct = isConstruct;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isStatic = isStatic;
        this.isSynchronized = isSynchronized;
        Preconditions.checkArgument(content!=null);
        this.content = content;
        Preconditions.checkArgument(comment!=null);
        this.comment = comment;
        Preconditions.checkArgument(params!=null);
        this.params = params;
        Preconditions.checkArgument(methodBinding!=null);
        this.methodBinding = methodBinding;
        Preconditions.checkArgument(fullReturnType!=null);
        this.fullReturnType = fullReturnType;
        Preconditions.checkArgument(belongTo!=null);
        this.belongTo = belongTo;
        Preconditions.checkArgument(fullParams!=null);
        this.fullParams = fullParams;
        Preconditions.checkArgument(fullVariables!=null);
        this.fullVariables = fullVariables;
        Preconditions.checkArgument(methodCalls!=null);
        this.methodCalls = methodCalls;
        Preconditions.checkArgument(fieldAccesses!=null);
        this.fieldAccesses = fieldAccesses;
        Preconditions.checkArgument(throwTypes!=null);
        this.throwTypes = throwTypes;
        nodeId=createNode(inserter);
}

    private long createNode(BatchInserter inserter){
        Map<String,Object> map=new HashMap<>();
        map.put(JavaCodeGraphBuilder.NAME, name);
        map.put(JavaCodeGraphBuilder.FULLNAME, fullName);
        map.put(JavaCodeGraphBuilder.RETURN_TYPE_STR, returnType);
        map.put(JavaCodeGraphBuilder.VISIBILITY, visibility);
        map.put(JavaCodeGraphBuilder.IS_CONSTRUCTOR, isConstruct);
        map.put(JavaCodeGraphBuilder.IS_ABSTRACT, isAbstract);
        map.put(JavaCodeGraphBuilder.IS_STATIC, isStatic);
        map.put(JavaCodeGraphBuilder.IS_FINAL, isFinal);
        map.put(JavaCodeGraphBuilder.IS_SYNCHRONIZED, isSynchronized);
        map.put(JavaCodeGraphBuilder.CONTENT, content);
        map.put(JavaCodeGraphBuilder.COMMENT, comment);
        map.put(JavaCodeGraphBuilder.PARAM_TYPE_STR, params);
        return inserter.createNode(map, JavaCodeGraphBuilder.METHOD);
    }

    public String getFullName() {
        return fullName;
    }

    public IMethodBinding getMethodBinding() {
        return methodBinding;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public long getNodeId() {
        return nodeId;
    }

    public String getFullParams() {
        return fullParams;
    }

    public String getFullReturnType() {
        return fullReturnType;
    }

    public String getThrowTypes() {
        return throwTypes;
    }

    public String getFullVariables() {
        return fullVariables;
    }

    public Set<IMethodBinding> getMethodCalls() {
        return methodCalls;
    }

    public String getFieldAccesses() {
        return fieldAccesses;
    }
}