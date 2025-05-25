package servlets;

import server.RequestParser.RequestInfo;
import server.Servlet;
import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Message;
import graph.Agent;
import utils.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * The TopicDisplayer servlet handles the real-time display and updating of topic values
 * in the computation graph system. It processes GET requests to publish messages to topics
 * and updates both the topic table and graph visualization.
 * 
 * Features:
 * - Handles publishing messages to topics
 * - Updates the topic table with current values
 * - Updates the graph visualization in real-time
 * - Provides reset functionality to clear the display
 * - Handles error cases with appropriate error responses
 */
public class TopicDisplayer implements Servlet {

    /**
     * Handles GET requests for topic updates and message publishing.
     * 
     * The method handles two types of requests:
     * 1. Reset requests (?reset=true) - Clears the topic table
     * 2. Publish requests (?topic=X&message=Y) - Publishes a message to a topic
     * 
     * After processing, it updates both the topic table and graph visualization
     * with the current state of all topics and agents.
     * 
     * @param request The HTTP request information
     * @param out The output stream to write the response to
     * @throws IOException If there's an error writing the response
     */
    @Override
    public void handle(RequestInfo request, OutputStream out) throws IOException {
        Logger.info("TopicDisplayer: handle");
        PrintWriter writer = new PrintWriter(out, true);

        // Check if this is a reset request
        String reset = request.getParameters().get("reset");
        if (reset != null && reset.equals("true")) {
            sendEmptyTable(writer);
            return;
        }

        // Get topic and message from parameters
        String topic = request.getParameters().get("topic");
        String message = request.getParameters().get("message");
        
        if (topic == null || message == null) {
            sendError(writer, "Missing topic or message parameter");
            return;
        }

        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        Topic topicObj = topicManager.getTopic(topic);

        if (topicObj == null) {
            sendError(writer, "Topic not found: " + topic);
            return;
        }

        // Publish the message
        topicObj.publish(new Message(message));
        Logger.info("Published message '" + message + "' to topic '" + topic + "'");

        // Build JSON of topic values and agent equations
        StringBuilder json = new StringBuilder();
        json.append("{\"topics\":{");
        boolean first = true;
        
        for (Topic t : topicManager.getTopics()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            Message msg = t.getLastMessage();
            String value = msg != null ? msg.asText : "";
            json.append(String.format("\"%s\":\"%s\"", 
                escapeJson(t.name), 
                escapeJson(value)));
        }
        json.append("},\"agents\":{");
        
        // Add agent equations
        first = true;
        Set<Agent> processedAgents = new HashSet<>();
        for (Topic t : topicManager.getTopics()) {
            for (Agent agent : t.getSubscribers()) {
                if (processedAgents.add(agent)) {  // Only process if agent hasn't been seen before
                    if (!first) {
                        json.append(",");
                    }
                    first = false;
                    Message eq = agent.getEquation();
                    String equation = eq != null ? eq.asText : "?";
                    // Include UUID in the key to make it unique
                    String uniqueKey = "A" + agent.getName() + "_" + agent.getUUID().substring(0, 8);
                    json.append(String.format("\"%s\":{\"uuid\":\"%s\",\"equation\":\"%s\"}",
                        escapeJson(uniqueKey),
                        escapeJson(agent.getUUID()),
                        escapeJson(equation)));
                }
            }
        }
        json.append("}");
        json.append("}");
        Logger.info("TopicDisplayer: Generated JSON update: " + json.toString());

        // Load the topic table template
        Path templatePath = Paths.get(System.getProperty("user.dir"), "html_files", "topic_table.html");
        String template = Files.readString(templatePath);

        // Send response
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println();
        writer.println(template);
        writer.println("<script>");
        writer.println("  window.updateTopicTable(" + json.toString() + ".topics);");
        // Update graph with new values
        writer.println("  try {");
        // Get the top-level window
        writer.println("    const topWindow = window.top;");
        // Get the graph frame
        writer.println("    const graphFrame = topWindow.document.getElementById('graphFrame');");
        writer.println("    if (graphFrame && graphFrame.contentWindow) {");
        writer.println("      const graphWindow = graphFrame.contentWindow;");
        writer.println("      if (graphWindow.updateGraph) {");
        writer.println("        graphWindow.updateGraph(" + json.toString() + ");");
        writer.println("      } else {");
        writer.println("        console.error('updateGraph function not found in graph window');");
        writer.println("      }");
        writer.println("    } else {");
        writer.println("      console.error('Could not access graph frame or its content window');");
        writer.println("    }");
        writer.println("  } catch (e) {");
        writer.println("    console.error('Error accessing graph frame:', e);");
        writer.println("  }");
        writer.println("</script>");
        writer.flush();
    }

    /**
     * Sends an empty topic table response.
     * Used when resetting the display or when there are no topics.
     * 
     * @param writer The PrintWriter to write the response to
     * @throws IOException If there's an error reading the template or writing the response
     */
    private void sendEmptyTable(PrintWriter writer) throws IOException {
        // Load the topic table template
        Path templatePath = Paths.get(System.getProperty("user.dir"), "html_files", "topic_table.html");
        String template = Files.readString(templatePath);

        // Send response with empty data
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println();
        writer.println(template);
        writer.println("<script>");
        writer.println("  window.updateTopicTable({});");
        writer.println("</script>");
        writer.flush();
    }

    /**
     * Sends an error response with the specified message.
     * 
     * @param writer The PrintWriter to write the response to
     * @param message The error message to display
     */
    private void sendError(PrintWriter writer, String message) {
        writer.println("HTTP/1.1 400 Bad Request");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println();
        writer.println("<html><body><h2>Error: " + escapeHtml(message) + "</h2></body></html>");
        writer.flush();
    }

    /**
     * Escapes special HTML characters in a string to prevent XSS.
     * 
     * @param input The string to escape
     * @return The escaped string safe for HTML output
     */
    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Escapes special characters in a string for JSON output.
     * 
     * @param input The string to escape
     * @return The escaped string safe for JSON output
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Closes any resources held by the servlet.
     * Currently, this servlet doesn't hold any resources that need closing.
     */
    @Override
    public void close() throws IOException {
    }
}
