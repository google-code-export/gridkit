import java.io.File;
import java.io.IOException;

import com.gemstone.gemfire.addon.pogo.KeyTypeManager;
import com.gemstone.gemfire.addon.pogo.MapLite;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.distributed.Locator;

import domain.ObjectX;
import domain.ObjectY;


public class StartPOGOClient {

	public static void main(String[] args) throws IOException {
		

		KeyTypeManager.registerKeyType(ObjectX.getKeyType());
		
		ClientCache cache = new ClientCacheFactory()
        .set("mcast-port", "0")
        .create();
		
		
		Region region = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("pogo");
		
		System.out.println("Reading object v1");
		System.out.println("pogo.v1 -> " + region.get("pogo.v1"));

		System.out.println("Reading object v2");
		System.out.println("pogo.v2 -> " + region.get("pogo.v2"));
		
		System.out.println("Writing object v1");

		ObjectY obj = new ObjectY("pogo.v1", 10);
		region.put("pogo.v1", obj);
		
		System.out.println("Done");
	}
	
}
