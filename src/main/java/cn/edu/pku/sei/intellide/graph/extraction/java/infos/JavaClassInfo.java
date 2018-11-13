package cn.edu.pku.sei.intellide.graph.extraction.java.infos;

import cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.Map;

public class JavaClassInfo {

    @Getter
    private final String name;
    @Getter
    private final String fullName;
    @Getter
    private final boolean isInterface;
    @Getter
    private final String visibility;
    @Getter
    private final boolean isAbstract;
    @Getter
    private final boolean isFinal;
    @Getter
    private final String comment;
    @Getter
    private final String content;

    @Getter
    private final String superClassType;
    @Getter
    private final String superInterfaceTypes;
    @Getter
    private long nodeId;

    public JavaClassInfo(BatchInserter inserter, String name, String fullName, boolean isInterface, String visibility, boolean isAbstract, boolean isFinal, String comment, String content, String superClassType, String superInterfaceTypes) {
        Preconditions.checkArgument(name != null);
        this.name = name;
        Preconditions.checkArgument(fullName != null);
        this.fullName = fullName;
        this.isInterface = isInterface;
        Preconditions.checkArgument(visibility != null);
        this.visibility = visibility;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        Preconditions.checkArgument(comment != null);
        this.comment = comment;
        Preconditions.checkArgument(content != null);
        this.content = content;
        Preconditions.checkArgument(superClassType != null);
        this.superClassType = superClassType;
        Preconditions.checkArgument(superInterfaceTypes != null);
        this.superInterfaceTypes = superInterfaceTypes;
        nodeId = createNode(inserter);
    }

    private long createNode(BatchInserter inserter) {
        Map<String, Object> map = new HashMap<>();
        map.put(JavaExtractor.NAME, name);
        map.put(JavaExtractor.FULLNAME, fullName);
        map.put(JavaExtractor.IS_INTERFACE, isInterface);
        map.put(JavaExtractor.VISIBILITY, visibility);
        map.put(JavaExtractor.IS_ABSTRACT, isAbstract);
        map.put(JavaExtractor.IS_FINAL, isFinal);
        map.put(JavaExtractor.COMMENT, comment);
        map.put(JavaExtractor.CONTENT, content);
        return inserter.createNode(map, JavaExtractor.CLASS);
    }

}