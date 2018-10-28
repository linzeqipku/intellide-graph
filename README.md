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
