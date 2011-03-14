package com.medx.framework.proxy.handler;

import java.lang.reflect.Method;

import com.medx.framework.annotation.ModelPackage;
import com.medx.framework.annotation.handler.NounForm;
import com.medx.framework.annotation.handler.Plural;
import com.medx.framework.annotation.handler.Singular;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.metadata.TypedAttrKey;
import com.medx.framework.util.DictUtil;
import com.medx.framework.util.ReflectionUtil;
import com.medx.framework.util.TextUtil;

public abstract class AbstractMethodHandlerFactory implements MethodHandlerFactory {
	protected final ModelMetadata modelMetadata;
	
	public AbstractMethodHandlerFactory(ModelMetadata modelMetadata) {
		this.modelMetadata = modelMetadata;
	}
	
	protected MethodInfo getMethodInfo(Method method) {
		ClassNounInfo classNounInfo = getClassNounInfo(method.getDeclaringClass());
		
		String verb = TextUtil.getCamelPrefix(method.getName());
		String attrName = TextUtil.getCamelPostfix(method.getName());
		NounForm nounForm = classNounInfo.getNounForm(attrName);
		TypedAttrKey attrKey = classNounInfo.getAttrKey(attrName);
		Class<?>[] parameterTypes = method.getParameterTypes();
		
		return new MethodInfo(verb, attrName, nounForm, attrKey, parameterTypes);
	}
	
	protected ClassNounInfo getClassNounInfo(Class<?> clazz) {
		ClassNounInfo result = new ClassNounInfo();
		
		Package modelPacket = ReflectionUtil.getModelPackage(clazz.getPackage());
		
		String modelPackageName = modelPacket.getName();
		ModelPackage modelPackage = modelPacket.getAnnotation(ModelPackage.class);
		
		for (Method getter : ReflectionUtil.getGetters(clazz)) {
			String getterName = TextUtil.getCamelPostfix(getter.getName());
			
			String modelAttrName = DictUtil.getAttrName(modelPackage, modelPackageName, clazz.getCanonicalName(), getterName);
			
			TypedAttrKey attrKey = modelMetadata.getAttrKey(modelAttrName);
			
			if (!hasNounlForm(getter)) 
				result.getAttrKeyByUnknown().put(getterName, attrKey);
			else {
				result.getAttrKeyByPlural().put(getPluralForm(getter), attrKey);
				result.getAttrKeyBySingular().put(getSingularForm(getter), attrKey);
			}
		}
		
		return result;
	}

	private static boolean hasNounlForm(Method getter) {
		return getter.getAnnotation(Plural.class) != null || getter.getAnnotation(Singular.class) != null;
	}
	
	private static String getPluralForm(Method getter) {
		Plural plural = getter.getAnnotation(Plural.class);
		
		return plural != null ? plural.value() : TextUtil.getCamelPostfix(getter.getName());
	}
	
	private static String getSingularForm(Method getter) {
		Singular singular = getter.getAnnotation(Singular.class);
		
		return singular != null ? singular.value() : TextUtil.getCamelPostfix(getter.getName());
	}
}
