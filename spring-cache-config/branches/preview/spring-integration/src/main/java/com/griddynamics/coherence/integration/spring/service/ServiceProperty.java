package com.griddynamics.coherence.integration.spring.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceProperty {
	String value();
}
