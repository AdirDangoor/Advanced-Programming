package configs;

import graph.ParallelAgent;
import graph.TopicManagerSingleton;
import graph.Agent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The GenericConfig class implements the Config interface and provides
 * functionality to read a configuration file, create agents based on the
 * configuration, and manage their lifecycle.
 */
public class GenericConfig implements Config {
    private String confFile;
    private List<ParallelAgent> agents = new ArrayList<>();

    /**
     * Sets the path to the configuration file.
     */
//    public void setConfFile(String confFile) {
//        this.confFile = confFile;
//    }
    public void setConfFile(String confFile) {
        try {
            this.confFile = confFile;
            if (!Files.exists(Paths.get(this.confFile))) {
                // Try alternative paths
                String[] alternatives = {
                        "./" + confFile,
                        "../" + confFile,
                        "src/" + confFile,
                        "src/main/resources/" + confFile
                };

                for (String alt : alternatives) {
                    if (Files.exists(Paths.get(alt))) {
                        this.confFile = alt;
                        break;
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * Reads the configuration file, creates agents based on the configuration,
     * and adds them to the list of agents.
     */
    @Override
    public void create() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(confFile));
            if (lines.size() % 3 != 0) {
                //System.out.println("Invalid configuration file");
                return;
            }

            TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

            for (int i = 0; i < lines.size(); i += 3) {
                String className = lines.get(i);
                String[] subs = lines.get(i + 1).split(",");
                String[] pubs = lines.get(i + 2).split(",");

                Class<?> agentClass = Class.forName(className);
                Agent agent = (Agent) agentClass.getConstructor(TopicManagerSingleton.TopicManager.class, List.class, List.class)
                        .newInstance(topicManager, Arrays.asList(subs), Arrays.asList(pubs));

                ParallelAgent parallelAgent = new ParallelAgent(agent, 10);
                agents.add(parallelAgent);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * Returns the name of the configuration.
     */
    @Override
    public String getName() {
        return "GenericConfig";
    }

    /**
     * Returns the version of the configuration.
     */
    @Override
    public int getVersion() {
        return 0;
    }

    /**
     * Closes all agents and releases any resources held by them.
     */
    @Override
    public void close() {
        for (ParallelAgent agent : agents) {
            agent.close();
        }
    }
}