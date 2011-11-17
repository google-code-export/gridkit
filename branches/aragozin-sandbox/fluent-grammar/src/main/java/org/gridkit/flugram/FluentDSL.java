package org.gridkit.flugram;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluentDSL {

	public static <T, V> T newNode(Class<T> fi, AstNodeHandler<V> nodeHandler) {
		AstNode node = new AstNode(nodeHandler);
		return (T)newState(fi, node);
	}

	private static Object newState(Class<?> fi, AstNode node) {
		return Proxy.newProxyInstance(fi.getClassLoader(), new Class[]{fi}, node);
	}
	
	private static class AstNode implements InvocationHandler {
		
		private AstNode parent;
		private Method callSite;
		private Class returnFI;
		private AstNodeHandler<?> handler;

		public AstNode(AstNodeHandler<?> handler) {
			this.handler = handler;
		}
		
		public AstNode(AstNodeHandler<?> handler, AstNode parent, Method callSite, Class returnFI) {
			this.handler = handler;
			this.callSite = callSite;
			this.returnFI = returnFI;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			if (isPushAction(method)) {
				// non term token
				
				try {
					// TODO lookup any one-arg method
					Method term = lookupTermMethod(method.getName(), new Class[]{Object.class});
					
					Method handlerProvider = lookupHandlerProvider(method.getName(), method.getParameterTypes());
					
					AstNodeHandler<?> h = (AstNodeHandler<?>) handlerProvider.invoke(handler, args);
					
					Class returnFI = inferReturnType(method);
					
					AstNode nested = new AstNode(handler, this, term, returnFI);
					
					return newState(method.getReturnType(), nested);
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			else if (isPopAction(method)) {
				// end to clause
				
				Object node = handler.evaluate();
				
				if (parent == null) {
					return node;
				}
				else {
					parent.pop(callSite, node);
					return newState(returnFI, parent);
				}				
			}
			else {
				// regular term
				
				Method m = lookupTermMethod(method.getName(), method.getParameterTypes());
				m.invoke(handler, args);
				return newState(method.getReturnType(), this);
			}
		}


		private boolean isPushAction(Method method) {
			return method.isAnnotationPresent(AstPush.class);
		}

		private boolean isPopAction(Method method) {
			return method.isAnnotationPresent(AstPop.class);
		}

		public void pop(Method term, Object node) {
			try {
				term.invoke(handler, new Object[]{node});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private Method lookupHandlerProvider(String name, Class<?>[] parameterTypes) {			
			return lookupTermMethod(name, parameterTypes);
		}

		private Method lookupTermMethod(String name, Class<?>[] parameterTypes) {
			try {
				Method m = handler.getClass().getMethod(name, parameterTypes);
				m.setAccessible(true);
				return m;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static Class lookupTopInterface(Class[] interfaces, Method m) {
		for(Class i : interfaces) {
			for(Method cm: i.getMethods()) {
				if (cm == m) {
					return i;
				}
			}
		}
		throw new RuntimeException("Cannot locate host class for " + m + " in " + Arrays.toString(interfaces));
	}
	
	// package visibility for testing
	static Class inferReturnType(Class topClass, Method method) {
		Type genRT = method.getGenericReturnType();
		if (genRT instanceof TypeVariable) {
			
		}
		else if (genRT instanceof ParameterizedType) {
			ParameterizedType ptrt = (ParameterizedType) genRT;
			Class c = ptrt.getClass();
			for(Type i : c.getGenericInterfaces()) {
				if (isAssignable)
			}
		}
		
		return null;
	}

	static Class inferSyntaticScope(Type type) {
		if (type instanceof Class) {
			
		}
		else if (type instanceof ParameterizedType) {
			
		}
	}
	
	static boolean isAssignable(Class rtType, Type typeVar) {
		if (typeVar instanceof Class) {
			return rtType.isAssignableFrom((Class) typeVar);
		}
		else if (typeVar instanceof ParameterizedType) {
			return rtType.isAssignableFrom((Class)((ParameterizedType)typeVar).getRawType());
		}
		else if (typeVar instanceof TypeVariable) {
			return isAssignable(rtType, ((TypeVariable) typeVar).getBounds()[0]);
		}
		else {
			return false;
		}
	}
	
	static class SimpleTypeVar {		
		
		SimpleTypeBinding resolution;		
		List<Type> bindings = new ArrayList<Type>();
		
	}
	
	static class SimpleTypeBinding {
		Class rawClass;
		SimpleTypeVar[] parameters;
		Map<TypeVariable<?>, SimpleTypeVar> varBinding = new HashMap<TypeVariable<?>, SimpleTypeVar>();
	}
	
	static class SimpleTypeResolutionScope {

		Map<Class, SimpleTypeBinding> bindings;

		public resolveRoot(ParameterizedType rootBinding) {
			
			SimpleTypeBinding binding = new SimpleTypeBinding();			
			binding.rawClass = (Class)rootBinding.getRawType();
			
			binding.parameters = new SimpleTypeVar[binding.rawClass.getTypeParameters().length]; 
						
			for(int n = 0; n != binding.rawClass.getTypeParameters().length; ++n) {
				binding.parameters[n] = new SimpleTypeVar();
				binding.parameters[n].bindings.add(binding.rawClass.getTypeParameters()[n]);
				
				TypeVariable<?> tv = binding.rawClass.getTypeParameters()[n];
				binding.varBinding.put(tv, binding.parameters[n]);
			}

			for(int n = 0; n != rootBinding.getActualTypeArguments().length; ++n) {
				Type x = rootBinding.getActualTypeArguments()[n];
				binding.parameters[n].bindings.add(x);
			}
			
			bindings.put(binding.rawClass, binding);
			
			for(Type x: binding.rawClass.getGenericInterfaces()) {
				if (x instanceof ParameterizedType) {
					bind((ParameterizedType) x, binding.parameters);
				}
				else {
					bind(x);
				}
			}
		}

		private void bind(ParameterizedType x, SimpleTypeVar[] parameters) {
						
			Class rawClass = (Class)x.getRawType();
			
			SimpleTypeBinding binding;
			if (!bindings.containsKey(rawClass)) {
				binding = new SimpleTypeBinding();
				binding.rawClass = rawClass;
				binding.parameters = new SimpleTypeVar[rawClass.getTypeParameters().length];

				for(int n = 0; n != binding.rawClass.getTypeParameters().length; ++n) {
					binding.parameters[n] = new SimpleTypeVar();
					binding.parameters[n].bindings.add(binding.rawClass.getTypeParameters()[n]);
					
					TypeVariable<?> tv = binding.rawClass.getTypeParameters()[n];
					binding.varBinding.put(tv, binding.parameters[n]);
				}
				
				bindings.put(binding.rawClass, binding);
			}
			else {
				binding = bindings.get(rawClass);				
			}
						
			
		}
		
		private void bind(Class x) {
			
			Class rawClass = x;
			
			SimpleTypeBinding binding;
			if (!bindings.containsKey(rawClass)) {
				binding = new SimpleTypeBinding();
				binding.rawClass = rawClass;
				b
			}
						
		}				
	}
}
