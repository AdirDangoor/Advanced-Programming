package server;

import java.io.*;
import java.util.*;
import utils.Logger;

/**
 * The RequestParser class provides functionality to parse HTTP requests into structured data.
 * It handles parsing of request lines, headers, query parameters, and request bodies.
 * 
 * Features:
 * - Parses HTTP request line (method, URI, version)
 * - Extracts query parameters from URIs
 * - Processes request headers
 * - Handles request bodies (including form data)
 * - Supports both GET and POST request parsing
 */
public class RequestParser {

    /**
     * Parses an HTTP request from a BufferedReader into a structured RequestInfo object.
     * 
     * The method handles:
     * 1. Request line parsing (HTTP method, URI, version)
     * 2. Query parameter extraction
     * 3. Header parsing
     * 4. Body content reading (for POST requests)
     * 5. Form data parsing (for application/x-www-form-urlencoded)
     * 
     * @param reader The BufferedReader containing the HTTP request
     * @return A RequestInfo object containing the parsed request data
     * @throws IOException If there's an error reading from the input stream
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request");
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            throw new IOException("Invalid request line");
        }
        String httpCommand = requestParts[0];
        String uri = requestParts[1];

        Logger.info("Request line: " + requestLine);
        Logger.info("HTTP command: " + httpCommand);
        Logger.info("URI: " + uri);

        String[] uriSegments = uri.split("\\?")[0].split("/");
        uriSegments = Arrays.stream(uriSegments).filter(s -> !s.isEmpty()).toArray(String[]::new);

        Map<String, String> parameters = new HashMap<>();
        if (uri.contains("?")) {
            String queryString = uri.split("\\?", 2)[1];
            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            //System.out.println("Header line: " + line);
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        StringBuilder bodyBuilder = new StringBuilder();

        // Check for Content-Length header to determine if there's a body
        int contentLength = 0;
        if (headers.containsKey("Content-Length")) {
            try {
                contentLength = Integer.parseInt(headers.get("Content-Length"));
            } catch (NumberFormatException e) {
                // Handle invalid Content-Length, maybe log a warning
            }
        }

        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            int bytesRead = reader.read(buffer, 0, contentLength);

            if (bytesRead > 0) {
                bodyBuilder.append(buffer, 0, bytesRead);
            }

            // Parse body parameters
            if (headers.containsKey("Content-Type") && headers.get("Content-Type").contains("application/x-www-form-urlencoded")) {
                String body = bodyBuilder.toString();
                if (!body.isEmpty()) {
                    String[] bodyParams = body.split("&");
                    for (String param : bodyParams) {
                        String[] keyValue = param.split("=", 2);
                        if (keyValue.length == 2) {
                            parameters.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
            }

        }

        byte[] contentBytes = bodyBuilder.toString().getBytes();

        return new RequestInfo(httpCommand, uri, uriSegments, parameters, contentBytes);
    }

    /**
     * Represents a parsed HTTP request with all its components.
     * This class provides structured access to the request's method, URI,
     * parameters, and body content.
     */
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        /**
         * Creates a new RequestInfo instance with the parsed request data.
         * 
         * @param httpCommand The HTTP method (GET, POST, DELETE)
         * @param uri The full request URI
         * @param uriSegments The URI split into segments
         * @param parameters Map of query parameters or form data
         * @param content The raw request body content
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        /**
         * Gets the HTTP method of the request.
         * @return The HTTP method (e.g., GET, POST, DELETE)
         */
        public String getHttpCommand() {
            return httpCommand;
        }

        /**
         * Gets the full request URI.
         * @return The complete URI including query parameters
         */
        public String getUri() {
            return uri;
        }

        /**
         * Gets the URI segments (path components).
         * @return Array of URI segments
         */
        public String[] getUriSegments() {
            return uriSegments;
        }

        /**
         * Gets the request parameters.
         * These can come from either the query string or form data.
         * @return Map of parameter names to values
         */
        public Map<String, String> getParameters() {
            return parameters;
        }

        /**
         * Gets the raw request body content.
         * @return Byte array containing the request body
         */
        public byte[] getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "RequestInfo{" +
                    "httpCommand='" + httpCommand + '\'' +
                    ", uri='" + uri + '\'' +
                    ", uriSegments=" + Arrays.toString(uriSegments) +
                    ", parameters=" + parameters +
                    ", content=" + Arrays.toString(content) +
                    '}';
        }
    }
}