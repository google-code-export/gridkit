package org.gridkit.gemfire.search.test;

import org.compass.core.*;
import org.compass.core.config.CompassConfiguration;
import java.util.Date;

import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.spi.InternalCompass;
import org.gridkit.search.compass.marshall.GridkitMarshallingStrategy;
import org.gridkit.gemfire.search.demo.model.Author;

public class CompassTest {
    static Author author = new Author();

    static {
        author.setId(0);
        author.setName("The quick brown fox jumped over the lazy dogs");
        author.setBirthday(new Date());
    }

    public static void main(String[] args) throws InterruptedException {
        CompassConfiguration configuration = new CompassConfiguration().configure("/compass.cfg.xml");

        InternalCompass compass = (InternalCompass)configuration.buildCompass();

        RuntimeCompassSettings runtimeSettings = new RuntimeCompassSettings(compass.getSettings());
        SearchEngine searchEngine = compass.getSearchEngineFactory().openSearchEngine(runtimeSettings);

        MarshallingStrategy marshallingStrategy = new GridkitMarshallingStrategy(
            compass.getMapping(), searchEngine, compass.getConverterLookup(),
            compass.getResourceFactory(), compass.getPropertyNamingStrategy()
        );

        printProperties(marshallingStrategy.marshall(author));

        compass.close();
    }

    public static void printProperties(Resource resource) {
        for (Property property : resource.getProperties()) {
            System.out.println(property);
        }
    }
}
