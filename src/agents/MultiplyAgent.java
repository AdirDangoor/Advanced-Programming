package agents;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton.TopicManager;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MultiplyAgent implements Agent {
    private final TopicManager manager;
    private final List<String> subs;
    private final List<String> pubs;
    private final Map<String, Double> lastValues = new HashMap<>();
    private final String name;
    private final String uuid;  // Add UUID field
    private Message equation;  // Store the current equation

    public MultiplyAgent(TopicManager manager, List<String> subs, List<String> pubs) {
        this.manager = manager;
        this.subs = subs;
        this.pubs = pubs;
        this.name = "MultiplyAgent";
        this.uuid = UUID.randomUUID().toString();  // Generate unique ID
        this.equation = new Message("? * ?");  // Initial equation

        if (subs.size() != 2 || pubs.size() != 1) {
            throw new IllegalArgumentException("MultiplyAgent requires exactly 2 inputs and 1 output");
        }

        // Subscribe to input topics
        for (String topic : subs) {
            Topic t = manager.getTopic(topic);
            t.subscribe(this);
        }

        // Register as publisher for output topics
        for (String topic : pubs) {
            Topic t = manager.getTopic(topic);
            t.addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public void reset() {
        lastValues.clear();
        equation = new Message("? * ?");
    }

    @Override
    public Message getEquation() {
        return equation;
    }

    private void updateEquation() {
        String val1 = String.valueOf(lastValues.getOrDefault(subs.get(0), Double.NaN));
        String val2 = String.valueOf(lastValues.getOrDefault(subs.get(1), Double.NaN));
        if (val1.equals("NaN")) val1 = "?";
        if (val2.equals("NaN")) val2 = "?";
        equation = new Message(String.format("%s * %s", val1, val2));
    }

    @Override
    public void callback(String topic, Message msg) {
        try {
            double value = msg.asDouble;
            lastValues.put(topic, value);

            // Update the equation whenever we get a new value
            updateEquation();

            // If we have both values, multiply them
            if (lastValues.size() == 2) {
                double result = lastValues.values().stream()
                    .reduce(1.0, (a, b) -> a * b);
                Topic outTopic = manager.getTopic(pubs.get(0));
                outTopic.publish(new Message(result));
            }
        } catch (Exception e) {
            System.err.println("Error processing message in MultiplyAgent: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        // Unsubscribe from all topics
        for (String topic : subs) {
            Topic t = manager.getTopic(topic);
            t.unsubscribe(this);
        }
        
        // Remove as publisher from output topics
        for (String topic : pubs) {
            Topic t = manager.getTopic(topic);
            t.removePublisher(this);
        }
    }
} 