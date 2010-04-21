import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.tangosol.net.NamedCache;


public class SimpleApp {

	public static void main(String[] args) {
		ApplicationContext context = new GenericXmlApplicationContext("classpath:/test-context.xml");
		NamedCache cache = context.getBean("simpleDistributedCache", NamedCache.class);
		cache.put("aaa", "bbb");
	}
}
