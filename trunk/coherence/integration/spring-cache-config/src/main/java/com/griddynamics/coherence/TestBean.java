package com.griddynamics.coherence;

import com.tangosol.net.NamedCache;
import com.tangosol.run.xml.SimpleParser;
import com.tangosol.util.Filter;
import com.tangosol.util.QueryMap;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.ChainedExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.EqualsFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class TestBean {

    public static void main(String[] args) {
        ApplicationContext factory = new ClassPathXmlApplicationContext("app-context.xml");

        System.out.println("*************************************************************");
        System.out.print("Getting NamedCahce bean...   ");
        NamedCache namedCache = (NamedCache) factory.getBean("cache_1");
        System.out.println("OK");

//        System.out.print("Putting value to cache...   ");
//        namedCache.put("key", "value");
//        System.out.println("OK");
//        System.out.print("Getting value from cache...  ");
//        System.out.println(namedCache.get("key"));

        namedCache.put("Vasiliy Bikiv", new Address("Rogova", "Uryupkinskaya", "Saratov", "Saratovskaya", "410002", "Russia"));
        namedCache.put("Sergey Tryuber", new Address("Zelenaya", "Uryupkinskaya", "Volgograd", "Saratovskaya", "410002", "Russia"));
        namedCache.put("Igor Davidov", new Address("Baltiyskaya", "Uryupkinskaya", "Samara", "Saratovskaya", "410002", "Russia"));
        namedCache.put("Fedor Pushkin", new Address("Gromova", "Uryupkinskaya", "Saratov", "Saratovskaya", "410002", "Russia"));
        namedCache.put("Ilya Rogov", new Address("Stolnaya", "Uryupkinskaya", "Tomsk", "Saratovskaya", "410002", "Russia"));
        namedCache.put("Stanislav Bochkin", new Address("Rogova", "Uryupkinskaya", "Budapesht", "Saratovskaya", "410002", "Russia"));


//        ValueExtractor extractor = new ReflectionExtractor("getCity");
//        Filter filter = new EqualsFilter(extractor, "Saratov");
//        ValueExtractor extractor = new ReflectionExtractor("getCountry");
//        Filter filter = new EqualsFilter(extractor, "Russia");

//        for (Iterator iter = namedCache.entrySet(filter).iterator(); iter.hasNext();) {
////        for (Iterator iter = namedCache.entrySet().iterator(); iter.hasNext();) {
//            Map.Entry entry = (Map.Entry) iter.next();
//            System.out.println("Found:\n" + entry.getValue());
//        }

    }
}

