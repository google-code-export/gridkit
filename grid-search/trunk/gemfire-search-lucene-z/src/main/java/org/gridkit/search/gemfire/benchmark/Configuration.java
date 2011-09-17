package org.gridkit.search.gemfire.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Configuration {
    public static final String locationProperty = "properties.location";
    public static final String defaultLocation = "benchmark.properties";

    public final String locatorHost;
    public final Integer locatorPort;

    public final String bindAddress;

    public final String ftsDataFolder;

    public Configuration(Properties properties) throws UnknownHostException {
        String localAddress = InetAddress.getLocalHost().getHostAddress();

        this.locatorHost = properties.getProperty("locator.host", localAddress);
        this.locatorPort = Integer.valueOf(properties.getProperty("locator.port", "5555"));

        this.bindAddress = properties.getProperty("bind.address", localAddress);

        this.ftsDataFolder = properties.getProperty("fts.data.folder");
    }

    public Configuration(String propertiesLocation) throws IOException {
        this(loadProperties(propertiesLocation));
    }

    public Configuration() throws IOException {
        this(System.getProperty(locationProperty, defaultLocation));
    }

    private static Properties loadProperties(String propertiesLocation) throws IOException {
        InputStream input = (new File(propertiesLocation)).toURI().toURL().openStream();

        Properties properties = new Properties();
        properties.load(input);

        input.close();
        return properties;
    }
}
