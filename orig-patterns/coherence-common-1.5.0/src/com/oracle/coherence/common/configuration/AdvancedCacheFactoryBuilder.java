
package com.oracle.coherence.common.configuration;


import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultCacheFactoryBuilder;

import com.tangosol.run.xml.XmlElement;


/**
* CacheFactoryBuilder implementation that creates instances of
* AdvancedConfigurableCacheFactory.
*
* @author jh  2009.08.20
*/
public class AdvancedCacheFactoryBuilder
        extends DefaultCacheFactoryBuilder
    {
    // ----- constructors ---------------------------------------------------

    /**
    * Default constructor.
    */
    public AdvancedCacheFactoryBuilder()
        {
        super();
        }


    // ----- DefaultCacheFactoryBuilder overrides ---------------------------

    /**
    * {@inheritDoc}
    */
    public ConfigurableCacheFactory getSingletonFactory()
        {
        return super.getSingletonFactory();
        }

    /**
    * {@inheritDoc}
    */
    public void setSingletonFactory(ConfigurableCacheFactory factory)
        {
        super.setSingletonFactory(factory);
        }

    /**
    * {@inheritDoc}
    */
    protected ConfigurableCacheFactory buildFactory(XmlElement xmlConfig, ClassLoader loader)
        {
        return new AdvancedConfigurableCacheFactory(xmlConfig);
        }
    }
