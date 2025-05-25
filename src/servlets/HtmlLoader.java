package servlets;
import utils.Logger;

import server.RequestParser.RequestInfo;
import server.Servlet;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * The HtmlLoader servlet is responsible for serving HTML files from a specified directory.
 * It handles GET requests to the /app/ endpoint and serves static HTML content.
 * 
 * Features:
 * - Serves HTML files from a configured directory
 * - Automatically appends .html extension if missing
 * - Defaults to index.html when no specific file is requested
 * - Provides custom 404 error page for missing files
 * - Handles UTF-8 encoding for all responses
 */
public class HtmlLoader implements Servlet {

    private static final String BASE_DIR = System.getProperty("user.dir");

    /**
     * Constructs the full path to an HTML file given the base folder and filename.
     * 
     * @param folder The base folder containing HTML files
     * @param fileName The name of the HTML file to load
     * @return Path object representing the full path to the HTML file
     */
    public static Path getHtmlFilePath(String folder, String fileName) {
        return Paths.get(BASE_DIR, folder, fileName);
    }

    private final String htmlFilesDir;

    /**
     * Creates a new HtmlLoader that serves files from the specified directory.
     * 
     * @param htmlFilesDir The directory containing HTML files to serve
     */
    public HtmlLoader(String htmlFilesDir) {
        this.htmlFilesDir = htmlFilesDir;
    }

    /**
     * Handles HTTP GET requests for HTML files.
     * 
     * The method performs the following steps:
     * 1. Extracts the requested file name from the URI
     * 2. Adds .html extension if missing
     * 3. Checks if the file exists
     * 4. Serves the file content or an appropriate error page
     * 
     * @param request The HTTP request information
     * @param out The output stream to write the response to
     * @throws IOException If there's an error reading or writing files
     */
    @Override
    public void handle(RequestInfo request, OutputStream out) {
        Logger.info("HtmlLoader: " + request.getUri());

        try {
            // remove /app/ from the URI
            String fileName = request.getUri().substring(request.getUri().indexOf("/app/") + 5);

            if (fileName.isEmpty()) {
                fileName = "index.html"; // Default file
            }

            if (!fileName.endsWith(".html")) {
                fileName += ".html";
            }

            Logger.info("Requested file: " + fileName);

            Path filePath = getHtmlFilePath(htmlFilesDir, fileName);
            Logger.info("File path: " + filePath);

            if (!Files.exists(filePath)) {
                // HTML not found — respond with custom English HTML message
                String errorHtml =
                        "<html lang=\"en\">" +
                                "<head>" +
                                "    <meta charset=\"UTF-8\">" +
                                "    <title>404 - Not Found</title>" +
                                "    <style>" +
                                "        body { font-family: Arial, sans-serif; background: #fff3f3; color: #333; text-align: center; padding-top: 50px; }" +
                                "        .message { font-size: 1.5rem; color: #c92a2a; }" +
                                "    </style>" +
                                "</head>" +
                                "<body>" +
                                "    <div class=\"message\">The requested HTML file could not be found.</div>" +
                                "</body>" +
                                "</html>";

                byte[] errorBytes = errorHtml.getBytes(StandardCharsets.UTF_8);
                out.write(("HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: " + errorBytes.length + "\r\n" +
                        "Connection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                out.write(errorBytes);
                out.flush();
                return;
            }

            String content = Files.readString(filePath);
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + contentBytes.length + "\r\n" +
                    "Connection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(contentBytes);
            out.flush();

        } catch (IOException e) {
            Logger.error("Error loading HTML file: " + e.getMessage());
            try {
                out.write(("HTTP/1.1 500 Internal Server Error\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Closes any resources held by the servlet.
     * Currently, this servlet doesn't hold any resources that need closing.
     */
    @Override
    public void close() throws IOException {
        // No resources to close
    }
}