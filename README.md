# 软件项目知识图谱的自动构建与智能问答

- [基本功能概览](#user-guide)
- [构建与部署](#install)
- [FAQ](https://github.com/linzeqipku/intellide-graph/blob/master/docs/FAQ.md)

<a name="user-guide"></a>

## 基本功能概览

- Onlie Demo: [http://106.75.143.22:3000/](http://106.75.143.22:3000/)

- 选择感兴趣的软件项目，并进入其知识图谱主页

    ![](https://github.com/linzeqipku/intellide-graph/raw/master/docs/figures/start.gif)
    
    - 首先看到的是简介页面；
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

<a name="install"></a>
## 构建与部署

### 编译构建

本系统的依赖环境包括：

1. [Java](https://www.java.com/download/) 1.8+ (用于知识图谱的自动构建与后端服务器的运行)
2. [Node.js](https://www.npmjs.com/) (用于前端服务器的运行)
3. [Maven](https://maven.apache.org/) 3.2+ (optional，用于从源代码开始编译构建)

可以使用maven从源代码开始进行编译构建：

```
mvn package
```

### 数据准备

1. JSON格式数据（DTS、需求、MR）

    将DTS、需求、MR的JSON数据文件分别放在3个文件夹中

2. docx格式数据

    将架构设计文档、需求分析文档、特性设计文档统一放在一个文件夹中
    
3. pdf文档数据

    将pdf格式的配置文档统一放在一个文件夹中
    
### 自动构建知识图谱

1. 编写yaml配置文件

    在任意目录中新建一个yml文件，在该文件中配置：(1)知识图谱的输出文件夹路径；(2)需要允许哪些知识抽取模块；(3)这些知识抽取模块所输入的源数据的路径.
    配置文件的示例如下：
    
    ```
    graphDir: D:/Data/TestGraph-chinese  # 知识图谱的输出文件夹路径，如果需要中文支持，该路径需要由"-chinese"来结尾
    
    # 依次执行如下数据解析插件
    cn.edu.pku.sei.intellide.graph.extraction.json.DTSExtractor: D:/Data/TestData/DTS
    cn.edu.pku.sei.intellide.graph.extraction.json.RequirementExtractor: D:/Data/TestData/Requirement
    cn.edu.pku.sei.intellide.graph.extraction.json.MRExtractor: D:/Data/TestData/MR
    cn.edu.pku.sei.intellide.graph.extraction.docx.DocxExtractor: D:/Data/TestData/Docx
    cn.edu.pku.sei.intellide.graph.extraction.pdf.PDFExtractor: D:/Data/TestData/PDF
    
    # 依次执行如下知识关联与挖掘插件
    cn.edu.pku.sei.intellide.graph.extraction.doc2req.Doc2ReqExtractor:
    ```
    
 2. 运行如下命令，自动生成知识图谱

     ```
     java -jar intellide-graph.jar -gen {yml_config_path}
     ```
     
     运行完毕之后，可以在配置文件中所指定的输出文件夹路径中生成neo4j图数据库格式的知识图谱.
     
### 启动web服务

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
