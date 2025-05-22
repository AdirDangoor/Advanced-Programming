package servlets;

import server.RequestParser.RequestInfo;
import server.Servlet;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HelloServlet implements Servlet {

    @Override
    public void handle(RequestInfo request, OutputStream out) {
        String content = "<html><body><h1>Hello</h1></body></html>";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        System.out.println("HelloServlet: " + request);
        try {
            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + contentBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes(StandardCharsets.UTF_8));

            out.write(contentBytes);
            out.flush();
            System.out.println("Response sent!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}
