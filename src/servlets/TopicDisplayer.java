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

        // Create HTML response with table
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        
        // Add table of topics and values
        html.append("<h2>Topic Values</h2>");
        html.append("<table border='1'>");
        html.append("<tr><th>Topic</th><th>Value</th></tr>");
        
        // Build JSON of topic values for JavaScript
        StringBuilder topicValues = new StringBuilder();
        topicValues.append("{");
        boolean first = true;
        
        for (Topic t : topicManager.getTopics()) {
            html.append("<tr>");
            html.append("<td>").append(escapeHtml(t.name)).append("</td>");
            Message msg = t.getLastMessage();
            String value = msg != null ? escapeHtml(msg.asText) : "";
            html.append("<td>").append(value).append("</td>");
            html.append("</tr>");
            
            if (!first) {
                topicValues.append(",");
            }
            first = false;
            topicValues.append("\"").append(t.name).append("\":\"").append(value).append("\"");
        }
        topicValues.append("}");
        html.append("</table>");

        // Add script to update the bottom line info when clicking nodes
        html.append("<script>");
        html.append("  try {");
        html.append("    const graphFrame = window.parent.graphFrame;");
        html.append("    if (graphFrame && graphFrame.contentWindow) {");
        html.append("      const graphDoc = graphFrame.contentWindow.document;");
        html.append("      const network = graphDoc.getElementById('mynetwork');");
        html.append("      if (network && network.network) {");
        html.append("        const topicValues = ").append(topicValues.toString()).append(";");
        html.append("        network.network.on('click', function(params) {");
        html.append("          if (params.nodes.length > 0) {");
        html.append("            const nodeId = params.nodes[0];");
        html.append("            const bottomLine = graphDoc.getElementById('bottomLine');");
        html.append("            if (bottomLine) {");
        html.append("              if (nodeId.startsWith('T')) {");
        html.append("                const topicName = nodeId.substring(1);");
        html.append("                const value = topicValues[topicName] || 'No value';");
        html.append("                bottomLine.textContent = 'Topic: ' + topicName + ' | Current Value: ' + value;");
        html.append("              } else if (nodeId.startsWith('A')) {");
        html.append("                const agentName = nodeId.substring(1);");
        html.append("                const node = network.network.body.nodes.get(nodeId);");
        html.append("                if (node && node.options.equation) {");
        html.append("                  bottomLine.textContent = 'Agent: ' + agentName + ' | Equation: ' + node.options.equation;");
        html.append("                } else {");
        html.append("                  bottomLine.textContent = 'Agent: ' + agentName;");
        html.append("                }");
        html.append("              }");
        html.append("            }");
        html.append("          }");
        html.append("        });");
        html.append("      }");
        html.append("    }");
        html.append("  } catch (e) {");
        html.append("    console.error('Error updating click handler:', e);");
        html.append("  }");
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
