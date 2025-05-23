// ConfLoader.java
package servlets;

import server.Servlet;
import server.RequestParser;

import java.io.*;
import utils.Logger;

public class ConfLoader implements Servlet {
    @Override
    public void handle(RequestParser.RequestInfo req, OutputStream out) throws IOException {
        try {
            // print the request
            Logger.info("ConfLoader: " + req);



        } catch (Exception e) {
            String errorHtml = "<html><body><h2>Error parsing configuration</h2><pre>" +
                    e.getMessage() + "</pre></body></html>";
            String response = "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/html\r\n\r\n" + errorHtml;
            out.write(response.getBytes());
        }
    }

    @Override
    public void close() throws IOException {

    }
}
