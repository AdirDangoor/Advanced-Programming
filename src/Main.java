import server.HTTPServer;
import server.MyHTTPServer;
import servlets.HelloServlet;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;
import servlets.ConfLoader;

public class Main {
    public static void main(String[] args) throws Exception{
        System.out.print("Hello and welcome!\n");

        HTTPServer server=new MyHTTPServer(8080,5);
        server.addServlet("GET", "/", new HelloServlet());

        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app", new HtmlLoader("html_files"));

        server.start();
        System.in.read();
        server.close();
        System.out.println("done");

    }
}