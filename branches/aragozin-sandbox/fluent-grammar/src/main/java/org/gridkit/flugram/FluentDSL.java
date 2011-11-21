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
		GenericContext genCtx = new GenericContext(fi);
		return (T)newState(fi, node, genCtx);
	}

	private static Object newState(Class<?> fi, AstNode node, GenericContext gc) {
		node.genContext = gc;
		return Proxy.newProxyInstance(fi.getClassLoader(), new Class[]{fi}, node);
	}
	
	private static class AstNode implements InvocationHandler {
		
		private AstNode parent;
		private Method callSite;
		private AstNodeHandler<?> handler;
		
		private GenericContext genContext;

		public AstNode(AstNodeHandler<?> handler) {
			this.handler = handler;
		}
		
		public AstNode(AstNodeHandler<?> handler, AstNode parent, Method callSite) {
			this.handler = handler;
			this.parent = parent;
			this.callSite = callSite;
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
					
					AstNode nested = new AstNode(h, this, term);
					
					System.out.println("PUSH: " + method.getName() + " - syntatic scope " + returnType(proxy, method) + ", handler " + h.getClass());

					GenericContext nextCtx = genContext.derive(method.getGenericReturnType());
					return newState(method.getReturnType(), nested, nextCtx);
					
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
					
					AstNode nested = new AstNode(h, null, term);
					
					System.out.println("REPLACE: " + method.getName() + " - syntatic scope " + returnType(proxy, method) + ", handler " + h.getClass());
					
					GenericContext nextCtx = genContext.derive(method.getGenericReturnType());
					return newState(method.getReturnType(), nested, nextCtx);
					
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
					Class returnFI = genContext.resolveToClass(method.getGenericReturnType());
					GenericContext nextCtx = genContext.derive(method.getGenericReturnType());
					return newState(returnFI, parent, nextCtx);
				}				
			}
			else {
				// regular term
				
				Method m = lookupTermMethod(method.getName(), method.getParameterTypes());
				m.invoke(handler, args);
				
				System.out.println("TERM: " + method.getName() + " on " + handler.getClass());

				Class returnFI = genContext.resolveToClass(method.getGenericReturnType());
				GenericContext nextCtx = genContext.derive(method.getGenericReturnType());
				return newState(returnFI, this, nextCtx);
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
			TypeResolver resolver = new TypeResolver();
			resolver.bind((Class)type);
			return resolver.findSyntaticScope();
		}
		else if (type instanceof ParameterizedType) {
			TypeResolver resolver = new TypeResolver();
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
	
	static class TypeVar {		
		
		BindClause resolution;		
		List<Type> bindings = new ArrayList<Type>();
		List<TypeVar> synonims = new ArrayList<TypeVar>();
		
	}
	
	static class BindClause {
		Class<?> rawClass;
		TypeVar[] parameters;
		Map<String, TypeVar> varBinding = new HashMap<String, TypeVar>();
	}
	
	static class TypeResolver {

		Map<Class<?>, BindClause> bindings =  new HashMap<Class<?>, FluentDSL.BindClause>();
		
		@SuppressWarnings("rawtypes")
		void bind(ParameterizedType x, Map<String, TypeVar> scope) {
						
			Class rawClass = (Class)x.getRawType();
			
			BindClause binding;
			bind(rawClass);

			binding = bindings.get(rawClass);				
		
			for(int n = 0; n != x.getActualTypeArguments().length; ++n) {
				Type arg = x.getActualTypeArguments()[n];
				binding.parameters[n].bindings.add(arg);
				if (arg instanceof TypeVariable) {
					TypeVar tv = scope.get(((TypeVariable) arg).getName());
					tv.toString(); // NPE check
					tv.synonims.add(binding.parameters[n]);
					binding.parameters[n].synonims.add(tv);
				}
			}			
		}
		
		@SuppressWarnings("rawtypes")
		void bind(Class type) {
			
			Class rawClass = type;
			
			BindClause binding;
			if (!bindings.containsKey(rawClass)) {
				binding = new BindClause();
				binding.rawClass = rawClass;
				binding.parameters = new TypeVar[rawClass.getTypeParameters().length];

				for(int n = 0; n != binding.rawClass.getTypeParameters().length; ++n) {
					binding.parameters[n] = new TypeVar();
					binding.parameters[n].bindings.add(binding.rawClass.getTypeParameters()[n]);
					
					TypeVariable<?> tv = binding.rawClass.getTypeParameters()[n];
					binding.varBinding.put(tv.getName(), binding.parameters[n]);
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
			BindClause tb = bindings.get(SyntScope.class);
			return resolve(tb.parameters[0]);
		}
		
		static Class<?> resolve(TypeVar x) {

			HashSet<TypeVar> checked = new HashSet<FluentDSL.TypeVar>();
			HashSet<TypeVar> queue = new HashSet<FluentDSL.TypeVar>();
			checked.add(x);
			queue.add(x);
			while(!queue.isEmpty()) {
				TypeVar var = queue.iterator().next();
				queue.remove(var);
				
				for(Type t : var.bindings) {
					if (t instanceof Class) {
						return (Class<?>)t;
					}
					else if (t instanceof ParameterizedType) {
						return (Class<?>)((ParameterizedType)t).getRawType();
					}
				}
				
				for(TypeVar a : var.synonims) {
					if (checked.add(a)) {
						queue.add(a);
					}
				}
			}
			return null;			
		}
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	static class GenericContext {
		
		Class rootClass;
		TypeResolver resolution;
		
		TypeVariable<?> rootVar;
		TypeVar rootVarResolution;
		
		public GenericContext(Type type) {
			this(type, Collections.EMPTY_MAP);
		}

		public GenericContext(TypeVariable<?> rv, TypeVar rvr) {
			this.rootVar = rv;
			this.rootVarResolution = rvr;
		}
		
		public GenericContext(Type type, Map<String, TypeVar> vars) {
			resolution = new TypeResolver();
			if (type instanceof Class) {
				rootClass = (Class) type;
				resolution.bind((Class)type);
			}
			else if (type instanceof ParameterizedType) {
				rootClass = (Class) ((ParameterizedType)type).getRawType();
				resolution.bind((ParameterizedType)type, vars);
			}
			else {
				throw new IllegalArgumentException("Unsuitable " + type);
			}
		}
		
		public Class resolveToClass(Type type) {
			if (type instanceof Class) {
				return (Class)type;
			}
			else if (type instanceof ParameterizedType) {
				return (Class) ((ParameterizedType)type).getRawType();
			}
			else if (type instanceof TypeVariable) {
				TypeVar tv = resolve((TypeVariable)type);
				tv.getClass(); // NPE check
				return TypeResolver.resolve(tv);
			}
			else {
				throw new IllegalArgumentException("Unsuitable type " + type);
			}
		}
		
		public TypeVar resolve(TypeVariable<?> var) {
			return getVarMap().get(var.getName());
		}

		private Map<String, TypeVar> getVarMap() {
			if (rootClass != null) {
				return resolution.bindings.get(rootClass).varBinding;
			}
			else {
				return (Map)Collections.singletonMap(rootVar, rootVarResolution);
			}
		}

		public GenericContext derive(Type type) {
			if (type instanceof Class) {
				return new GenericContext(type);
			}
			else if (type instanceof ParameterizedType) {
				return new GenericContext(type, getVarMap());
			}
			else if (type instanceof TypeVariable<?>) {
				TypeVariable<?> name = (TypeVariable<?>) type;
				TypeVar res = resolve(name);
				return new GenericContext(name, res);
			}
			else {
				throw new IllegalArgumentException("Unsuitable type " + type);
			}
		}
	}
}
