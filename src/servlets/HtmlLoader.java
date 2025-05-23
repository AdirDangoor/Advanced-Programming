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

public class HtmlLoader implements Servlet {

    private final String filePath;

    public HtmlLoader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void handle(RequestInfo request, OutputStream out) {
        Logger.info("HtmlLoader: " + request.getUri());
//        // Serve only specific files
//        if (!request.getUri().equals("/temp.html") && !request.getUri().equals("/form.html") && !request.getUri().equals("/")) {
//            try {
//                out.write(("HTTP/1.1 404 Not Found\r\n\r\n").getBytes(StandardCharsets.UTF_8));
//                out.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return;
//        }

        try {
            String fileName = request.getUri().substring(1); // Remove leading '/'
            if(fileName.isEmpty()) {
                fileName = "index.html"; // Default to index.html if no specific file is requested
            }
            String content = Files.readString(Paths.get(System.getProperty("user.dir"), "html_files", fileName));
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + contentBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes(StandardCharsets.UTF_8));

            out.write(contentBytes);
            out.flush();

        } catch (IOException e) {
            System.out.println("Error loading HTML file: " + e.getMessage());
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