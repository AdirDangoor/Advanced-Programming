package servlets;

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
        System.out.println("HtmlLoader: " + request.getUri());
        try {
            String content = Files.readString(Paths.get(System.getProperty("user.dir"), filePath, "form.html"));
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + contentBytes.length + "\r\n" +
                    "Connection: close\r\n" +  // add this
                    "\r\n").getBytes(StandardCharsets.UTF_8));

            out.write(contentBytes);
            out.flush(); // make sure everything is sent

        } catch (IOException e) {
            System.out.println("Error loading HTML file: " + e.getMessage());
            try {
                out.write(("HTTP/1.1 500 Internal Server Error\r\n\r\n").getBytes());
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