package agents;

import graph.TopicManagerSingleton.TopicManager;
import graph.Topic;
import graph.Agent;
import graph.Message;
import java.util.List;

public class IncAgent implements Agent {
    private final TopicManager manager;
    private final List<String> subs;
    private final List<String> pubs;
    private final String name;

    public IncAgent(TopicManager manager, List<String> subs, List<String> pubs) {
        this.manager = manager;
        this.subs = subs;
        this.pubs = pubs;
        this.name = "IncAgent";

        if (subs.size() != 1 || pubs.size() != 1) {
            throw new IllegalArgumentException("IncAgent requires exactly 1 input and 1 output");
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
        // Nothing to reset since we don't store state
    }

    /**
     * This method is called when a message is published to the subscribed topic.
     * The method should increase the message by 1 and publish the result to the first topic in the list of published topics.
     */
    @Override
    public void callback(String topicName, Message message) {
        try {
            double value = message.asDouble;
            if (!Double.isNaN(value)) {
                double result = value + 1;
                Topic outTopic = manager.getTopic(pubs.get(0));
                outTopic.publish(new Message(result));
            }
        } catch (Exception e) {
            System.err.println("Error processing message in IncAgent: " + e.getMessage());
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
