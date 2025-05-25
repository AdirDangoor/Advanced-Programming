package server;

/**
 * The HTTPServer interface defines the contract for a simple HTTP server implementation.
 * It extends Runnable to allow the server to run in its own thread.
 * 
 * The server supports basic HTTP operations (GET, POST, DELETE) through a servlet-based
 * architecture, where different URL paths can be handled by different servlet implementations.
 */
public interface HTTPServer extends Runnable {
    /**
     * Registers a servlet to handle requests for a specific HTTP command and URI pattern.
     * 
     * @param httpCommand The HTTP method (GET, POST, DELETE)
     * @param uri The URI pattern that this servlet will handle
     * @param s The servlet instance that will handle matching requests
     */
    public void addServlet(String httpCommand, String uri, Servlet s);

    /**
     * Removes a servlet registration for a specific HTTP command and URI pattern.
     * 
     * @param httpCommand The HTTP method (GET, POST, DELETE)
     * @param uri The URI pattern to unregister
     */
    public void removeServlet(String httpCommand, String uri);

    /**
     * Starts the HTTP server in a new thread.
     */
    public void start();

    /**
     * Closes the HTTP server and releases any resources.
     * This should stop the server thread and clean up any open connections.
     */
    public void close();
}
