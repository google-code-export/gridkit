package org.gridkit.gemfire.search.demo;

import com.gemstone.gemfire.distributed.Locator;

import static org.gridkit.gemfire.search.demo.DemoFactory.*;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class LocatorNode implements Callable<Void> {
    private CountDownLatch locatorLatch;

    public LocatorNode(CountDownLatch locatorLatch) {
        this.locatorLatch = locatorLatch;
    }

    @Override
    public Void call() throws Exception {
        InetAddress inetAddress = InetAddress.getByName(locatorHost);

        Properties properties = new Properties();
		properties.setProperty("mcast-port", "0");

        Locator.startLocatorAndDS(locatorPort, null, inetAddress, properties);

        locatorLatch.countDown();

        return null;
    }
}
