package servlets;

import server.RequestParser.RequestInfo;
import server.Servlet;
import graph.TopicManagerSingleton;
import graph.Topic;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo request, OutputStream out) {
        PrintWriter writer = new PrintWriter(out, true);

        String[] uriSegments = request.getUriSegments();
        if (uriSegments.length < 2) {
            sendNotFound(writer);
            return;
        }

        String topic = URLDecoder.decode(uriSegments[1], StandardCharsets.UTF_8);
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

        Topic topicObj = topicManager.getTopic(topic);
        if (topicObj == null) {
            sendNotFound(writer);
            return;
        }

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("<html><body>");
        responseBuilder.append("<h2>Topic: ").append(escapeHtml(topic)).append("</h2>");
        responseBuilder.append("<h3>Messages:</h3>");
        responseBuilder.append("<ul>");
        
    }

    @Override
    public void close() throws IOException {

    }

    private void sendNotFound(PrintWriter writer) {
        writer.println("HTTP/1.1 404 Not Found");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println();
        writer.println("<html><body><h2>404 - Topic not found</h2><a href=\"/app\">Back</a></body></html>");
    }

    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
