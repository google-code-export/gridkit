import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import com.gemstone.gemfire.distributed.Locator;


public class StartLocator {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		InetAddress addr = InetAddress.getByName("127.0.0.1");
//		InetAddress addr = InetAddress.getByName("0.0.0.0");
		Properties props = new Properties();
		
		Locator.startLocatorAndDS(5555, new File("locator.bin"), props);
		for(Locator locator : Locator.getLocators()) {
			System.out.println("Locator: ");
			System.out.println("  " + locator.toString());
		}
		while(true) {
			Thread.sleep(5000);
		}
	}
	
}
