package com.medx.framework.annotation.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AttributeAccessHandler {
	String verb();
	Class<?> attributeType();
	NoonForm noonForm() default NoonForm.DEFAULT;
}
