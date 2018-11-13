package cn.edu.pku.sei.intellide.graph.extraction.java.infos;

import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class JavaProjectInfo {

    private Map<String, JavaClassInfo> classInfoMap = new HashMap<>();
    private Map<String, JavaMethodInfo> methodInfoMap = new HashMap<>();
    private Map<String, JavaFieldInfo> fieldInfoMap = new HashMap<>();

    private Map<IMethodBinding, JavaMethodInfo> methodBindingMap = new HashMap<>();

    public void addClassInfo(JavaClassInfo info) {
        classInfoMap.put(info.getFullName(), info);
    }

    public void addMethodInfo(JavaMethodInfo info) {
        methodInfoMap.put(info.getFullName(), info);
        methodBindingMap.put(info.getMethodBinding(), info);
    }

    public void addFieldInfo(JavaFieldInfo info) {
        fieldInfoMap.put(info.getFullName(), info);
    }

    public void parseRels(BatchInserter inserter) {
        methodInfoMap.values().forEach(info -> methodBindingMap.put(info.getMethodBinding(), info));
        classInfoMap.values().forEach(classInfo -> {
            findJavaClassInfo(classInfo.getSuperClassType()).forEach(superClassInfo -> inserter.createRelationship(classInfo.getNodeId(), superClassInfo.getNodeId(), JavaExtractor.EXTEND, new HashMap<>()));
            findJavaClassInfo(classInfo.getSuperInterfaceTypes()).forEach(superInterfaceInfo -> inserter.createRelationship(classInfo.getNodeId(), superInterfaceInfo.getNodeId(), JavaExtractor.IMPLEMENT, new HashMap<>()));
        });
        methodInfoMap.values().forEach(methodInfo -> {
            findJavaClassInfo(methodInfo.getBelongTo()).forEach(owner -> inserter.createRelationship(owner.getNodeId(), methodInfo.getNodeId(), JavaExtractor.HAVE_METHOD, new HashMap<>()));
            findJavaClassInfo(methodInfo.getFullParams()).forEach(param -> inserter.createRelationship(methodInfo.getNodeId(), param.getNodeId(), JavaExtractor.PARAM_TYPE, new HashMap<>()));
            findJavaClassInfo(methodInfo.getFullReturnType()).forEach(rt -> inserter.createRelationship(methodInfo.getNodeId(), rt.getNodeId(), JavaExtractor.RETURN_TYPE, new HashMap<>()));
            findJavaClassInfo(methodInfo.getThrowTypes()).forEach(tr -> inserter.createRelationship(methodInfo.getNodeId(), tr.getNodeId(), JavaExtractor.THROW_TYPE, new HashMap<>()));
            findJavaClassInfo(methodInfo.getFullVariables()).forEach(var -> inserter.createRelationship(methodInfo.getNodeId(), var.getNodeId(), JavaExtractor.VARIABLE_TYPE, new HashMap<>()));
            methodInfo.getMethodCalls().forEach(call -> {
                if (methodBindingMap.containsKey(call))
                    inserter.createRelationship(methodInfo.getNodeId(), methodBindingMap.get(call).getNodeId(), JavaExtractor.METHOD_CALL, new HashMap<>());
            });
            findJavaFieldInfo(methodInfo.getFieldAccesses()).forEach(access -> inserter.createRelationship(methodInfo.getNodeId(), access.getNodeId(), JavaExtractor.FIELD_ACCESS, new HashMap<>()));
        });
        fieldInfoMap.values().forEach(fieldInfo -> {
            findJavaClassInfo(fieldInfo.getBelongTo()).forEach(owner -> inserter.createRelationship(owner.getNodeId(), fieldInfo.getNodeId(), JavaExtractor.HAVE_FIELD, new HashMap<>()));
            findJavaClassInfo(fieldInfo.getFullType()).forEach(type -> inserter.createRelationship(fieldInfo.getNodeId(), type.getNodeId(), JavaExtractor.FIELD_TYPE, new HashMap<>()));
        });
    }

    private Set<JavaClassInfo> findJavaClassInfo(String str) {
        Set<JavaClassInfo> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (classInfoMap.containsKey(token))
                r.add(classInfoMap.get(token));
        return r;
    }

    private Set<JavaFieldInfo> findJavaFieldInfo(String str) {
        Set<JavaFieldInfo> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (fieldInfoMap.containsKey(token))
                r.add(fieldInfoMap.get(token));
        return r;
    }

}
