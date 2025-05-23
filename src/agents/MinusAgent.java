package agents;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton.TopicManager;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MinusAgent implements Agent {
    private final TopicManager manager;
    private final List<String> subs;
    private final List<String> pubs;
    private final Map<String, Double> lastValues = new HashMap<>();
    private final String name;

    public MinusAgent(TopicManager manager, List<String> subs, List<String> pubs) {
        this.manager = manager;
        this.subs = subs;
        this.pubs = pubs;
        this.name = "MinusAgent";

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
    public void reset() {
        lastValues.clear();
    }

    @Override
    public void callback(String topic, Message msg) {
        try {
            double value = msg.asDouble;
            lastValues.put(topic, value);

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