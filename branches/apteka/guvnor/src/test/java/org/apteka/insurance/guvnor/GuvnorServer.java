package org.apteka.insurance.guvnor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.openejb.core.CoreUserTransaction;
import org.junit.Ignore;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.plus.naming.Transaction;
import org.mortbay.jetty.webapp.WebAppContext;

@Ignore
public class GuvnorServer {
	private static final String CONTEXT_PATH = "/guvnor";
	
    private static final    int HTTP_PORT = 8080;
    private static final String HTTP_HOST = "localhost";
    
    public static void main(String[] args) throws Exception {
    	System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
  
    	setUpEJB();
    	
    	Server server = new Server();
    	
        server.addConnector(createConnector());
        server.setHandler(createContext());
        
        server.setStopAtShutdown(true);
        
        server.start();
        server.join();
	}

    public static WebAppContext createContext() throws IOException, URISyntaxException {
        WebAppContext wac = new WebAppContext();

        wac.setContextPath(CONTEXT_PATH);
        wac.setWar(getGuvnorServerWarPath());
        
        return wac;
    }
    
    public static Connector createConnector() {
        Connector connector = new SelectChannelConnector();
        
        connector.setPort(HTTP_PORT);
        connector.setHost(HTTP_HOST);
        
        return connector;
    }

    public static void setUpEJB() throws NamingException {
    	InitialContext ic = new InitialContext();
    	TransactionManager tm = (TransactionManager) ic.lookup("openejb:TransactionManager");
    	CoreUserTransaction cut = new CoreUserTransaction(tm);
    	new Transaction(cut);
    }
    
    public static String getGuvnorServerWarPath() {
    	File dir = new File("./target/");
    	
    	return dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".war");
		}})[0].getAbsolutePath();
    }
}
