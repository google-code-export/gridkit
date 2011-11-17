package org.gridkit.flugram;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
			this.parent = parent;
			this.callSite = callSite;
			this.returnFI = returnFI;
		}

		@SuppressWarnings("rawtypes")
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			if (isPushAction(method)) {
				// non term token
				
				try {
					// TODO lookup any one-arg method
					Method term = lookupTermMethod(method.getName(), new Class[]{Object.class});
					
					Method handlerProvider = lookupHandlerProvider(method.getName(), method.getParameterTypes());
					
					AstNodeHandler<?> h = (AstNodeHandler<?>) handlerProvider.invoke(handler, args);
					
					AstNode nested = new AstNode(h, this, term, returnType(proxy, method));
					
					System.out.println("PUSH: " + method.getName() + " - syntatic scope " + returnType(proxy, method) + ", handler " + h.getClass());

					return newState(method.getReturnType(), nested);
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			else if (isReplaceAction(method)) {
				// non term token
				
				try {
					// TODO lookup any one-arg method
					Method term = lookupTermMethod(method.getName(), new Class[]{Object.class});
					
					Method handlerProvider = lookupHandlerProvider(method.getName(), method.getParameterTypes());
					
					AstNodeHandler<?> h = (AstNodeHandler<?>) handlerProvider.invoke(handler, args);
					
					AstNode nested = new AstNode(h, null, term, returnType(proxy, method));
					
					System.out.println("REPLACE: " + method.getName() + " - syntatic scope " + returnType(proxy, method) + ", handler " + h.getClass());
					
					return newState(method.getReturnType(), nested);
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			else if (isPopAction(method)) {
				// end to clause
				
				Method m = evaluateMethod(handler, method);
				Object node = m.invoke(handler, args);

				System.out.println("POP: " + method.getName() );
				
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
				
				System.out.println("TERM: " + method.getName() + " on " + handler.getClass());
				
				if (method.getGenericReturnType() instanceof TypeVariable) {
					return newState(returnType(proxy, method), this);
				}
				else {
					return newState(method.getReturnType(), this);
				}				
			}
		}

		private Method evaluateMethod(AstNodeHandler<?> h, Method method) {
			Method m;
			try {
				m = h.getClass().getMethod(method.getName(), method.getParameterTypes());
				m.setAccessible(true);
			} 
			catch (NoSuchMethodException e) {
				throw new RuntimeException("No eval method '" + method.getName() + "' at " + h.getClass());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			return m;
		}

		private Class returnType(Object proxy, Method method) {
			Class[] interfaces = proxy.getClass().getInterfaces();
			Class returnFI = inferReturnType(lookupTopInterface(interfaces, method));
			return returnFI;
		}


		private boolean isPushAction(Method method) {
			return method.isAnnotationPresent(AstPush.class);
		}

		private boolean isPopAction(Method method) {
			return method.isAnnotationPresent(AstPop.class);
		}

		private boolean isReplaceAction(Method method) {
			return method.isAnnotationPresent(AstReplace.class);
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
			}
			catch (NoSuchMethodException e) {
				throw new RuntimeException("Term methof '" + name + "' is not found in " + handler.getClass());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	static Class lookupTopInterface(Class[] interfaces, Method m) {
		for(Class i : interfaces) {
			for(Method cm: i.getMethods()) {
				if (cm.equals(m)) {
					return i;
				}
			}
		}
		throw new RuntimeException("Cannot locate host class for " + m + " in " + Arrays.toString(interfaces));
	}
	
	// package visibility for testing
	@SuppressWarnings("rawtypes")
	static Class inferReturnType(Class topClass) {
		Class syntScope = inferSyntaticScope(topClass);
		if (syntScope == null) {
			throw new IllegalArgumentException("Cannot derive syntatic scope for " + topClass);
		}		
		return syntScope;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Class inferSyntaticScope(Type type) {
		if (type instanceof Class) {
			SimpleTypeResolver resolver = new SimpleTypeResolver();
			resolver.bind((Class)type);
			return resolver.findSyntaticScope();
		}
		else if (type instanceof ParameterizedType) {
			SimpleTypeResolver resolver = new SimpleTypeResolver();
			resolver.bind((ParameterizedType)type, Collections.EMPTY_MAP);
			return resolver.findSyntaticScope();			
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		List<SimpleTypeVar> synonims = new ArrayList<SimpleTypeVar>();
		
	}
	
	static class SimpleTypeBinding {
		Class<?> rawClass;
		SimpleTypeVar[] parameters;
		Map<TypeVariable<?>, SimpleTypeVar> varBinding = new HashMap<TypeVariable<?>, SimpleTypeVar>();
	}
	
	static class SimpleTypeResolver {

		Map<Class<?>, SimpleTypeBinding> bindings =  new HashMap<Class<?>, FluentDSL.SimpleTypeBinding>();
		
		@SuppressWarnings("rawtypes")
		void bind(ParameterizedType x, Map<TypeVariable<?>, SimpleTypeVar> scope) {
						
			Class rawClass = (Class)x.getRawType();
			
			SimpleTypeBinding binding;
			bind(rawClass);

			binding = bindings.get(rawClass);				
		
			for(int n = 0; n != x.getActualTypeArguments().length; ++n) {
				Type arg = x.getActualTypeArguments()[n];
				binding.parameters[n].bindings.add(arg);
				if (arg instanceof TypeVariable) {
					SimpleTypeVar tv = scope.get(arg);
					tv.toString(); // NPE check
					tv.synonims.add(binding.parameters[n]);
					binding.parameters[n].synonims.add(tv);
				}
			}			
		}
		
		@SuppressWarnings("rawtypes")
		void bind(Class type) {
			
			Class rawClass = type;
			
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
			
			binding = bindings.get(rawClass);
			
			for(Type x: rawClass.getGenericInterfaces()) {
				if (x instanceof ParameterizedType) {
					bind((ParameterizedType) x, binding.varBinding);
				}
				else {
					bind((Class)x);
				}
			}			
		}		
		
		Class<?> findSyntaticScope() {
			SimpleTypeBinding tb = bindings.get(SyntScope.class);
						
			HashSet<SimpleTypeVar> checked = new HashSet<FluentDSL.SimpleTypeVar>();
			HashSet<SimpleTypeVar> queue = new HashSet<FluentDSL.SimpleTypeVar>();
			checked.add(tb.parameters[0]);
			queue.add(tb.parameters[0]);
			while(!queue.isEmpty()) {
				SimpleTypeVar var = queue.iterator().next();
				queue.remove(var);
				
				for(Type t : var.bindings) {
					if (t instanceof Class) {
						return (Class<?>)t;
					}
					else if (t instanceof ParameterizedType) {
						return (Class<?>)((ParameterizedType)t).getRawType();
					}
				}
				
				for(SimpleTypeVar a : var.synonims) {
					if (checked.add(a)) {
						queue.add(a);
					}
				}
			}
			return null;			
		}
	}
}
