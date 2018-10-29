package cn.edu.pku.sei.intellide.graph.webapp.entity;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties
public class Conf {

    private String graphDir;
    private String dataDir;
    private String infoDir;

    public String getGraphDir() {
        return graphDir;
    }

    public void setGraphDir(String graphDir) {
        this.graphDir = graphDir;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getInfoDir() {
        return infoDir;
    }

    public void setInfoDir(String infoDir) {
        this.infoDir = infoDir;
    }
}


