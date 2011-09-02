import java.io.IOException;
import java.util.Collections;

import com.gemstone.gemfire.addon.pogo.KeyTypeManager;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.PartitionAttributesFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.IndexExistsException;
import com.gemstone.gemfire.cache.query.IndexType;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.CacheServerLauncher;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegionHelper;

import domain.ObjectX;


public class StartQueryExecutor {
	
	public static void main(String[] args) throws InterruptedException, IOException, FunctionDomainException, TypeMismatchException, NameResolutionException, QueryInvocationTargetException {
		
		System.out.println(CacheServerLauncher.class.getSimpleName() + ".PRINT_LAUNCH_COMMAND");
		
		KeyTypeManager.registerKeyType(ObjectX.getKeyType());
		
		Cache cache = new CacheFactory()
        .set("mcast-port", "0")
        .set("locators", "127.0.0.1[5555]")
        .create();
		
//		CacheServer server = cache.addCacheServer();
//		server.start();
		
		
		PartitionAttributes pa = new PartitionAttributesFactory()
        .setLocalMaxMemory(300)
        .setRedundantCopies(1)
        .setTotalNumBuckets(100).create();
		
		Region region = cache.createRegionFactory(RegionShortcut.PARTITION)
        .setPartitionAttributes(pa)
        .create("maplite");

		Region sregion = cache.createRegionFactory(RegionShortcut.PARTITION)
		.setPartitionAttributes(pa)
		.create("string");

		Region region2 = cache.createRegionFactory(RegionShortcut.PARTITION)
		.setPartitionAttributes(pa)
		.create("pogo");
		
		System.out.println("Creating index");
		try {
			cache.getQueryService().createIndex("mapliteIndex", IndexType.FUNCTIONAL, "value.get('textField')", "/maplite.values value");
			cache.getQueryService().createIndex("pogoIndex", IndexType.FUNCTIONAL, "value.getText()", "/pogo.values value");
		} catch (IndexExistsException e) {
			// ignore
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PartitionedRegion pr = ((PartitionedRegion)sregion);
		
		for(int i = 0; i != pr.getTotalNumberOfBuckets(); ++i) {
			pr.put(i, "String-" + i);
		}

		int bucketId = pr.getKeyInfo("A").getBucketId();
		Query q = cache.getQueryService().newQuery("select x from /string x");
		// run query for entries colocated with "A"
		Object rs = pr.executeQuery((DefaultQuery)q, new Object[0], Collections.singleton(bucketId));

		for(int i = 0; i != pr.getTotalNumberOfBuckets(); ++i) {
			System.out.println("Partition " + i + " -> " + rs);
		}
		
		
		while(true) {
			Thread.sleep(5000);
		}
	}
}
