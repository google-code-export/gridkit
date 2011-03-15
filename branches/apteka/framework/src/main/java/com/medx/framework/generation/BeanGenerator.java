package com.medx.framework.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.medx.framework.bean.Bean;
import com.medx.framework.bean.BeanManager;
import com.medx.framework.generation.impl.CollectionGenerator;
import com.medx.framework.generation.impl.EnumGenerator;
import com.medx.framework.generation.impl.ListGenerator;
import com.medx.framework.generation.impl.MapGenerator;
import com.medx.framework.generation.impl.SetGenerator;
import com.medx.framework.generation.impl.StandardAndPrimitiveGenerator;
import com.medx.framework.metadata.ClassKey;
import com.medx.framework.metadata.ClassKeyType;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.metadata.TypedAttrKey;

public class BeanGenerator implements Generator<Map<Integer, Object>>, GenerationContext {
	private static final Map<ClassKeyType, Generator<?>> generators = new HashMap<ClassKeyType, Generator<?>>();
	
	static {
		StandardAndPrimitiveGenerator standardAndPrimitiveGenerator = new StandardAndPrimitiveGenerator();
		
		generators.put(ClassKeyType.PRIMITIVE, standardAndPrimitiveGenerator);
		generators.put(ClassKeyType.STANDARD, standardAndPrimitiveGenerator);
		
		generators.put(ClassKeyType.ENUM, new EnumGenerator());
		
		generators.put(ClassKeyType.COLLECTION, new CollectionGenerator());
		generators.put(ClassKeyType.SET, new SetGenerator());
		generators.put(ClassKeyType.LIST, new ListGenerator());
		generators.put(ClassKeyType.MAP, new MapGenerator());
	}
	
	private final ModelMetadata modelMetadata;
	private final BeanManager beanManager;
	
	private final Set<Class<?>> classStack = new HashSet<Class<?>>();
	
	public BeanGenerator(ModelMetadata modelMetadata, BeanManager beanManager) {
		this.modelMetadata = modelMetadata;
		this.beanManager = beanManager;
	}

	public List<Bean> generate(Class<?> javaClass) {
		classStack.clear();
		
		List<Bean> result = new ArrayList<Bean>();
		
		for (Map<Integer, Object> beanMap : generate(modelMetadata.getClassKey(javaClass), this))
			result.add(beanManager.<Bean>createBean(beanMap));
		
		return result;
	}

	@Override
	public List<Map<Integer, Object>> generate(ClassKey classKey, GenerationContext context) {
		if (context.getClassStack().contains(classKey.getJavaClass()))
			return Collections.singletonList(null);
		
		context.getClassStack().add(classKey.getJavaClass());

		Map<Integer, Object> prototype = new HashMap<Integer, Object>();
		
		prototype.put(BeanManager.BEAN_KEY, Boolean.TRUE);
		prototype.put(classKey.getId(), Boolean.TRUE);
		
		List<Map<Integer, Object>> oldGen = new ArrayList<Map<Integer,Object>>(Collections.singletonList(prototype));
		List<Map<Integer, Object>> newGen = new ArrayList<Map<Integer,Object>>();
		
		for (TypedAttrKey attrKey : modelMetadata.getAttrKeys(classKey.getJavaClass())) {
			for (Object attr : context.getGenerator(attrKey.getClassKey()).generate(attrKey.getClassKey(), context))
				for (Map<Integer, Object> oldBean : oldGen) {
					Map<Integer, Object> bean = new HashMap<Integer, Object>(oldBean);
					bean.put(attrKey.getId(), attr);
					newGen.add(bean);
				}
		
			oldGen = newGen;
			newGen = new ArrayList<Map<Integer,Object>>();
		}

		context.getClassStack().remove(classKey.getJavaClass());
		
		return oldGen;
	}

	@Override
	public Generator<?> getGenerator(ClassKey classKey) {
		if (classKey.getType() == ClassKeyType.USER)
			return this;
		else
			return generators.get(classKey.getType());
	}
	
	@Override
	public Set<Class<?>> getClassStack() {
		return classStack;
	}
}
