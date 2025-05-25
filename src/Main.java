import server.HTTPServer;
import server.MyHTTPServer;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;
import servlets.ConfLoader;
import utils.Logger;

public class Main {
    public static void main(String[] args) throws Exception{
        // Set debug mode - change to false to disable debug logging
        Logger.setDebugMode(false);
        
        Logger.info("Starting server");

        HTTPServer server=new MyHTTPServer(8080,5);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));

        server.start();
        System.in.read();
        server.close();
        Logger.info("Server stopped");
    }
}