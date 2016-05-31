package net.javacoding.jspider.core.util;

import lombok.extern.slf4j.Slf4j;
import net.javacoding.jspider.core.Spider;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * <p><tt>TestSlack</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class TestSlack {
    private Log log = LogFactory.getLog( TestSlack.class );

//    @Test
    public void sentSlack() {
        sendMessage( "Test", "@chrisstillwell" );
    }

//    @Test
    public void sentSlackUsingGet() throws IOException {

        String channel = "@chrisstillwell";
        String sender = "jspider@test";
        String message = "Test message using GetMethod";
        GetMethod method = new GetMethod( String.format("https://slack.com/api/chat.postMessage?token=xoxp-6525398036-6525294374-12436352048-88ceba57d1&channel=%s&username=%s&pretty=1&text=%s",
                                                        URLEncoder.encode( channel ),
                                                        URLEncoder.encode( sender ),
                                                        URLEncoder.encode( message) )
                                                        );
        HttpClient httpClient = new HttpClient();
        httpClient.executeMethod( method );
        System.out.println("============ ResponseBodyAsString ============");
        String xmlResponse = method.getResponseBodyAsString();
        System.out.println( xmlResponse );
    }


    private int sendMessage( String message, String channel ) {
        String sender;
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            sender = "jspider@" + hostName;
        } catch ( UnknownHostException e ) {
            log.warn( e.getMessage(), e );
            sender = "jspider";
        }


        log.info( "Sending to : " + channel + " message:" + message );

//        String command = String.format("curl -X POST --data-urlencode 'payload={\"text\":\"%s\"}' %s", message, webhookUrl);
        String command = String.format("curl \"https://slack.com/api/chat.postMessage?token=xoxp-6525398036-6525294374-12436352048-88ceba57d1&channel=%s&username=%s&pretty=1\" --data-urlencode \"text=%s\"",
                                       URLEncoder.encode( channel ),
                                       URLEncoder.encode( sender ),
                                       URLEncoder.encode( message) );
        String[] commands = { "bash", "-c", command };

        try {
            Process process = Runtime.getRuntime().exec( commands );
            int exitCode = process.waitFor();

            String line;

            BufferedReader error = new BufferedReader( new InputStreamReader( process.getErrorStream(), "UTF-8" ) );
            while ( (line = error.readLine()) != null ) {
                log.info( line );
            }
            error.close();

            BufferedReader input = new BufferedReader( new InputStreamReader( process.getInputStream(), "UTF-8" ) );
            while ( (line = input.readLine()) != null ) {
                log.info( line );
            }

            input.close();

            OutputStream outputStream = process.getOutputStream();
            PrintStream printStream = new PrintStream( outputStream, false, "UTF-8" );
            printStream.println();
            printStream.flush();
            printStream.close();

            return exitCode;
        } catch ( Exception e ) {
            throw new RuntimeException( "failed to executeDDL: " + message, e );
        }
    }

    @Test
    public void loadSlackProperties() throws Exception {
        String urlString = "http://config-server/configuration-web/jspider-slack.properties";
        URL url = new URL( urlString);
        InputStream inputStream = url.openStream();
        Properties slackProperties = new Properties(  );
        slackProperties.load( inputStream );
        slackProperties.list( System.out );
        String slackToken = slackProperties.getProperty( "token" );
        String slackChannel = slackProperties.getProperty( "channel" );
        System.out.printf("slackToken=%s%nslackChannel=%s%n", slackToken, slackChannel );
    }

}
