package server;

import java.io.IOException;
import java.io.OutputStream;

public interface Servlet {
    void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException;
    void close() throws IOException;
}
