package com.griddynamics.coherence.support;

import java.io.IOException;
import java.util.Iterator;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.run.xml.SimpleElement;
import com.tangosol.run.xml.SimpleParser;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.ClassHelper;


/**
 * SpringAwareCacheFactory provides a facility to access caches declared
 * in a "cache-config.dtd" compliant configuration file, similar to its super
 * class {@link DefaultConfigurableCacheFactory}.  In addition, this factory
 * provides the ability to reference beans in a Spring application context
 * via the use of a class-scheme element.
 * <p/>
 * <p>This factory can be configured to start its own Spring application
 * context from which to retrieve these beans.  This can be useful for stand-alone
 * JVMs such as cache servers.  It can also be configured at runtime with a
 * pre-configured Spring bean factory.  This can be useful for Coherence
 * applications running in an environment that is itself responsible for starting
 * the Spring bean factory, such as a web container.
 *
 * @author pperalta Jun 14, 2007
 * @see #instantiateAny(CacheInfo, XmlElement,
 *      BackingMapManagerContext, ClassLoader)
 */
public class SpringAwareCacheFactory
        extends DefaultConfigurableCacheFactory
        implements BeanFactoryAware {
    // ----- constructors -------------------------------------------------

    /**
     * Construct a default DefaultConfigurableCacheFactory using the
     * default configuration file name.
     */
//    public SpringAwareCacheFactory() {
//        super();
//    }

    SpringAwareCacheFactory(String xmlConfig) throws IOException {
        super(new SimpleParser().parseXml(xmlConfig));
    }

    /**
     * Construct a SpringAwareCacheFactory using the specified path to
     * a "cache-config.dtd" compliant configuration file or resource.  This
     * will also create a Spring ApplicationContext based on the supplied
     * path to a Spring compliant configuration file or resource.
     *
     * @param sCacheConfig location of a cache configuration
     * @param sAppContext  location of a Spring application context
     */
//    public SpringAwareCacheFactory(String sCacheConfig, String sAppContext) {
//        super(sCacheConfig);
//
//        azzert(sAppContext != null && sAppContext.length() > 0,
//                "Application context location required");
//
//        m_beanFactory = sCacheConfig.startsWith("file:") ? (BeanFactory)
//                new FileSystemXmlApplicationContext(sAppContext) :
//                new ClassPathXmlApplicationContext(sAppContext);
//
//        // register a shutdown hook so the bean factory cleans up
//        // upon JVM exit
//        ((AbstractApplicationContext) m_beanFactory).registerShutdownHook();
//    }

    /**
     * Construct a SpringAwareCacheFactory using the specified path to
     * a "cache-config.dtd" compliant configuration file or resource and
     * the supplied Spring BeanFactory.
     *
     * @param sPath       the configuration resource name or file path
     * @param beanFactory Spring BeanFactory used to load Spring beans
     */
//    public SpringAwareCacheFactory(String sPath, BeanFactory beanFactory) {
//        super(sPath);
//
//        m_beanFactory = beanFactory;
//    }


    // ----- extended methods -----------------------------------------------

    /**
     * Create an Object using the "class-scheme" element.
     * <p/>
     * In addition to the functionality provided by the super class,
     * this will retreive an object from the configured Spring BeanFactory
     * for class names that use the following format:
     * <pre>
     * &lt;class-name&gt;spring-bean:sampleCacheStore&lt;/class-name&gt;
     * </pre>
     * <p/>
     * Parameters may be passed to these beans via setter injection as well:
     * <pre>
     *   &lt;init-params&gt;
     *     &lt;init-param&gt;
     *       &lt;param-name&gt;setEntityName&lt;/param-name&gt;
     *       &lt;param-value&gt;{cache-name}&lt;/param-value&gt;
     *     &lt;/init-param&gt;
     *   &lt;/init-params&gt;
     * </pre>
     * <p/>
     * Note that Coherence will manage the lifecycle of the instantiated Spring
     * bean, therefore any beans that are retreived using this method should be
     * scoped as a prototype in the Spring configuration file, for example:
     * <pre>
     *   &lt;bean id="sampleCacheStore"
     *         class="com.company.SampleCacheStore"
     *         scope="prototype"/&gt;
     * </pre>
     *
     * @param info     the cache info
     * @param xmlClass "class-scheme" element.
     * @param context  BackingMapManagerContext to be used
     * @param loader   the ClassLoader to instantiate necessary classes
     * @return a newly instantiated Object
     * @see DefaultConfigurableCacheFactory#instantiateAny(
     *CacheInfo, XmlElement, BackingMapManagerContext, ClassLoader)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Object instantiateAny(CacheInfo info, XmlElement xmlClass,
                                 BackingMapManagerContext context, ClassLoader loader) {
       if (translateSchemeType(xmlClass.getName()) != SCHEME_CLASS) {
            throw new IllegalArgumentException(
                    "Invalid class definition: " + xmlClass);
        }
        String sClass = xmlClass.getSafeElement("class-name").getString();

        if (sClass.startsWith(SPRING_BEAN_PREFIX)) {
            String sBeanName = sClass.substring(SPRING_BEAN_PREFIX.length());

            azzert(sBeanName != null && sBeanName.length() > 0,
                    "Bean name required");

            XmlElement xmlParams = xmlClass.getElement("init-params");
            XmlElement xmlConfig = null;
            if (xmlParams != null) {
                xmlConfig = new SimpleElement("config");
                XmlHelper.transformInitParams(xmlConfig, xmlParams);
            }
            Object oBean = getBeanFactory().getBean(sBeanName);

            if (xmlConfig != null) {
                for (Iterator iter = xmlConfig.getElementList().iterator(); iter.hasNext();) {
                    XmlElement xmlElement = (XmlElement) iter.next();

                    String sMethod = xmlElement.getName();
                    String sParam = xmlElement.getString();
                    try {
                        ClassHelper.invoke(oBean, sMethod, new Object[]{sParam});
                    }
                    catch (Exception e) {
                        ensureRuntimeException(e, "Could not invoke " + sMethod +
                                "(" + sParam + ") on bean " + oBean);
                    }
                }
            }
            return oBean;
        } else {
            return super.instantiateAny(info, xmlClass, context, loader);
        }
    }

    /**
     * Get the Spring BeanFactory used by this CacheFactory
     *
     * @return the Spring {@link BeanFactory} used by this CacheFactory
     */
    public BeanFactory getBeanFactory() {
        azzert(m_beanFactory != null, "Spring BeanFactory == null");
        return m_beanFactory;
    }

    /**
     * Set the Spring BeanFactory used by this CacheFactory
     *
     * @param beanFactory the Spring {@link BeanFactory} used by this CacheFactory
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
        //Object oBean = getBeanFactory().getBean("entityCacheStore");
    }


    // ----- data fields ----------------------------------------------------

    /**
     * Spring BeanFactory used by this CacheFactory
     */
    private BeanFactory m_beanFactory;

    /**
     * Prefix used in cache configuration "class-name" element to indicate
     * this bean is in Spring
     */
    private static final String SPRING_BEAN_PREFIX = "spring-bean:";
}
