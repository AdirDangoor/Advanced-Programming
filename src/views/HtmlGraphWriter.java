package views;

import configs.GenericConfig;
import configs.Graph;
import configs.Node;
import graph.Agent;
import graph.Topic;
import graph.Message;
import graph.TopicManagerSingleton;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class HtmlGraphWriter {
    public static String getGraphHtml(Graph graph) throws IOException {
        // Load the HTML template
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path templatePath = currentDir.resolve("html_files/graph.html");
        String template = Files.readString(templatePath, StandardCharsets.UTF_8);

        // Get topic manager for accessing topic values
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

        // Generate nodes array
        StringBuilder nodesJsArray = new StringBuilder("[\n");
        Map<String, Integer> nodeIds = new HashMap<>();
        int id = 1;

        // Add all nodes
        for (Node node : graph) {
            if (nodesJsArray.length() > 2) { // Add comma if not first entry
                nodesJsArray.append(",\n");
            }

            String nodeName = node.getName();
            nodeIds.put(nodeName, id);

            // Determine node type and style
            boolean isTopic = nodeName.startsWith("T");
            String label = nodeName.substring(1); // Remove T or A prefix
            
            if (isTopic) {
                // Topic node (rectangle)
                String value = "No value";
                Message nodeMessage = node.getMessage();
                
                if (nodeMessage != null) {
                    value = nodeMessage.asText;
                }
                
                nodesJsArray.append(String.format(
                    "    { id: '%s', " +
                    "label: '%s\\n%s', " +
                    "shape: 'box', " +
                    "color: { background: '#97C2FC', border: '#2B7CE9' }, " +
                    "font: { color: '#000000', multi: true, size: 14 }, " +
                    "margin: 10, " +
                    "value: '%s' }",
                    nodeName, label, value, value));
            } else {
                // Agent node (circle)
                String color = "#FB7E81"; // default red
                String borderColor = "#E6194B"; // darker red
                String equation = "";
                
                if (label.contains("Multiply")) {
                    color = "#FFB347"; // orange
                    borderColor = "#E67E22"; // darker orange
                    equation = "Multiplication Agent";
                } else if (label.contains("Divide")) {
                    color = "#98FB98"; // pale green
                    borderColor = "#2ECC71"; // darker green
                    equation = "Division Agent";
                } else if (label.contains("Power")) {
                    color = "#DDA0DD"; // plum
                    borderColor = "#9B59B6"; // darker purple
                    equation = "Power Agent";
                } else if (label.contains("Plus")) {
                    color = "#90EE90"; // light green
                    borderColor = "#32CD32"; // lime green
                    equation = "Addition Agent";
                } else if (label.contains("Minus")) {
                    color = "#FFB6C1"; // light pink
                    borderColor = "#FF69B4"; // hot pink
                    equation = "Subtraction Agent";
                } else if (label.contains("Inc")) {
                    color = "#87CEEB"; // sky blue
                    borderColor = "#3498DB"; // darker blue
                    equation = "Increment Agent";
                }
                
                nodesJsArray.append(String.format("    { id: '%s', label: 'Agent: %s', shape: 'circle', " +
                    "color: { background: '%s', border: '%s' }, font: { color: '#000000' }, " +
                    "equation: '%s' }",
                    nodeName, label, color, borderColor, equation));
            }
        }
        nodesJsArray.append("\n]");

        // Generate edges array
        StringBuilder edgesJsArray = new StringBuilder("[\n");
        for (Node node : graph) {
            for (Node target : node.getEdges()) {
                if (edgesJsArray.length() > 2) { // Add comma if not first entry
                    edgesJsArray.append(",\n");
                }
                edgesJsArray.append(String.format("    { from: '%s', to: '%s', color: { color: '#848484', highlight: '#2B7CE9' } }",
                    node.getName(), target.getName()));
            }
        }
        edgesJsArray.append("\n]");

        // Replace placeholders in template
        String result = template
            .replace("/* NODES_PLACEHOLDER */", nodesJsArray.toString())
            .replace("/* EDGES_PLACEHOLDER */", edgesJsArray.toString());

        return result;
    }
} 