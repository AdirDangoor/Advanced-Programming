package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The ParallelAgent class implements the Agent interface and provides a way to handle messages concurrently
 * using a separate worker thread and a blocking queue.
 */
public class ParallelAgent implements Agent {
    private final Agent agent;
    private final BlockingQueue<QueuedMessage> queue;
    private final Thread workerThread;
    private volatile boolean running = true;

    /**
     * Inner class representing a message with a topic.
     */
    private static class QueuedMessage {
        final String topic;
        final Message message;

        /**
         * Constructs a QueuedMessage with the specified topic and message.
         */
        QueuedMessage(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }

    /**
     * Constructs a ParallelAgent with the specified agent and queue capacity.
     * The agent's callback method will be called in a separate worker thread.
     * The queue is used to store messages that are waiting to be processed.
     */
    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);

        this.workerThread = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    QueuedMessage queuedMessage = queue.take(); // Blocking call
                    agent.callback(queuedMessage.topic, queuedMessage.message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        this.workerThread.start();
    }

    /**
     * Adds them message and topic to the queue as a QueuedMessage.
     */
    @Override
    public void callback(String topic, Message message) {
        try {
            queue.put(new QueuedMessage(topic, message)); // Non-blocking callback
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the name of the agent.
     */
    @Override
    public String getName() {
        return agent.getName();
    }

    /**
     * Returns the UUID of the agent.
     */
    @Override
    public String getUUID() {
        return agent.getUUID();
    }

    /**
     * Resets the agent.
     */
    @Override
    public void reset() {
        agent.reset();
    }

    @Override
    public Message getEquation() {
        return agent.getEquation();
    }

    /**
     * Stops the worker thread and waits for it to exit cleanly.
     */
    @Override
    public void close() {
        running = false;
        workerThread.interrupt(); // Ensure take() unblocks
        try {
            workerThread.join(); // Wait for the thread to exit cleanly
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}