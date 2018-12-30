package cn.edu.pku.sei.intellide.graph.extraction.java;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.java.infos.JavaProjectInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 解析java源代码，抽取出代码实体以及这些代码实体之间的静态依赖关系，并将它们存储到neo4j图数据库中。
 * <p>
 * Class实体示例：
 * name: UnixStat
 * fullName: zstorg.apache.tools.zip.UnixStat
 * content, comment, isAbstract, isFinal, isInterface, visibility
 * <p>
 * Method实体示例：
 * name: error
 * fullName: cn.edu.pku.sei.tsr.service.ras.util.ZipGenerator.error( String msg, boolean quit )
 * paramType: String msg, boolean quit
 * returnType: void
 * content, comment, isAbstract, isConstructor, isFinal, isStatic, isSynchronized, visibility
 * <p>
 * Field实体示例：
 * name: STRATEGY_ASSIGN
 * fullName: cn.edu.pku.sei.tsr.entity.ConfigurationItem.STRATEGY_ASSIGN
 * isFinal, isStatic, type, visibility
 */

@Slf4j
public class JavaExtractor extends KnowledgeExtractor {

    public static final Label CLASS = Label.label("Class");
    public static final Label METHOD = Label.label("Method");
    public static final Label FIELD = Label.label("Field");
    public static final RelationshipType EXTEND = RelationshipType.withName("extend");
    public static final RelationshipType IMPLEMENT = RelationshipType.withName("implement");
    public static final RelationshipType HAVE_METHOD = RelationshipType.withName("haveMethod");
    public static final RelationshipType PARAM_TYPE = RelationshipType.withName("paramType");
    public static final RelationshipType RETURN_TYPE = RelationshipType.withName("returnType");
    public static final RelationshipType THROW_TYPE = RelationshipType.withName("throwType");
    public static final RelationshipType METHOD_CALL = RelationshipType.withName("methodCall");
    public static final RelationshipType VARIABLE_TYPE = RelationshipType.withName("variableType");
    public static final RelationshipType HAVE_FIELD = RelationshipType.withName("haveField");
    public static final RelationshipType FIELD_TYPE = RelationshipType.withName("fieldType");
    public static final RelationshipType FIELD_ACCESS = RelationshipType.withName("fieldAccess");
    public static final String NAME = "name";
    public static final String FULLNAME = "fullName";
    public static final String IS_INTERFACE = "isInterface";
    public static final String VISIBILITY = "visibility";
    public static final String IS_ABSTRACT = "isAbstract";
    public static final String IS_FINAL = "isFinal";
    public static final String COMMENT = "comment";
    public static final String CONTENT = "content";
    public static final String RETURN_TYPE_STR = "returnType";
    public static final String TYPE_STR = "type";
    public static final String PARAM_TYPE_STR = "paramType";
    public static final String IS_CONSTRUCTOR = "isConstructor";
    public static final String IS_STATIC = "isStatic";
    public static final String IS_SYNCHRONIZED = "isSynchronized";

    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {

        JavaProjectInfo javaProjectInfo = new JavaProjectInfo();
        Collection<File> javaFiles = FileUtils.listFiles(new File(this.getDataDir()), new String[]{"java"}, true);
        Set<String> srcPathSet = new HashSet<>();
        Set<String> srcFolderSet = new HashSet<>();
        for (File javaFile : javaFiles) {
            String srcPath = javaFile.getAbsolutePath();
            String srcFolderPath = javaFile.getParentFile().getAbsolutePath();
            srcPathSet.add(srcPath);
            srcFolderSet.add(srcFolderPath);
        }
        String[] srcPaths = new String[srcPathSet.size()];
        srcPathSet.toArray(srcPaths);

        String[] srcFolderPaths = new String[srcFolderSet.size()];
        srcFolderSet.toArray(srcFolderPaths);

        BatchInserter inserter = this.getInserter();

        ASTParser parser = ASTParser.newParser(AST.JLS10);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setEnvironment(null, new String[]{this.getDataDir()}, new String[]{"utf-8"}, true);
        Map<String, String> options = JavaCore.getOptions();
        options.put("org.eclipse.jdt.core.compiler.source", "1.8");
        parser.setCompilerOptions(options);
        String[] encodings = new String[srcPaths.length];
        for (int i = 0; i < srcPaths.length; i++)
            encodings[i] = "utf-8";
        parser.createASTs(srcPaths, encodings, new String[]{}, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
                try {
                    log.debug("AST parsing: " + sourceFilePath);
                    javaUnit.accept(new JavaASTVisitor(javaProjectInfo, FileUtils.readFileToString(new File(sourceFilePath), "utf-8"), inserter));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        javaProjectInfo.parseRels(this.getInserter());
    }

}


