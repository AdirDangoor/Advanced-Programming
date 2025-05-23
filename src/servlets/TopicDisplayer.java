package servlets;

import server.RequestParser.RequestInfo;
import server.Servlet;
import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Message;
import configs.Graph;
import views.HtmlGraphWriter;
import utils.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo request, OutputStream out) throws IOException {
        Logger.info("TopicDisplayer: handle");
        PrintWriter writer = new PrintWriter(out, true);

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

        // Create HTML response with table and graph
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        
        // Add table of topics and values
        html.append("<h2>Topic Values</h2>");
        html.append("<table border='1'>");
        html.append("<tr><th>Topic</th><th>Value</th></tr>");
        
        for (Topic t : topicManager.getTopics()) {
            html.append("<tr>");
            html.append("<td>").append(escapeHtml(t.name)).append("</td>");
            Message msg = t.getLastMessage();
            String value = msg != null ? escapeHtml(msg.asText) : "";
            html.append("<td>").append(value).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        // Add script to refresh graph frame
        html.append("<script>");
        html.append("  window.parent.graphFrame.location.reload();");
        html.append("</script>");

        html.append("</body></html>");

        // Send response
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println();
        writer.println(html.toString());
        writer.flush();
    }

    private void sendError(PrintWriter writer, String message) {
        writer.println("HTTP/1.1 400 Bad Request");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println();
        writer.println("<html><body><h2>Error: " + escapeHtml(message) + "</h2></body></html>");
        writer.flush();
    }

    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    @Override
    public void close() throws IOException {
    }
}
