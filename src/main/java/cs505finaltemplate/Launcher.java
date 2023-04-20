package cs505finaltemplate;

import cs505finaltemplate.CEP.CEPEngine;
import cs505finaltemplate.Topics.TopicConnector;
import cs505finaltemplate.embeddedDB.DBEngine;
import cs505finaltemplate.graphDB.GraphDBEngine;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.*;


public class Launcher {

    public static GraphDBEngine graphDBEngine;
    public static DBEngine embedded;
    public static String inputStreamName;
    public static CEPEngine cepEngine;
    public static TopicConnector topicConnector;
    public static final int WEB_PORT = 9999;

    public static String lastCEPOutput = "{}";
    public static ArrayList<String> alerts = new ArrayList<>();
    public static Map<String, Integer> CEPList = new HashMap<>();

    public static void main(String[] args) throws IOException {


        //starting DB/CEP init
        System.out.println("Starting Embedded Database...");
        //Embedded database initialization
        embedded = new DBEngine();
        System.out.println("Embedded Database Started...");

        //READ CLASS COMMENTS BEFORE USING
        graphDBEngine = new GraphDBEngine();
        graphDBEngine.clearDB();

        cepEngine = new CEPEngine();

        System.out.println("Starting CEP...");

        inputStreamName = "testInStream";
        String inputStreamAttributesString = "zip_code string";

        String outputStreamName = "testOutStream";
        String outputStreamAttributesString = "zip_code string, count long";

        //This query must be modified.  Currently, it provides the last zip_code and total count
        //You want counts per zip_code, to say another way "grouped by" zip_code
        String queryString = " " +
            "from testInStream#window.timeBatch(15 sec) " +
            "select zip_code, count() as count " +
            "group by zip_code " +
            "insert into testOutStream; ";

        cepEngine.createCEP(inputStreamName, outputStreamName, inputStreamAttributesString, outputStreamAttributesString, queryString);

        System.out.println("CEP Started...");
        //end DB/CEP Init

        //start message collector
        Map<String,String> message_config = new HashMap<>();
        message_config.put("hostname","vbu231.cs.uky.edu"); //Fill config for your team in
        message_config.put("port","9099"); //
        message_config.put("username","team_4");
        message_config.put("password","myPassCS505");
        message_config.put("virtualhost","4");

        topicConnector = new TopicConnector(message_config);
        topicConnector.connect();
        //end message collector

        //Embedded HTTP initialization
        startServer();

        try {
            while (true) {
                Thread.sleep(5000);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void startServer() throws IOException {

        final ResourceConfig rc = new ResourceConfig()
        .packages("cs505finaltemplate.httpcontrollers");
        //.register(AuthenticationFilter.class);

        System.out.println("Starting Web Server...");
        URI BASE_URI = UriBuilder.fromUri("http://0.0.0.0/").port(WEB_PORT).build();
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);

        try {
            httpServer.start();
            System.out.println("Web Server Started...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
