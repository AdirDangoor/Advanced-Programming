package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * MyHTTPServer is a simple multi-threaded HTTP server implementation.
 * It supports handling of GET, POST, and DELETE requests through a servlet-based architecture.
 * 
 * Features:
 * - Multi-threaded request handling using a thread pool
 * - Support for multiple servlets mapped to different URI patterns
 * - Automatic best-match servlet selection based on URI
 * - Graceful shutdown with resource cleanup
 * - Connection timeout handling
 */
public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private final ExecutorService threadPool;
    private final Map<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> deleteServlets = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    /**
     * Creates a new HTTP server instance.
     * 
     * @param port The port number to listen on
     * @param nThreads The number of threads in the thread pool for handling requests
     */
    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(nThreads);
    }

    /**
     * Registers a servlet for a specific HTTP method and URI pattern.
     * 
     * @param httpCommand The HTTP method (GET, POST, DELETE)
     * @param uri The URI pattern to match against incoming requests
     * @param s The servlet to handle matching requests
     */
    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        Map<String, Servlet> targetMap = getServletMap(httpCommand);
        if (targetMap != null) {
            targetMap.put(uri, s);
        }
    }

    /**
     * Removes a servlet registration for a specific HTTP method and URI pattern.
     * 
     * @param httpCommand The HTTP method (GET, POST, DELETE)
     * @param uri The URI pattern to unregister
     */
    @Override
    public void removeServlet(String httpCommand, String uri) {
        Map<String, Servlet> targetMap = getServletMap(httpCommand);
        if (targetMap != null) {
            targetMap.remove(uri);
        }
    }

    /**
     * Gets the appropriate servlet map for a given HTTP method.
     * 
     * @param httpCommand The HTTP method (GET, POST, DELETE)
     * @return The corresponding servlet map, or null if the method is not supported
     */
    private Map<String, Servlet> getServletMap(String httpCommand) {
        switch (httpCommand.toUpperCase()) {
            case "GET":
                return getServlets;
            case "POST":
                return postServlets;
            case "DELETE":
                return deleteServlets;
            default:
                return null;
        }
    }

    /**
     * Main server loop that accepts incoming connections.
     * Each connection is handled in a separate thread from the thread pool.
     */
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000); // Allow periodic checks of the `running` flag

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(() -> handleClient(clientSocket));
                } catch (SocketTimeoutException e) {
                    // Check if still running, then continue
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles an individual client connection.
     * Parses the request, finds the appropriate servlet, and delegates request handling.
     * 
     * @param clientSocket The socket connected to the client
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            clientSocket.setSoTimeout(5000); // Set timeout to prevent blocking forever

            // Parse the request
            RequestParser.RequestInfo request = RequestParser.parseRequest(in);

            Servlet servlet = findBestMatchingServlet(request.getHttpCommand(), request.getUri());

            if (servlet != null) {
                servlet.handle(request, out);
            } else {
                String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(notFoundResponse.getBytes());
            }

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds the best matching servlet for a given HTTP method and URI.
     * The best match is the servlet with the longest matching URI prefix.
     * 
     * @param httpCommand The HTTP method of the request
     * @param uri The request URI
     * @return The best matching servlet, or null if no match is found
     */
    private Servlet findBestMatchingServlet(String httpCommand, String uri) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand);
        if (servletMap == null) return null;

        return servletMap.entrySet().stream()
                .filter(entry -> uri.startsWith(entry.getKey()))
                .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * Starts the server in a new thread.
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * Stops the server and cleans up resources.
     * This includes shutting down the thread pool and closing any open connections.
     */
    @Override
    public void close() {
        running = false;
        threadPool.shutdown();
    }
}