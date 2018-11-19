package cn.edu.pku.sei.intellide.graph.extraction.java;

import org.eclipse.jdt.core.dom.*;

import java.util.Set;

/**
 * Evaluates fully qualified name of TypeDeclaration, Type and Name objects.
 */
public class NameResolver {

    /**
     * Evaluates fully qualified name of the TypeDeclaration object.
     */
    public static String getFullName(TypeDeclaration decl) {
        String name = decl.getName().getIdentifier();
        ASTNode parent = decl.getParent();
        // resolve full name e.g.: A.B
        while (parent != null && parent.getClass() == TypeDeclaration.class) {
            name = ((TypeDeclaration) parent).getName().getIdentifier() + "." + name;
            parent = parent.getParent();
        }
        // resolve fully qualified name e.g.: some.package.A.B
        if (decl.getRoot().getClass() == CompilationUnit.class) {
            CompilationUnit root = (CompilationUnit) decl.getRoot();
            if (root.getPackage() != null) {
                PackageDeclaration pack = root.getPackage();
                name = pack.getName().getFullyQualifiedName() + "." + name;
            }
        }
        return name;
    }

    /**
     * Evaluates fully qualified name of the Type object.
     */
    public static String getFullName(Type t) {
        if (t == null)
            return "";
        ITypeBinding binding = t.resolveBinding();
        if (binding != null){
            return binding.getQualifiedName();
        }
        if (t.isNameQualifiedType()) {
            return ((NameQualifiedType) t).getQualifier().getFullyQualifiedName();
        }
        if (t.isPrimitiveType()) {
            return t.toString();
        }
        if (t.isQualifiedType()) {
            QualifiedType t0 = (QualifiedType) t;
            return getFullName(t0.getQualifier()) + "." + t0.getName().getIdentifier();
        }
        if (t.isSimpleType()) {
            return ((SimpleType) t).getName().getFullyQualifiedName();
        }
        if (t.isWildcardType()) {
            return "? (extends|super) " + getFullName(((WildcardType) t).getBound());
        }

        if (t.isParameterizedType()) {
            ParameterizedType t0 = ((ParameterizedType) t);
            String s = getFullName(t0.getType()) + "<";
            for (Object type : t0.typeArguments())
                s += getFullName((Type) type) + ",";
            return s.substring(0, s.length() - 1) + ">";
        }
        if (t.isUnionType()) {
            UnionType t0 = (UnionType) t;
            String s = "";
            for (Object type : t0.types())
                s += getFullName((Type) type) + "|";
            return s.substring(0, s.length() - 1);
        }
        if (t.isIntersectionType()) {
            IntersectionType t0 = (IntersectionType) t;
            String s = "";
            for (Object type : t0.types())
                s += getFullName((Type) type) + "&";
            return s.substring(0, s.length() - 1);
        }
        if (t.isArrayType()) {
            return getFullName(((ArrayType) t).getElementType()) + "[]";
        }
        return "";
    }

}
