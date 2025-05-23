package servlets;
import utils.Logger;

import server.RequestParser.RequestInfo;
import server.Servlet;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class HtmlLoader implements Servlet {

    private static final String BASE_DIR = System.getProperty("user.dir");

    public static Path getHtmlFilePath(String folder, String fileName) {
        return Paths.get(BASE_DIR, folder, fileName);
    }

    private final String htmlFilesDir;

    public HtmlLoader(String htmlFilesDir) {
        this.htmlFilesDir = htmlFilesDir;
    }

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


    @Override
    public void close() throws IOException {
        // No resources to close
    }
}