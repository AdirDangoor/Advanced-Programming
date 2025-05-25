package agents;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton.TopicManager;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MinusAgent implements Agent {
    private final TopicManager manager;
    private final List<String> subs;
    private final List<String> pubs;
    private final Map<String, Double> lastValues = new HashMap<>();
    private final String name;
    private final String uuid;
    private Message equation;  // Store the current equation

    public MinusAgent(TopicManager manager, List<String> subs, List<String> pubs) {
        this.manager = manager;
        this.subs = subs;
        this.pubs = pubs;
        this.name = "MinusAgent";
        this.uuid = UUID.randomUUID().toString();
        this.equation = new Message("? - ?");  // Initial equation

        if (subs.size() != 2 || pubs.size() != 1) {
            throw new IllegalArgumentException("MinusAgent requires exactly 2 inputs and 1 output");
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
        equation = new Message("? - ?");
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
        equation = new Message(String.format("%s - %s", val1, val2));
    }

    @Override
    public void callback(String topic, Message msg) {
        try {
            double value = msg.asDouble;
            lastValues.put(topic, value);

            // Update the equation whenever we get a new value
            updateEquation();

            // If we have both values, subtract them
            if (lastValues.size() == 2) {
                // Get values in the correct order (first - second)
                double first = lastValues.get(subs.get(0));
                double second = lastValues.get(subs.get(1));
                
                double result = first - second;
                Topic outTopic = manager.getTopic(pubs.get(0));
                outTopic.publish(new Message(result));
            }
        } catch (Exception e) {
            System.err.println("Error processing message in MinusAgent: " + e.getMessage());
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