package org.gridkit.coherence.store;

import org.gridkit.coherence.util.classloader.Isolate;
import org.gridkit.coherence.util.classloader.NodeActions;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class ReplicationCheckApp {
	
	public static class DataUpload implements Runnable {
		@Override
		public void run() {
			NamedCache cache = CacheFactory.getCache("store-cache");
			
			BatchStoreUploader uploader = new BatchStoreUploader(cache);
			uploader.put("1", "test");
			uploader.put("2", "test2");
			uploader.flush();
		}
	}
	
	public static class DataRead implements Runnable {
		@Override
		public void run() {
			NamedCache cache = CacheFactory.getCache("store-cache");
			
			System.out.println(cache.get("1"));
			System.out.println(cache.get("2"));
		}
	}
	
	public static void main(String[] args) {
		Isolate storage1 = new Isolate("storage-node-1", "com.tangosol", "org.gridkit");
		storage1.start();
        Isolate storage2 = new Isolate("storage-node-2", "com.tangosol", "org.gridkit");
        storage2.start();
        Isolate client = new Isolate("client", "com.tangosol", "org.gridkit");
        client.start();

        System.setProperty("org.gridkit.batch-store-uploader.partition-count", "1");
        storage1.submit(NodeActions.Start.class, "batch-uploader-test-cache-config.xml");
        storage1.submit(NodeActions.GetService.class, "DistributedStore");
        
        System.setProperty("tangosol.coherence.distributed.localstorage", "false");
        client.submit(NodeActions.Start.class, "batch-uploader-test-cache-config.xml");
        client.submit(DataUpload.class);

        System.setProperty("tangosol.coherence.distributed.localstorage", "true");
        storage2.submit(NodeActions.Start.class, "batch-uploader-test-cache-config.xml");
        storage2.submit(NodeActions.GetService.class, "DistributedStore");
        
        try {
			Thread.sleep(10000); //waiting for partition backup to be copied to the second node
		} catch (InterruptedException e) {
		}
        
        storage1.submit(NodeActions.Crash.class, 0);
        
        client.submit(DataRead.class);
        
        client.stop();
        storage2.stop();
	}
}
