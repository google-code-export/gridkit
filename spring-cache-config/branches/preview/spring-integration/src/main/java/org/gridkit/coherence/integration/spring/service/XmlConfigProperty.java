package org.gridkit.coherence.integration.spring.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface XmlConfigProperty {
	String value();
}
