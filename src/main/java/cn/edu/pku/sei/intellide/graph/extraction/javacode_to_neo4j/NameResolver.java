package cn.edu.pku.sei.intellide.graph.extraction.javacode_to_neo4j;

import org.eclipse.jdt.core.dom.*;

import java.util.Set;

/**
 * Evaluates fully qualified name of TypeDeclaration, Type and Name objects.
 */
public class NameResolver {

    private static Set<String> srcPathSet=null;

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

        if (t.isNameQualifiedType()){
            return ((NameQualifiedType)t).getQualifier().getFullyQualifiedName();
        }
        if (t.isPrimitiveType()){
            return ((PrimitiveType)t).toString();
        }
        if (t.isQualifiedType()){
            QualifiedType t0 = (QualifiedType) t;
            return getFullName(t0.getQualifier()) + "." + t0.getName().getIdentifier();
        }
        if (t.isSimpleType()){
            return getFullName(((SimpleType)t).getName());
        }
        if (t.isWildcardType()){
            return "? (extends|super) "+getFullName(((WildcardType)t).getBound());
        }

        if (t.isParameterizedType()) {
            ParameterizedType t0=((ParameterizedType) t);
            String s=getFullName(t0.getType())+"<";
            for (Object type:t0.typeArguments())
                s+=getFullName((Type)type)+",";
            return s.substring(0,s.length()-1)+">";
        }
        if (t.isUnionType()) {
            UnionType t0=(UnionType)t;
            String s="";
            for (Object type:t0.types())
                s+=getFullName((Type)type)+"|";
            return s.substring(0,s.length()-1);
        }
        if (t.isIntersectionType()){
            IntersectionType t0=(IntersectionType)t;
            String s="";
            for (Object type:t0.types())
                s+=getFullName((Type)type)+"&";
            return s.substring(0,s.length()-1);
        }
        if (t.isArrayType()){
            return getFullName(((ArrayType)t).getElementType())+"[]";
        }
        return "";
    }

    /**
     * Evaluates fully qualified name of the Name object.
     */
    private static String getFullName(Name name) {
        // check if the root node is a CompilationUnit
        if (name.getRoot().getClass() != CompilationUnit.class) {
            // cannot resolve a full name, CompilationUnit root node is missing
            return name.getFullyQualifiedName();
        }
        // get the root node
        CompilationUnit root = (CompilationUnit) name.getRoot();
        // check if the name is declared in the same file
        TypeDeclVisitor tdVisitor = new TypeDeclVisitor(name.getFullyQualifiedName());
        root.accept(tdVisitor);
        if (tdVisitor.getFound()) {
            // the name is the use of the TypeDeclaration in the same file
            return getFullName(tdVisitor.getTypeDecl());
        }
        // check if the name is declared in the same package or imported
        PckgImprtVisitor piVisitor = new PckgImprtVisitor(name.getFullyQualifiedName());
        root.accept(piVisitor);
        if (piVisitor.getFound()) {
            // the name is declared in the same package or imported
            return piVisitor.getFullName();
        }
        // could be a class from the java.lang (String) or a param name (T, E,...)
        return name.getFullyQualifiedName();
    }

	public static void setSrcPathSet(Set<String> srcPathSet) {
		NameResolver.srcPathSet = srcPathSet;
	}

	private static class PckgImprtVisitor extends ASTVisitor {
        private boolean found = false;
        private String fullName;
        private String name;
        private String[] nameParts;

        PckgImprtVisitor(String aName) {
            super();
            name = aName;
            nameParts = name.split("\\.");
        }

        private void checkInDir(String dirName) {
            String name=dirName+"."+nameParts[0] + ".java";
            for (String fileName:srcPathSet){
            	fileName=fileName.replace("\\", ".").replace("/", ".");
            	if (fileName.contains(name)){
            		fullName = dirName;
                    for (String namePart : nameParts) {
                        fullName += "." + namePart;
                    }
                    found = true;
            	}
            }
        }

        public boolean visit(PackageDeclaration node) {
            String pckgName = node.getName().getFullyQualifiedName();
            checkInDir(pckgName);
            return true;
        }

        public boolean visit(ImportDeclaration node) {
            if (node.isOnDemand()) {
                String pckgName = node.getName().getFullyQualifiedName();
                checkInDir(pckgName);
            } else {
                String importName = node.getName().getFullyQualifiedName();
                if (importName.endsWith("." + nameParts[0])) {
                    fullName = importName;
                    for (int i = 1; i < nameParts.length; i++) {
                        fullName += "." + nameParts[i];
                    }
                    found = true;
                }
            }
            return true;
        }

        boolean getFound() {
            return found;
        }

        String getFullName() {
            return fullName;
        }

    }

    private static class TypeDeclVisitor extends ASTVisitor {
        private boolean found = false;
        private TypeDeclaration typeDecl;
        private String name;

        TypeDeclVisitor(String aName) {
            super();
            name = aName;
        }

        public boolean visit(TypeDeclaration node) {
            if (getFullName(node).endsWith("." + name)) {
                found = true;
                typeDecl = node;
            }
            return true;
        }

        boolean getFound() {
            return found;
        }

        TypeDeclaration getTypeDecl() {
            return typeDecl;
        }
    }
}
