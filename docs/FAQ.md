# FAQ

- [构建与部署](#install)

- [开发者相关](#developer)

<a name="install"></a>
## 构建与部署

- **项目使用SVN做的版本控制，应该如何处理？**

    将SVN仓库迁移成git仓库后进行解析：```git svn clone {svn_url}```

- **打开页面后出现诸如```TS1005```等TypeScript编译错误提示.**

    原因：部署环境中已存在其它版本的TypeScript，与本系统所使用的TypeScript发生版本冲突.
    
    解决方法：卸载部署环境中已存在的其它版本的TypeScript.

<a name="developer"></a>
## 开发者相关

- **如何配置开发环境.**

    - [IntelliJ IDEA](https://www.jetbrains.com/idea/)
    - [Lombok插件](https://projectlombok.org/)

- **对更多数据格式的解析支持（以C#源代码数据为例）.**

    有时，我们需要提供对更多类型的数据格式的支持. 例如，对于源代码，当前只支持解析Java，暂时无法解析C#源代码数据.

    因此，我们需要实现一个新的[KnowledgeExtractor](https://github.com/linzeqipku/intellide-graph/blob/enterprise/src/main/java/cn/edu/pku/sei/intellide/graph/extraction/KnowledgeExtractor.java)，来支持对C#源代码数据的解析.

    [JavaExtractor](https://github.com/linzeqipku/intellide-graph/blob/enterprise/src/main/java/cn/edu/pku/sei/intellide/graph/extraction/java/JavaExtractor.java)是目前已经实现了的用于解析Java源代码的KnowledgeExtractor，可以参考它来实现类似的CSharpExtractor.

    对KnowledgeExtractor的解释：

    - KnowledgeExtractor的任务是解析来自```getDataDir()```目录的源数据，并将抽取出的实体以及关联关系加入到目标Neo4j图数据库中. 这一任务主要通过```extraction()```方法来完成.
    - 若```isBatchInsert() == false```，则目标Neo4j图数据库的连接对象应该通过```getDb()```来取得；否则，应该通过```getInserter()```来取得. 这是两种不同的通过java代码来操作neo4j图数据库的方式. 前者符合数据库的事务规范，且支持的读写功能更丰富，但写入速度较慢；后者无视事务规范且功能简单，但胜在写入速度极快. 一般我们建议使用后者来进行图数据操作.

    为了实现一个KnowledgeExtractor，首先我们需要定义数据模型. 例如，在JavaExtractor中，我们这么定义数据模型：

    ```
    public static final Label CLASS = Label.label("Class");  //定义一种类型的实体：类/接口
    public static final Label METHOD = Label.label("Method");  //定义一种类型的实体：方法
    public static final Label FIELD = Label.label("Field");  //定义一种类型的实体：域
    
    public static final RelationshipType EXTEND = RelationshipType.withName("extend");  //  定义一种类型的关联关系：类到类或者接口到接口之间的继承关系
    public static final RelationshipType IMPLEMENT = RelationshipType.withName("implement");  //  类到接口的实现关系
    public static final RelationshipType HAVE_METHOD = RelationshipType.withName("haveMethod");  //  类/接口到方法的拥有关系
    public static final RelationshipType PARAM_TYPE = RelationshipType.withName("paramType");  //  方法到类/接口的输入参数类型关系
    public static final RelationshipType RETURN_TYPE = RelationshipType.withName("returnType");  //  方法到类/接口的返回类型关系
    public static final RelationshipType THROW_TYPE = RelationshipType.withName("throwType");  //  方法到类/接口的异常抛出关系
    public static final RelationshipType METHOD_CALL = RelationshipType.withName("methodCall");  //  方法到方法的调用关系
    public static final RelationshipType VARIABLE_TYPE = RelationshipType.withName("variableType");  //  方法到类/接口的创建对象关系
    public static final RelationshipType HAVE_FIELD = RelationshipType.withName("haveField");  //  类/接口到域的拥有关系
    public static final RelationshipType FIELD_TYPE = RelationshipType.withName("fieldType");  //  域到类/接口的类型关系
    public static final RelationshipType FIELD_ACCESS = RelationshipType.withName("fieldAccess");  //  方法到域的依赖关系
    
    //定义实体/关联关系中的具体属性
    public static final String NAME = "name";  //  类名/接口名/方法名/域名，都是simple name
    public static final String FULLNAME = "fullName";  // 全名
    public static final String IS_INTERFACE = "isInterface";  //  Class实体的属性，表示它是类还是接口
    //以下顾名思义，不一一介绍
    public static final String VISIBILITY = "visibility";
    public static final String IS_ABSTRACT = "isAbstract";
    public static final String IS_FINAL="isFinal";
    public static final String COMMENT="comment";
    public static final String CONTENT="content";
    public static final String RETURN_TYPE_STR="returnType";
    public static final String TYPE_STR="type";
    public static final String PARAM_TYPE_STR="paramType";
    public static final String IS_CONSTRUCTOR="isConstructor";
    public static final String IS_STATIC="isStatic";
    public static final String IS_SYNCHRONIZED="isSynchronized";
    ```

    由于C#和Java同为面向对象编程语言，其成分基本一致，无需给C#设计一套新的数据模型，直接使用这套Java的数据模型即可.
    因此，我们的主要任务是对C#源代码数据进行静态分析，按照这一数据模型抽取出实体和关联关系，并将其写入到图数据库中.

    在实现JavaExtractor时，我们使用Eclipse AST Parser对Java源代码进行解析.
    然而，目前还没有比较成熟的可以解析C#源代码的Java工具包可以直接拿来用.
    一种妥协方案是：可以先在外部使用其它语言的C# Parser（如[Roslyn](https://github.com/dotnet/roslyn/wiki/Getting-Started-C%23-Semantic-Analysis)）对C#源代码进行解析，并将抽取出来的实体与关联关系存储为json文件；CSharpExtractor读取该json文件，并将其内容写入到neo4j图数据库中.
