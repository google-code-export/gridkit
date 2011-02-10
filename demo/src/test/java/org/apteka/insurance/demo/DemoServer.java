package org.apteka.insurance.demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Ignore;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.FileResource;

@Ignore
public class DemoServer {
    private static final String CONTEXT_PATH = "/demo";

    private static final int HTTP_PORT = 9090;
    private static final String HTTP_HOST = "localhost";

    public static void main(String[] args) throws Exception {
        Server server = new Server();

        server.addConnector(createConnector());
        server.setHandler(createContext());

        server.setStopAtShutdown(true);

        server.start();
        server.join();
    }

    public static WebAppContext createContext() throws IOException, URISyntaxException {
        WebAppContext wac = new WebAppContext();

        //wac.setDefaultsDescriptor("./src/test/resources/webdefault.xml");

        wac.setContextPath(CONTEXT_PATH);
        wac.setBaseResource(new FileResource(new URL("file:./src/main/webapp/")));

        return wac;
    }

    public static Connector createConnector() {
        Connector connector = new SelectChannelConnector();

        connector.setPort(HTTP_PORT);
        connector.setHost(HTTP_HOST);

        return connector;
    }
}
