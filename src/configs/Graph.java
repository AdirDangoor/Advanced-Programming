package configs;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;
import utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The Graph class represents a graph structure that extends ArrayList of Node objects.
 * It provides methods to check for cycles and to initialize the graph from topics managed by TopicManager.
 */
public class Graph extends ArrayList<Node> {
    private Map<String, String> agentUUIDToNodeId = new HashMap<>();

    /**
     * Checks if the graph contains any cycles.
     *
     * @return true if the graph has cycles, false otherwise
     */
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) {  // Using the method already defined in Node
                return true; // A cycle was found in this node's connected component
            }
        }
        return false; // No cycle found
    }

    /**
     * Gets a unique agent key for an agent instance using its UUID
     */
    private String getUniqueAgentKey(Agent agent) {
        return agentUUIDToNodeId.computeIfAbsent(agent.getUUID(), uuid -> {
            String baseName = agent.getName();
            return "A" + baseName + "_" + uuid.substring(0, 8);  // Use first 8 chars of UUID
        });
    }

    /**
     * Initializes the graph from the topics managed by TopicManager.
     * Creates nodes for each topic and agent, and adds edges between them based on subscriptions and publications.
     */
    public void createFromTopics() {
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        Map<String, Node> agentNodes = new HashMap<>();

        for (Topic topic : topicManager.getTopics()) {
            Logger.info("Creating topic node for topic: " + topic.name);
            Node topicNode = new Node("T" + topic.name);
            this.add(topicNode);

            for (Agent agent : topic.getSubscribers()) {
                String agentKey = getUniqueAgentKey(agent);
                Logger.info("Processing agent with UUID: " + agent.getUUID() + ", key: " + agentKey);

                // Retrieve or create the agent node
                Node agentNode = agentNodes.computeIfAbsent(agentKey, k -> {
                    Node newNode = new Node(k);
                    this.add(newNode);
                    return newNode;
                });

                // Add an edge from the topic to the subscriber agent
                topicNode.addEdge(agentNode);
            }

            for (Agent agent : topic.getPublishers()) {
                String agentKey = getUniqueAgentKey(agent);
                Logger.info("Processing publisher agent with UUID: " + agent.getUUID() + ", key: " + agentKey);

                // Retrieve or create the agent node
                Node agentNode = agentNodes.computeIfAbsent(agentKey, k -> {
                    Node newNode = new Node(k);
                    this.add(newNode);
                    return newNode;
                });

                // Add an edge from the publisher agent to the topic
                agentNode.addEdge(topicNode);
            }
        }

        printGraph();
    }

    // PRINT THE GRAPH
    public void printGraph() {
        for (Node node : this) {
            Logger.info(node.toString());
        }
    }
}