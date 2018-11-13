package cn.edu.pku.sei.intellide.graph.webapp;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SnowView {

    public static void main(String[] args) {

        System.setProperty("file.encoding", "utf-8");
        CmdOption option = new CmdOption();
        CmdLineParser parser = new CmdLineParser(option);

        try {

            if (args.length == 0) {
                showHelp(parser);
                return;
            }
            parser.parseArgument(args);
            if (option.exec) {
                SpringApplication.run(SnowView.class, args);
            } else if (option.genConfigPath != null) {
                KnowledgeExtractor.executeFromYaml(FileUtils.readFileToString(new File(option.genConfigPath), "utf-8"));
            }

        } catch (CmdLineException cle) {
            System.out.println("Command line error: " + cle.getMessage());
            showHelp(parser);
            return;
        } catch (Exception e) {
            System.out.println("Error in main: " + e.getMessage());
            e.printStackTrace();
            return;
        }

    }

    public static void showHelp(CmdLineParser parser) {
        System.out.println("SnowGraph [options ...] [arguments...]");
        parser.printUsage(System.out);
    }

}

class CmdOption {

    @Option(name = "-gen", usage = "Generate a knowledge graph according to the yaml configure file")
    public String genConfigPath = null;

    @Option(name = "-exec", usage = "Run the web application in localhost")
    public boolean exec = false;

}