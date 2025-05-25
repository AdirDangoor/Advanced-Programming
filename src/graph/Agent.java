package graph;

public interface Agent {
    String getName();
    String getUUID();  // Add unique identifier for each agent instance
    void reset();
    void callback(String topic, Message msg);
    void close();
    Message getEquation();  // Get the current equation with values
}
