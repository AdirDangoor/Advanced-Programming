import server.HTTPServer;
import server.MyHTTPServer;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;
import servlets.ConfLoader;
import utils.Logger;

public class Main {
    public static void main(String[] args) throws Exception{
        // Set debug mode - change to false to disable debug logging
        
        int port = 8080;
        boolean debug = false;
        Logger.info("Starting server");

        HTTPServer server=new MyHTTPServer(port,5);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));

        server.start();
        Logger.info("Server is running!");
        Logger.info("Access the application at: http://localhost:" + port + "/app/");
        
        Logger.setDebugMode(debug);

        System.in.read();
        server.close();
        Logger.setDebugMode(true);
        Logger.info("Server stopped");
    }
}