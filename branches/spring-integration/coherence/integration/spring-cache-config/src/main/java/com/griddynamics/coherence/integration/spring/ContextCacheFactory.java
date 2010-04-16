package com.griddynamics.coherence.integration.spring;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.griddynamics.coherence.integration.spring.config.CacheMapping;
import com.griddynamics.coherence.integration.spring.config.CachingScheme;
import com.griddynamics.coherence.integration.spring.config.ContextBean;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.run.xml.SimpleElement;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.ClassHelper;

/**
 * @author Dmitri Babaev
 */
public class ContextCacheFactory extends DefaultConfigurableCacheFactory
		implements ApplicationContextAware, InitializingBean {
    
	private ApplicationContext context;

	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;
	}

	public void afterPropertiesSet() throws Exception {
		Map<String, CacheMapping> cacheMappings = context.getBeansOfType(CacheMapping.class);
		Map<String, CachingScheme> cachingSchemes = context.getBeansOfType(CachingScheme.class);
	}

	@Override
	public Object instantiateAny(CacheInfo info, XmlElement xmlClass,
			BackingMapManagerContext context, ClassLoader loader) {
		if (translateSchemeType(xmlClass.getName()) != SCHEME_CLASS) {
			throw new IllegalArgumentException("Invalid class definition: "
					+ xmlClass);
		}
		String sClass = xmlClass.getSafeElement("class-name").getString();

		if (sClass.startsWith(ContextBean.SPRING_BEAN_PREFIX)) {
			String sBeanName = sClass.substring(ContextBean.SPRING_BEAN_PREFIX.length());

			azzert(sBeanName != null && sBeanName.length() > 0,
					"Bean name required");

			XmlElement xmlParams = xmlClass.getElement("init-params");
			XmlElement xmlConfig = null;
			if (xmlParams != null) {
				xmlConfig = new SimpleElement("config");
				XmlHelper.transformInitParams(xmlConfig, xmlParams);
			}
			Object oBean = this.context.getBean(sBeanName);

			if (xmlConfig != null) {
				for (Object obj : xmlConfig.getElementList()) {
					XmlElement xmlElement = (XmlElement) obj;

					String sMethod = xmlElement.getName();
					String sParam = xmlElement.getString();
					try {
						ClassHelper.invoke(oBean, sMethod,
								new Object[] { sParam });
					} catch (Exception e) {
						ensureRuntimeException(e, "Could not invoke " + sMethod
								+ "(" + sParam + ") on bean " + oBean);
					}
				}
			}
			return oBean;
		} else {
			return super.instantiateAny(info, xmlClass, context, loader);
		}
	}
}
