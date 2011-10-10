package org.gridkit.search.gemfire.benchmark;

import com.gemstone.gemfire.distributed.Locator;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.Callable;

public class LocatorNode implements Callable<Void> {
    @Override
    public Void call() throws Exception {
        Configuration config = new Configuration();

        InetAddress locatorAddress = InetAddress.getByName(config.locatorHost);

        Properties properties = new Properties();
		properties.setProperty("mcast-port", "0");
        //properties.setProperty("disable-tcp", "true");

        Locator.startLocatorAndDS(config.locatorPort, null, locatorAddress, properties);

        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        (new LocatorNode()).call();
        Thread.sleep(Long.MAX_VALUE);
    }
}
