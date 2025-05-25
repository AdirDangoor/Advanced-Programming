package server;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Servlet interface defines the contract for handling HTTP requests in the server.
 * Each servlet implementation can handle specific types of requests and generate
 * appropriate responses.
 * 
 * Servlets are registered with the HTTP server for specific URI patterns and HTTP methods,
 * and the server will delegate matching requests to the appropriate servlet instance.
 */
public interface Servlet {
    /**
     * Handles an HTTP request and generates a response.
     * 
     * @param ri The parsed request information containing HTTP method, URI, parameters, etc.
     * @param toClient The output stream to write the response to
     * @throws IOException If there's an error reading the request or writing the response
     */
    void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException;

    /**
     * Closes any resources held by the servlet.
     * This method is called when the servlet is being removed or the server is shutting down.
     * 
     * @throws IOException If there's an error closing resources
     */
    void close() throws IOException;
}
