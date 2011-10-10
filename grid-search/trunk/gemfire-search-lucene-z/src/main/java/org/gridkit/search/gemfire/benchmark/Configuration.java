package org.gridkit.search.gemfire.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
    private static Logger log = LoggerFactory.getLogger(Configuration.class);
    
    public static final String locationProperty = "properties.location";
    public static final String defaultLocation = "benchmark.properties";

    private final Properties properties;
    
    public final String locatorHost;
    public final Integer locatorPort;

    public final String bindAddress;

    public final String ftsDataFolder;

    public final Integer warmUpCount;

    public Configuration() throws IOException {
        this(System.getProperty(locationProperty, defaultLocation));
    }
    
    public Configuration(String propertiesLocation) throws IOException {
        this(loadProperties(propertiesLocation));
    }
    
    public Configuration(Properties properties) throws UnknownHostException {
        this.properties = properties;
        
        String localAddress = InetAddress.getLocalHost().getHostAddress();

        this.locatorHost = getStringProperty("locator.host", localAddress);
        this.locatorPort = getIntegerProperty("locator.port", 5555);

        this.bindAddress = getStringProperty("bind.address", localAddress);

        this.ftsDataFolder = getStringProperty("fts.data.folder");

        this.warmUpCount = getIntegerProperty("warm.up.count", 1);
    }
    
    public String getStringProperty(String name, String defaultValue) {
        String result = System.getProperty(name);
        
        if (result != null)
            return result;
        else
            return properties.getProperty(name, defaultValue);
    }
    
    public Integer getIntegerProperty(String name, Integer defaultValue) {
        String value = getStringProperty(name);
        
        if (value == null)
            return defaultValue;
        else
            return Integer.valueOf(value);
    }
    
    public String getStringProperty(String name) {
        return getStringProperty(name, null);
    }
    
    public Integer getIntegerProperty(String name) {
        return getIntegerProperty(name, null);
    }

    private static Properties loadProperties(String propertiesLocation) {
        Properties properties = new Properties();

        try {
            InputStream input = (new File(propertiesLocation)).toURI().toURL().openStream();
            properties.load(input);
            input.close();
        } catch (Throwable t) {
            log.warn("properties load error", t);
            return new Properties();
        }
        
        return properties;
    }
}
