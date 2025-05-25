package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private final ExecutorService threadPool;
    private final Map<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> deleteServlets = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        Map<String, Servlet> targetMap = getServletMap(httpCommand);
        if (targetMap != null) {
            targetMap.put(uri, s);
        }
    }

    @Override
    public void removeServlet(String httpCommand, String uri) {
        Map<String, Servlet> targetMap = getServletMap(httpCommand);
        if (targetMap != null) {
            targetMap.remove(uri);
        }
    }

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

    private void handleClient(Socket clientSocket) {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            clientSocket.setSoTimeout(5000); // ⏱️ Set timeout to prevent blocking forever

            // log client connection
            //System.out.println("Client connected: " + clientSocket.getInetAddress());

            // parse the request
            RequestParser.RequestInfo request = RequestParser.parseRequest(in);

            Servlet servlet = findBestMatchingServlet(request.getHttpCommand(), request.getUri());

            if (servlet != null) {
                servlet.handle(request, out);
                //System.out.println("Response sent!");
            } else {
                String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(notFoundResponse.getBytes());
                //System.out.println("Sent 404 response");
            }

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //System.out.println("closing client socket");
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Servlet findBestMatchingServlet(String httpCommand, String uri) {
        // print name of function then print the value of httpCommand and uri
        //System.out.println("findBestMatchingServlet: " + httpCommand + " " + uri);

        Map<String, Servlet> servletMap = getServletMap(httpCommand);
        if (servletMap == null) return null;



        return servletMap.entrySet().stream()
                .filter(entry -> uri.startsWith(entry.getKey()))
                .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void close() {
        running = false;
        threadPool.shutdown();
    }
}