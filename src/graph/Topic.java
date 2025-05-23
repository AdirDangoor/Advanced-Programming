package graph;

import java.util.HashSet;
import java.util.Set;

public class Topic {
    public final String name;
    private final Set<Agent> subs = new HashSet<>();
    private final Set<Agent> pubs = new HashSet<>();
    private Message lastMessage;

    Topic(String name) {
        this.name = name;
        this.lastMessage = null;
    }

    public void subscribe(Agent a) {
        subs.add(a);
    }

    public void unsubscribe(Agent a) {
        subs.remove(a);
    }

    public void publish(Message m) {
        this.lastMessage = m;  // Store the message
        for (Agent a : subs) {
            a.callback(name, m);
        }
    }

    public void addPublisher(Agent a) {
        pubs.add(a);
    }

    public void removePublisher(Agent a) {
        pubs.remove(a);
    }

    // Getters
    public Set<Agent> getSubscribers() {
        return subs;
    }

    public Set<Agent> getPublishers() {
        return pubs;
    }

    public Message getLastMessage() {
        return lastMessage;
    }
}