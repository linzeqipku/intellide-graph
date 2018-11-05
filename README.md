# 软件项目知识图谱的自动构建与智能问答

## 编译构建

本系统的依赖环境包括：

1. JRE 1.8+
2. [Node.js](https://www.npmjs.com/)
3. Python 3 (optional)

可以使用maven从源代码开始进行编译构建：

```
mvn package
```

或者可以使用已编译好的jar包:

```
链接：https://pan.baidu.com/s/1uDLKt56avxbjjByAyXZskQ 
提取码：irdj
```

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
    graphDir: E:/graph.db  # 知识图谱的输出文件夹路径，如果需要中文支持，该路径需要由"-chinese"来结尾
    
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
     java -jar intellide-graph.jar -gen {yml_config_path}
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
    
    编辑intellide-graph.jar中的BOOT-INF/classes/application.properties文件，例如：
    
    ```
    server.port=8004
    
    graphDir= E:/graphs/
    dataDir= E:/tmp/  # 临时文件存储路径
    infoDir= E:/graphs/graphs.json  #知识图谱描述文件
    ```
    
2. 运行如下命令，启动后端服务器：

    ```
    java -Xms1024m -Xmx4096m -XX:MaxPermSize=2048m -XX:MaxNewSize=2048m -jar intellide-graph.jar -exec
    ```
    
3. 启动前端服务器

    前端项目：[woooking/snowview](https://github.com/woooking/snowview) (intelli-graph branch)
    
    在```src/config.ts```中配置后端服务器的URL
    
    编译项目：```npm install```
    
    启动前端服务器：```npm start```
    
4. 浏览器访问：

    ```
    http://localhost:3000/
    ```

## 基本使用方法

- Onlie Demo: [http://106.75.143.22:3000/](http://106.75.143.22:3000/)

- 选择感兴趣的软件项目，并进入其知识图谱主页

    ![](https://github.com/linzeqipku/intellide-graph/raw/master/docs/figures/start.gif)
    
    - 进入 http://localhost:3000/ 后，首先看到的是简介页面；
    - 点击页面左侧的“use it”标签，可以看到目前已经部署的所有软件项目知识图谱的列表；
    - 点击某个项目，进入其知识图谱主页；
    - 进入知识图谱主页后，首先看到的是该知识图谱的基本统计信息，即：各种类型的实体分别有多少个，各种类型的关联关系分别有多少条；
    - 知识图谱主页上显示了一张弦图来可视化这些基本统计信息：圆周上的每条弧代表一种类型的实体的数量，弧之间的弦代表两种实体之间的关联关系数量.
    
- 智能代码搜索

    ![](https://github.com/linzeqipku/intellide-graph/raw/master/docs/figures/api_search.gif)
    
    - 点击页面上方的“智能问答”标签，进入智能代码搜索页面；
    - 在搜索框中输入自然语言查询语句，系统会帮你找到与之相关的代码元素（类、接口、方法等），并给出它们之间的依赖关系图；
    - 默认支持英文查询，但对于中文的知识图谱（以"-chinese"作为名字后缀的知识图谱），也支持中文查询；
    - 代码搜索的基本原理：根据查询语句中的关键词与代码元素中的标识符以及注释中的关键词的匹配来找到候选的代码元素集合，之后根据它们之间的依赖关系远近选取出最合适的一个子图作为搜索结果.
    - 参考文献: Graph Embedding Based Code Search in Software Project, Internetware'18
    
- 知识图谱的可视化浏览

    ![](https://github.com/linzeqipku/intellide-graph/raw/master/docs/figures/surf.gif)
    
    - 点击某个实体结点，页面右侧会显示这些实体中包含的具体属性；
    - 可以点击这些属性，以展开其中的具体文本内容；
    - 选中实体结点之后，可以在页面的右上方选取感兴趣的关联关系类型，从而在页面中浏览与该实体结点具有这种关联关系的其它实体结点；
    - 例如，如果我想看某个类有没有相关的文档，可以选取“codeMention”这种类型的关联关系；
    - 如果待显示的新结点比较多，系统会按照内置的优先级，每次点击“expand”按钮后显示出若干个，直到全部都以及显示出来为止.
    
- 智能文档搜索

    ![](https://github.com/linzeqipku/intellide-graph/raw/master/docs/figures/doc_search.gif)
    
    - 点击页面上方的“语义搜索”标签，进入智能文档搜索页面；
    - 在搜索框中输入自然语言查询语句，系统会帮你找到与之相关的文档.

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

由于C#和Java同为面向对象编程语言，其成分基本一致，无需给C#设计一套新的数据模型，直接使用这套Java的数据模型即可.
因此，我们的主要任务是对C#源代码数据进行静态分析，按照这一数据模型抽取出实体和关联关系，并将其写入到图数据库中.

在实现JavaExtractor时，我们使用Eclipse AST Parser对Java源代码进行解析.
然而，目前还没有比较成熟的可以解析C#源代码的Java工具包可以直接拿来用.
一种妥协方案是：可以先在外部使用其它语言的C# Parser（如[C# Parser and CodeDOM](http://www.inevitablesoftware.com/Products.aspx)）对C#源代码进行解析，并将抽取出来的实体与关联关系存储为json文件；CSharpExtractor读取该json文件，并将其内容写入到neo4j图数据库中.
