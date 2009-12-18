/*
 * File: AdvancedConfigurableCacheFactory.java
 * 
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */
package com.oracle.coherence.common.configuration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.run.xml.XmlDocument;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.run.xml.XmlValue;
import com.tangosol.util.UUID;

/**
 * <p>A <strong>simple</strong> extension to the {@link DefaultConfigurableCacheFactory} that
 * provides the ability to "introduce" and override other cache configurations
 * from other files.</p>
 * 
 * <code>
 * 		<cache-config>
 * 			<introduce-cache-config file="cache-config-a.xml"/>
 * 			<introduce-cache-config file="cache-config-b.xml"/>
 * 		...
 * 		</cache-config/>
 * </code>
 * 
 * <p>There are numerous optimizations we can make here, but for now the goal is functionality.</p>
 * 
 * @author Brian Oliver
 */
public class AdvancedConfigurableCacheFactory extends DefaultConfigurableCacheFactory {

	private HashSet<String> inheritedConfigFileNames;
	private LinkedHashMap<String, XmlElement> cacheSchemeMappings;
	private LinkedHashMap<String, XmlElement> cachingSchemes;
	
	
	public AdvancedConfigurableCacheFactory() {
		super();
	}

	
	public AdvancedConfigurableCacheFactory(String path, ClassLoader loader) {
		super(path, loader);
	}

	
	public AdvancedConfigurableCacheFactory(String path) {
		super(path);
	}

	
	public AdvancedConfigurableCacheFactory(XmlElement xmlConfig) {
		super(xmlConfig);
	}

	
	@Override
	public void setConfig(XmlElement xmlConfig) {
		this.inheritedConfigFileNames = new HashSet<String>();
		this.cacheSchemeMappings = new LinkedHashMap<String, XmlElement>();
		this.cachingSchemes = new LinkedHashMap<String, XmlElement>();

		resolveIncludes(xmlConfig);

		StringBuilder builder = new StringBuilder();
		build(builder);

		XmlDocument xmlDocument = XmlHelper.loadXml(builder.toString());
		
		super.setConfig(xmlDocument);
	}


	/**
	 * <p>Resolves the &gt;include&lt; declarations in the specified {@link XmlElement} to
	 * create the set of cache mappings and cache schemes.</p>
	 *  
	 * @param xmlElement
	 */
	@SuppressWarnings("unchecked")
	private void resolveIncludes(XmlElement xmlElement) {	
		if (xmlElement != null && xmlElement.getName().equals("cache-config")) {
			
			List<XmlElement> cacheConfigElementList = (List<XmlElement>)xmlElement.getElementList();
			Iterator<XmlElement> cacheConfigElements = cacheConfigElementList.iterator();
						
			while (cacheConfigElements.hasNext()) {
				XmlElement element = cacheConfigElements.next();
				
				if (element.getName().equals("introduce-cache-config")) {
					
					XmlValue value = element.getAttribute("file");
					if (value != null) {
						String fileName = value.getString();
						if (inheritedConfigFileNames.contains(fileName)) {
							//already loaded
						} else {
							XmlDocument document = loadConfigAsResource(fileName, getContextClassLoader());
							inheritedConfigFileNames.add(fileName);
							resolveIncludes(document);
						}
					}
					
				} else if (element.getName().equals("caching-scheme-mapping")) {
					
					for (XmlElement child : (List<XmlElement>)element.getElementList()) {
						cacheSchemeMappings.put(child.getElement("cache-name").getString(""), child);
					}

				} else if (element.getName().equals("caching-schemes")) {
					
					for (XmlElement child : (List<XmlElement>)element.getElementList()) {
						String schemeName;
						XmlElement schemeNameElement = child.getElement("scheme-name");
						
						if (schemeNameElement == null) 
							schemeName = new UUID().toString();
						else
							schemeName = schemeNameElement.getString();
						
						cachingSchemes.put(schemeName, child);
					}
				}
			}
		}
	}
	
	
	/**
	 * <p>Builds a {@link String} representing an xml coherence cache configuration
	 * that has been resolved.</p>
	 * 
	 * @param builder
	 */
	private void build(StringBuilder builder) {
		builder.append("<cache-config>\n");
		
		builder.append("   <caching-scheme-mapping>\n");
		for(String cacheName : cacheSchemeMappings.keySet()) {
			build(builder, cacheSchemeMappings.get(cacheName), 2);
		}
		builder.append("   </caching-scheme-mapping>\n");
		
		builder.append("\n");
		builder.append("   <caching-schemes>\n");
		for(String schemeName : cachingSchemes.keySet()) {
			build(builder, cachingSchemes.get(schemeName), 2);
		}		
		builder.append("   </caching-schemes>\n");

		builder.append("</cache-config>\n");
	}
	
	
	/**
	 * <p>Adds a string representation of the specified {@link XmlElement} to the builder
	 * that has been resolved.</p>
	 * 
	 * @param builder
	 * @param xmlElement
	 * @param indent 
	 */
	@SuppressWarnings("unchecked")
	private void build(StringBuilder builder, XmlElement xmlElement, int indent) {	
		String padding = dup(' ', indent * 4);
		builder.append(padding);
		
		builder.append("<");
		builder.append(xmlElement.getName());
		for (String attributeName : ((Map<String, XmlValue>)xmlElement.getAttributeMap()).keySet()) {
			builder.append(" ");
			builder.append(attributeName);
			builder.append("=\"");
			builder.append(xmlElement.getAttribute(attributeName).toString());
			builder.append("\"");
		}

		if (xmlElement.getString("").trim().length() > 0) {
			builder.append(">");
			builder.append(xmlElement.getString("").trim());
			builder.append("</");
			builder.append(xmlElement.getName());
			builder.append(">\n");
			
		} else if (xmlElement.getElementList().size() == 0 && xmlElement.getString("").trim().length() == 0) {
			builder.append("/>\n");
				
		} else {	
			builder.append(">\n");
			for(XmlElement xml : (List<XmlElement>)xmlElement.getElementList()) {
				build(builder, xml, indent+1);
			}
			if (xmlElement.getString("").length() == 0)
				builder.append(padding);
			builder.append("</");
			builder.append(xmlElement.getName());
			builder.append(">\n");
		}
	}
}
