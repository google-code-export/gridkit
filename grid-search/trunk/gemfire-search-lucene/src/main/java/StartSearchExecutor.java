import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.gridkit.search.gemfire.SearchService;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.gemstone.gemfire.internal.cache.CacheServerLauncher;


public class StartSearchExecutor {
	
	public static void main(String[] args) throws InterruptedException, IOException, FunctionDomainException, TypeMismatchException, NameResolutionException, QueryInvocationTargetException {
		
		System.out.println(CacheServerLauncher.class.getSimpleName() + ".PRINT_LAUNCH_COMMAND");
		
		Cache cache = new CacheFactory()
        .set("mcast-port", "0")
        .set("locators", "127.0.0.1[5555]")
        .create();
		
//		CacheServer server = cache.addCacheServer();
//		server.start();

		PhraseQuery q = new PhraseQuery();
		q.add(new Term("search", "fox"));
		Set keySet = SearchService.getInstance().keySet("fake", q);
		System.out.println("Query result: " +keySet);		
	}
}
