package cn.edu.pku.sei.intellide.graph.webapp.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class Conf {

    private String boltUrl;
    private String dataDir;

    public String getBoltUrl() {
        return boltUrl;
    }

    public void setBoltUrl(String boltUrl) {
        this.boltUrl = boltUrl;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

}
