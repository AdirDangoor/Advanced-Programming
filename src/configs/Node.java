package configs;

import graph.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
    private String name;
    private List<Node> edges;
    private Message message;

    // Constructor
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public Message getMessage() {
        return message;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    // Add an edge
    public void addEdge(Node node) {
        edges.add(node);
    }

    // Public method to check for cycles
    public boolean hasCycles() {
        return hasCycles(new HashSet<>(), new HashSet<>());
    }

    // Private helper method to detect cycles using DFS
    private boolean hasCycles(Set<Node> visited, Set<Node> recursionStack) {
        if (recursionStack.contains(this)) {
            return true; // Found a cycle
        }
        if (visited.contains(this)) {
            return false; // Already checked this node
        }

        visited.add(this);
        recursionStack.add(this);

        for (Node neighbor : edges) {
            if (neighbor.hasCycles(visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(this);
        return false;
    }
}
