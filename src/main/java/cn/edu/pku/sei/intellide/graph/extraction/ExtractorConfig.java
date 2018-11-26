package cn.edu.pku.sei.intellide.graph.extraction;

import lombok.Getter;

public class ExtractorConfig {

    @Getter
    private String className, graphDir, dataDir;

    public ExtractorConfig(String className, String graphDir, String dataDir) {
        this.className = className;
        this.graphDir = graphDir;
        this.dataDir = dataDir;
    }

}