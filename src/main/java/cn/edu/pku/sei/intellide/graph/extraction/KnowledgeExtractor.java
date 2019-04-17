package cn.edu.pku.sei.intellide.graph.extraction;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class KnowledgeExtractor {

    @Getter
    @Setter
    private String graphDir;

    @Getter
    @Setter
    private String dataDir;

    @Getter
    private BatchInserter inserter = null;
    @Getter
    private GraphDatabaseService db = null;

    public static void execute(List<ExtractorConfig> extractorConfigList) {
        for (ExtractorConfig config : extractorConfigList) {
            System.out.println(config.getClassName() + " start ...");
            KnowledgeExtractor extractor = null;
            try {
                extractor = (KnowledgeExtractor) Class.forName(config.getClassName()).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            extractor.setGraphDir(config.getGraphDir());
            extractor.setDataDir(config.getDataDir());
            try {
                extractor.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(config.getClassName() + " finished.");
        }
    }

    public static void executeFromYaml(String yamlStr) {
        Yaml yaml = new Yaml();
        Map<String, Object> ret = yaml.load(yamlStr);
        String graphDir = (String) ret.get("graphDir");
        ret.remove("graphDir");
        boolean increment = false;
        if (ret.containsKey("increment") && (boolean)ret.get("increment")){
            increment = true;
            ret.remove("increment");
        }
        List<ExtractorConfig> configs = new ArrayList<>();
        for (String key : ret.keySet()) {
            configs.add(new ExtractorConfig(key, graphDir, (String) ret.get(key)));
        }
        if (new File(graphDir).exists() && !increment){
            try {
                FileUtils.deleteDirectory(new File(graphDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        execute(configs);
    }

    public static void main(String[] args) {
        try {
            KnowledgeExtractor.executeFromYaml(FileUtils.readFileToString(new File(args[0]), "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isBatchInsert() {
        return false;
    }

    public abstract void extraction();

    public void execute() throws IOException {
        if (this.isBatchInsert()) {
            inserter = BatchInserters.inserter(new File(graphDir));
            this.extraction();
            inserter.shutdown();
        } else {
            db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
            this.extraction();
            db.shutdown();
        }
    }

}