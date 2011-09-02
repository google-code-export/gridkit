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


public class StartMapLiteClient {

	public static void main(String[] args) throws IOException {
		
//		Locator.startLocator(5556, new File("client-locator.bin"));

		KeyTypeManager.registerKeyType(ObjectX.getKeyType());
		
		ClientCache cache = new ClientCacheFactory()
        .set("mcast-port", "0")
        //.set("locators", "127.0.0.1[5555]")
        .create();
		
		
//		PoolFactory poolFactory = PoolManager.createFactory();
//		poolFactory.addLocator("localhost", 5555);
//		Pool pool = poolFactory.create("remote");		
		
		Region region = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("maplite");
		
		System.out.println("Reading object v1");
		System.out.println("maplite.v1 -> " + region.get("maplite.v1"));

		System.out.println("Reading object v2");
		System.out.println("maplite.v2 -> " + region.get("maplite.v2"));
		
		System.out.println("Writing object v1");
		
		MapLite obj = new MapLite(ObjectX.getKeyType());
		obj.put(ObjectX.textField, "text.v1");
		obj.put(ObjectX.intField, 10);
		region.put("maplite.v1", obj);
		
		System.out.println("Done");
	}
	
}
