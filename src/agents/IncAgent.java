package agents;

import graph.TopicManagerSingleton.TopicManager;
import graph.Agent;
import graph.Message;
import java.util.List;

public class IncAgent implements Agent {
    private List<String> _subs;
    private List<String> _pubs;

    private TopicManager topicManager;

    public IncAgent(TopicManager topicManager, List<String> subs, List<String> pubs) {
        this.topicManager = topicManager;
        this._subs = subs;
        this._pubs = pubs;
        // subscribe to the first topic
        topicManager.getTopic(subs.get(0)).subscribe(this);
    }


    @Override
    public String getName() {
        return "IncAgent";
    }

    @Override
    public void reset() {

    }

    /**
     * This method is called when a message is published to the subscribed topic.
     * The method should increase the message by 1 and publish the result to the first topic in the list of published topics.
     */
    @Override
    public void callback(String topicName, Message message) {
        if (topicName.equals(_subs.get(0))) {
            double value = message.asDouble;
            if (!Double.isNaN(value)) {
                double result = value + 1;
                try {
                    topicManager.getTopic(_pubs.get(0)).publish(new Message(result));
                } catch (Exception e) {
                    //System.err.println("Error publishing to " + _pubs.get(0) + ": " + e.getMessage());
                }
            }
        }
    }
//    @Override
//    public void callback(String topicName, Message message) {
//        if (topicName.equals(_subs.get(0))) {
//            double value = message.asDouble;
//            if (!Double.isNaN(value)) {
//                double result = value + 1;
//                topicManager.getTopic(_pubs.get(0)).publish(new Message(result));
//            }
//        }
//    }

    @Override
    public void close() {
    }
}
