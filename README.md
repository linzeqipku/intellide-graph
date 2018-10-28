# 软件项目知识图谱的自动构建与智能问答

## 数据准备

1. Java源代码数据

    将整个项目的源代码统一解压到一个文件夹中即可.

2. git版本库数据

    给出该项目的.git文件夹即可.
    
3. html文档数据

    统一放在同一个文件夹中即可.
    对于docx文档，可以使用[此python脚本](https://gist.github.com/linzeqipku/3cec0b90e9e51445a2ffc5e15cdf4ae0)将其预处理为html格式.
    
4. pptx演示文稿数据

    统一放在同一个文件夹中即可.
    
## 自动构建知识图谱

1. 编写yaml配置文件

    在任意目录中新建一个yml文件，在该文件中配置：(1)知识图谱的输出文件夹路径；(2)需要允许哪些知识抽取模块；(3)这些知识抽取模块所输入的源数据的路径.
    配置文件的示例如下：
    
    ```
    graphDir: E:/graph.db  # 知识图谱的输出文件夹路径
    
    # 依次执行如下数据解析插件
    cn.edu.pku.sei.intellide.graph.extraction.java.JavaExtractor: E:/data/src
    cn.edu.pku.sei.intellide.graph.extraction.git.GitExtractor: E:/data/.git
    cn.edu.pku.sei.intellide.graph.extraction.html.HtmlExtractor: E:/data/html
    cn.edu.pku.sei.intellide.graph.extraction.pptx.PptxExtractor: E:/data/pptx
    
    # 依次执行如下知识关联与挖掘插件
    cn.edu.pku.sei.intellide.graph.extraction.tokenization.TokenExtractor:
    cn.edu.pku.sei.intellide.graph.extraction.code_mention.CodeMentionExtractor:
    cn.edu.pku.sei.intellide.graph.extraction.doc_link.DocLinkExtractor:
    ```
    
 2. 运行如下命令，自动生成知识图谱
 
     ```
     java -Dfile.encoding=utf-8 -jar intellide-graph.jar -g {yml_config_path}
     ```
     
     运行完毕之后，可以在配置文件中所指定的输出文件夹路径中生成neo4j图数据库格式的知识图谱.
     
## 启动web服务

1. 知识图谱准备

    将所有需要运行的知识图谱文件夹放到统一的一个文件目录下，例如：
    
    ```
    E:/graphs/lucene
    E:/graphs/jfreechart
    E:/graphs/poi
    ...
    ```
    
    在任意目录中新建一个json文件，描述这些知识图谱，例如：
    
    ```
    [
        {"name": "lucene", "description": "apache-lucene, a java library for text indexing"},
        {"name": "jfreechart", "description": "jfreechart, a java library for drawing diagrams"},
        {"name": "poi", "description": "apache-poi, a java library for editing Microsoft Office files"}
    ]
    ```
    
    编辑intellide-graph.jar中的application.yml文件，例如：
    
    ```
    server:
        graphDir: E:/graphs/
        dataDir: E:/tmp/  # 临时文件存储路径
        infoDir: E:/graphs/graphs.json  #知识图谱描述文件
    ```
    
2. 运行如下命令，启动后端服务器：

    ```
    java -Xms1024m -Xmx4096m -XX:MaxPermSize=2048m -XX:MaxNewSize=2048m -Dfile.encoding=utf-8 -jar intellide-graph.jar -e
    ```
    
3. 启动前端服务器

    前端项目：[woooking/snowview](https://github.com/woooking/snowview) (intelli-graph branch)
    
    编译项目：```npm install```
    
    启动前端服务器：```npm start```
    
4. 浏览器访问：

    ```
    http://localhost:3000/
    ```

## 对更多数据格式的解析支持（以C#源代码数据为例）

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
