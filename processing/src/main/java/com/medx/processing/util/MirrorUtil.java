package com.medx.processing.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.util.ClassUtil;
import com.medx.framework.util.DictUtil;
import com.medx.framework.util.TextUtil;

public class MirrorUtil {
	private static String GETTER_PATTERN  = "get[A-Z].*";
	
	public static String getEnvOption(String option, ProcessingEnvironment processingEnv) {
		return processingEnv.getOptions().get(option);
	}
	
	public List<TypeElement> filterDictTypes(Set<? extends Element> dictTypes, PackageElement modelPackage) {
		List<TypeElement> result = new ArrayList<TypeElement>();
		
		String packageName = modelPackage.getQualifiedName().toString();
		
		for (Element element : dictTypes)
			if (element instanceof TypeElement) {
				TypeElement typeElement = (TypeElement)element;
				
				if (!packageName.isEmpty() && typeElement.getQualifiedName().toString().startsWith(packageName + "."))
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
	
	private List<AttributeDescriptor> mapAttributeDescriptors(List<ExecutableElement> getters, PackageElement modelPackage) {
		return null;
	}
	
	
	public static AttributeDescriptor createAttributeDescriptor(ExecutableElement getter, PackageElement modelPackage) {
		AttributeDescriptor result = new AttributeDescriptor();
		
		String attrName = TextUtil.getCamelPostfix(getter.getSimpleName().toString());
		result.setName(DictUtil.getAttrName((TypeElement)getter.getEnclosingElement(), modelPackage, attrName));
		
		result.setClazz(ClassUtil.replacePrimitiveType(getter.getReturnType().toString()));
		
		return result;
	}
}
