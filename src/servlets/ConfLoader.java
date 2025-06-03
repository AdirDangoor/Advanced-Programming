// ConfLoader.java
package servlets;

import server.Servlet;
import server.RequestParser;
import configs.GenericConfig;
import configs.Graph;
import views.HtmlGraphWriter;
import utils.Logger;
import graph.TopicManagerSingleton;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

/**
 * The ConfLoader servlet handles the uploading and processing of configuration files
 * for the computation graph system. It processes POST requests containing configuration
 * files and initializes the computation graph based on the configuration.
 * 
 * Features:
 * - Processes multipart form data to extract configuration file content
 * - Creates a temporary file to store the configuration
 * - Initializes the computation graph based on the configuration
 * - Generates and returns an HTML visualization of the graph
 * - Manages cleanup of temporary files
 * - Handles error cases with appropriate error responses
 */
public class ConfLoader implements Servlet {
    /** Directory for storing temporary configuration files */
    private static final String TEMP_DIR = "temp_configs";
    private static GenericConfig activeConfig = null;

    /**
     * Handles POST requests containing configuration files.
     * 
     * The method performs the following steps:
     * 1. Clears any existing topic manager state
     * 2. Processes the uploaded configuration file
     * 3. Creates a temporary file with the configuration
     * 4. Initializes the computation graph
     * 5. Generates an HTML visualization of the graph
     * 6. Cleans up temporary files
     * 
     * @param req The HTTP request information containing the configuration file
     * @param out The output stream to write the response to
     * @throws IOException If there's an error processing the file or writing the response
     */
    @Override
    public void handle(RequestParser.RequestInfo req, OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        
        try {
            Logger.info("ConfLoader: Received request");
            
            // Clear previous state
            TopicManagerSingleton.get().clear();
            Logger.info("ConfLoader: Cleared previous topic manager state");
            
            // Ensure temp directory exists
            Files.createDirectories(Paths.get(TEMP_DIR));

            // Get the request body
            byte[] body = req.getContent();
            if (body == null || body.length == 0) {
                throw new IOException("No file content received");
            }
            Logger.info("ConfLoader: Received file content of size: " + body.length);

            // Process multipart form data
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body)));
            
            // Read until we find the file content
            String line;
            StringBuilder fileContent = new StringBuilder();
            boolean isFileContent = false;
            boolean foundFileContent = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Content-Type: text/plain") || line.contains("Content-Type: application/octet-stream")) {
                    reader.readLine(); // Skip empty line
                    isFileContent = true;
                    foundFileContent = true;
                    Logger.info("ConfLoader: Found file content marker");
                    continue;
                }
                if (isFileContent) {
                    if (line.startsWith("--")) {
                        break;
                    }
                    // Add "configs." prefix to each agent class name line
                    if (line.endsWith("Agent")) {
                        fileContent.append("agents.").append(line).append("\n");
                    } else {
                        fileContent.append(line).append("\n");
                    }
                }
            }

            if (!foundFileContent) {
                throw new IOException("Could not find file content in request");
            }

            String content = fileContent.toString().trim();
            if (content.isEmpty()) {
                throw new IOException("File content is empty");
            }
            Logger.info("ConfLoader: Parsed file content:\n" + content);

            // Generate unique temp file name
            String tempFileName = TEMP_DIR + "/" + UUID.randomUUID().toString() + ".conf";
            
            // Write content to temp file
            Files.write(Paths.get(tempFileName), content.getBytes());
            Logger.info("ConfLoader: Wrote content to temp file: " + tempFileName);

            // Close previous config if it exists
            if (activeConfig != null) {
                Logger.info("ConfLoader: Closing previous config");
                activeConfig.close();
            }

            // Create and process new config
            GenericConfig config = new GenericConfig();
            config.setConfFile(tempFileName);
            config.create();
            activeConfig = config; // Update static reference
            Logger.info("ConfLoader: Created and processed config");

            // Create graph structure
            Graph graph = new Graph();
            graph.createFromTopics();
            Logger.info("ConfLoader: Created graph structure");

            // Check for cycles
            if (graph.hasCycles()) {
                Logger.warn("ConfLoader: Graph contains cycles");
            }

            // Generate graph HTML
            String graphHtml;
            try {
                graphHtml = HtmlGraphWriter.getGraphHtml(graph);
                Logger.info("ConfLoader: Generated graph HTML");
            } catch (IOException e) {
                Logger.error("Error generating graph HTML: " + e.getMessage());
                throw new IOException("Failed to generate graph visualization", e);
            }

            // Add script to reset topic table
            StringBuilder html = new StringBuilder();
            html.append(graphHtml);

            // Send response
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html; charset=UTF-8");
            writer.println();
            writer.println(html.toString());
            writer.flush();
            Logger.info("ConfLoader: Sent response");

            // Clean up temp file
            Files.deleteIfExists(Paths.get(tempFileName));

        } catch (Exception e) {
            Logger.error("Error in ConfLoader: " + e.getMessage());
            e.printStackTrace();
            String errorHtml = "<html><body><h2>Error parsing configuration</h2><pre>" +
                    e.getMessage() + "</pre></body></html>";
            writer.println("HTTP/1.1 500 Internal Server Error");
            writer.println("Content-Type: text/html; charset=UTF-8");
            writer.println();
            writer.println(errorHtml);
            writer.flush();
        }
    }

    /**
     * Cleans up resources when the servlet is closed.
     * This includes removing all temporary configuration files and the temporary directory.
     * 
     * @throws IOException If there's an error cleaning up the temporary files
     */
    @Override
    public void close() throws IOException {
        // Close any active config
        if (activeConfig != null) {
            Logger.info("ConfLoader: Closing active config during shutdown");
            activeConfig.close();
            activeConfig = null;
        }

        // Clean up temp directory
        try {
            Files.walk(Paths.get(TEMP_DIR))
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            Logger.error("Error deleting temp file: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            Logger.error("Error cleaning up temp directory: " + e.getMessage());
        }
    }
}
