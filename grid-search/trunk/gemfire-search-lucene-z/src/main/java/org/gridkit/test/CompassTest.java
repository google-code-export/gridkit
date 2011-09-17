package org.gridkit.test;

import org.compass.core.*;
import org.compass.core.config.CompassConfiguration;

import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.spi.InternalCompass;
import org.gridkit.search.compass.marshall.GridkitMarshallingStrategy;

public class CompassTest {
    public static void main(String[] args) throws InterruptedException {
        CompassConfiguration configuration = new CompassConfiguration().configure("/compass.cfg.xml");

        InternalCompass compass = (InternalCompass)configuration.buildCompass();

        RuntimeCompassSettings runtimeSettings = new RuntimeCompassSettings(compass.getSettings());
        SearchEngine searchEngine = compass.getSearchEngineFactory().openSearchEngine(runtimeSettings);

        MarshallingStrategy marshallingStrategy = new GridkitMarshallingStrategy(
            compass.getMapping(), searchEngine, compass.getConverterLookup(),
            compass.getResourceFactory(), compass.getPropertyNamingStrategy()
        );

        //printProperties(marshallingStrategy.marshall(author));

        compass.close();
    }

    public static void printProperties(Resource resource) {
        for (Property property : resource.getProperties()) {
            System.out.println(property);
        }
    }
}
