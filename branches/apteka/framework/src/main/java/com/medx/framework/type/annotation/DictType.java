package com.medx.framework.type.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictType {
	String packageCutPrefix() default "";
	String xmlAddPrefix() default "";
	String javaAddPrefix() default "";
}
