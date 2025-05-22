package server;

import java.io.*;
import java.util.*;

public class RequestParser {

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

        System.out.println("Request line: " + requestLine);
        System.out.println("HTTP command: " + httpCommand);
        System.out.println("URI: " + uri);

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
            System.out.println("Header line: " + line);
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }
        System.out.println("Finished reading headers");

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

    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

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