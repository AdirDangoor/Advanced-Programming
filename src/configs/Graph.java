package configs;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The Graph class represents a graph structure that extends ArrayList of Node objects.
 * It provides methods to check for cycles and to initialize the graph from topics managed by TopicManager.
 */
public class Graph extends ArrayList<Node> {

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
     * Initializes the graph from the topics managed by TopicManager.
     * Creates nodes for each topic and agent, and adds edges between them based on subscriptions and publications.
     */
    public void createFromTopics() {
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();  // Get the topic manager singleton

        Map<String, Node> agentNodes = new HashMap<>();

        for (Topic topic : topicManager.getTopics()) {
            Node topicNode = new Node("T" + topic.name);  // Topic node, T is a prefix for topics
            this.add(topicNode);  // Add the topic node to the graph

            for (Agent agent : topic.getSubscribers()) {
                String agentKey = "A" + agent.getName();

                // Retrieve or create the agent node
                Node agentNode = agentNodes.computeIfAbsent(agentKey, k -> {
                    Node newNode = new Node(agentKey);
                    this.add(newNode);
                    return newNode;
                });

                // Add an edge from the topic to the subscriber agent
                topicNode.addEdge(agentNode);
            }

            for (Agent agent : topic.getPublishers()) {
                String agentKey = "A" + agent.getName();

                // Retrieve or create the agent node
                Node agentNode = agentNodes.computeIfAbsent(agentKey, k -> {
                    Node newNode = new Node(agentKey);
                    this.add(newNode);
                    return newNode;
                });

                // Add an edge from the publisher agent to the topic
                agentNode.addEdge(topicNode);
            }
        }
    }
}