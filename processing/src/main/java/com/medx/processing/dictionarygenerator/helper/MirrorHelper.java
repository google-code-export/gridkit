package com.medx.processing.dictionarygenerator.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import com.medx.framework.annotation.ModelPackage;
import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.TypeDescriptor;
import com.medx.framework.util.ClassUtil;
import com.medx.framework.util.DictUtil;
import com.medx.framework.util.TextUtil;

public class MirrorHelper {
	private static String GETTER_PATTERN  = "get[A-Z].*";
	
	public static String getEnvOption(String option, ProcessingEnvironment processingEnv) {
		return processingEnv.getOptions().get(option);
	}
	
	public static List<TypeElement> filterDictTypes(Set<? extends Element> dictTypes, String packageName) {
		List<TypeElement> result = new ArrayList<TypeElement>();
		
		for (Element element : dictTypes)
			if (element instanceof TypeElement) {
				TypeElement typeElement = (TypeElement)element;
				
				if (ClassUtil.isClassInPackage(typeElement.getQualifiedName().toString(), packageName))
					result.add(typeElement);
			}
		
		return result;
	}
	
	public static List<ExecutableElement> filterExecutableElements(List<? extends Element> elements) {
		List<ExecutableElement> result = new ArrayList<ExecutableElement>();
		
		for (Element element : elements)
			if (element instanceof ExecutableElement)
				result.add((ExecutableElement)element);
		
		return result;
	}
	
	public static List<ExecutableElement> filterGetters(List<ExecutableElement> elements) {
		List<ExecutableElement> result = new ArrayList<ExecutableElement>();
		
		for (ExecutableElement element : elements)
			if (isGetter(element))
				result.add(element);
		
		return result;
	}
	
	public static boolean isGetter(ExecutableElement element) {
		if (element.getParameters().size() > 0)
			return false;

		TypeKind typeKind = element.getReturnType().getKind();
		
		if (!(typeKind.isPrimitive() || typeKind == TypeKind.DECLARED || typeKind == TypeKind.ARRAY))
			return false;
		
		if (!element.getSimpleName().toString().matches(GETTER_PATTERN))
			return false;
		
		return true;
	}
	
	public static List<AttributeDescriptor> mapAttributeDescriptors(List<ExecutableElement> getters, String modelPackageName, ModelPackage modelPackage) {
		List<AttributeDescriptor> result = new ArrayList<AttributeDescriptor>();
		
		for(ExecutableElement getter : getters)
			result.add(createAttributeDescriptor(getter, modelPackageName, modelPackage));
		
		return result;
	}
	
	public static AttributeDescriptor createAttributeDescriptor(ExecutableElement getter, String modelPackageName, ModelPackage modelPackage) {
		AttributeDescriptor result = new AttributeDescriptor();
		
		String attrName = TextUtil.getCamelPostfix(getter.getSimpleName().toString());
		String className = ((TypeElement)getter.getEnclosingElement()).getQualifiedName().toString();
		
		result.setName(DictUtil.getAttrName(modelPackage, modelPackageName, className, attrName));
		
		result.setClazz(ClassUtil.replacePrimitiveType(getter.getReturnType().toString()));
		
		return result;
	}
	
	public static TypeDescriptor createTypeDescriptor(TypeElement clazz) {
		TypeDescriptor result = new TypeDescriptor();
		
		result.setJavaClassName(clazz.getQualifiedName().toString());
		
		return result;
	}
}
