package cn.edu.pku.sei.intellide.graph.webapp.entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties
public class Conf {

    @Getter @Setter
    private String graphDir,dataDir,infoDir;

}


