package org.gridkit.flugram;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

public class FluentDSL {
	
	static boolean TRACE = Boolean.getBoolean("org.gridkit.flugram.trace");

	@SuppressWarnings("unchecked")
	public static <T, V> T newNode(Class<T> fi, AstNodeHandler<V> nodeHandler) {
		AstNode node = new AstNode(nodeHandler);
		return (T)newState(new TrackedType(fi), node);
	}

	private static Object newState(TrackedType type, AstNode node) {
		Class<?> fi = type.getRawType();
		node.runtimeType = type;
		return Proxy.newProxyInstance(fi.getClassLoader(), new Class[]{fi}, node);
	}
	
	private static class AstNode implements InvocationHandler {
		
		private AstNode parent;
		private Method callSite;
		private AstNodeHandler<?> handler;
		
		private TrackedType runtimeType;

		public AstNode(AstNodeHandler<?> handler) {
			this.handler = handler;
		}
		
		public AstNode(AstNodeHandler<?> handler, AstNode parent, Method callSite) {
			this.handler = handler;
			this.parent = parent;
			this.callSite = callSite;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			if (isPushAction(method)) {
				// non term token				
				try {
					// TODO lookup any one-arg method
					Method term = lookupTermMethod(method.getName(), new Class[]{Object.class});
					Method handlerProvider = lookupHandlerProvider(method.getName(), method.getParameterTypes());
					AstNodeHandler<?> h = (AstNodeHandler<?>) handlerProvider.invoke(handler, args);
					AstNode nested = new AstNode(h, this, term);
					
					if (TRACE) {
						System.out.println(ident() + "PUSH: " + method.getName() + ", handler " + h.getClass());
					}

					TrackedType type = runtimeType.resolve(method.getGenericReturnType());
					return newState(type, nested);
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}				
			}
			else if (isReplaceAction(method)) {
				// non term token				
				try {
					// TODO lookup any one-arg method
					Method handlerProvider = lookupHandlerProvider(method.getName(), method.getParameterTypes());
					AstNodeHandler<?> h = (AstNodeHandler<?>) handlerProvider.invoke(handler, args);
					AstNode nested = new AstNode(h, parent, callSite);
					
					if (TRACE) {
						System.out.println(ident() + "REPLACE: " + method.getName() + ", handler " + h.getClass());
					}
					
					TrackedType type = runtimeType.resolve(method.getGenericReturnType());
					return newState(type, nested);
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			else if (isPopAction(method)) {
				// end to clause
				
				Method m = evaluateMethod(handler, method);
				Object node = m.invoke(handler, args);

				if (TRACE) {
					System.out.println(ident() + "POP: " + method.getName() );
				}
				
				if (parent == null) {
					return node;
				}
				else {
					parent.pop(callSite, node);
					TrackedType type = runtimeType.resolve(method.getGenericReturnType());
					return newState(type, parent);
				}				
			}
			else {
				// regular term
				
				Method m = lookupTermMethod(method.getName(), method.getParameterTypes());
				m.invoke(handler, args);
				
				System.out.println(ident() + "TERM: " + method.getName() + " on " + handler.getClass());

				TrackedType type = runtimeType.resolve(method.getGenericReturnType());
				return newState(type, this);
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

		private String ident() {
			StringBuilder b = new StringBuilder();
			for(int i = 0; i != depth(); ++i) {
				b.append(' ');
			}
			return b.toString();
		}
		
		private int depth() {
			AstNode p = parent;
			int n = 0;
			while(p != null) {
				++n;
				p = p.parent;
			}
			return n;
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
				throw new RuntimeException("Term method '" + name + "' is not found in " + handler.getClass());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
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
	
	@SuppressWarnings({"rawtypes"})
	static class TrackedType {
		
		private boolean delayed = false;
		
		private Type lazyType;
		private TrackedType lazyScope;
		
		private Class rawType;
		private Map<TypeVariable<?>, TrackedType> variables = new HashMap<TypeVariable<?>, FluentDSL.TrackedType>();
		
		public TrackedType(Class type) {
			rawType = type;
			bindVariables(type);
		}

		TrackedType(ParameterizedType type, TrackedType scope) {
			rawType = (Class) type.getRawType();
			bindVariables(type, scope);
		}
		
		TrackedType(Type type, TrackedType scope, boolean delayed) {
			this.delayed = delayed;
			lazyType = type;
			lazyScope = scope;
			if (delayed) {
				delayed = true;
			}
			else {
				init();
			}
		}
		
		private void init() {
			delayed = false;
			if (lazyType instanceof Class) {
				rawType = (Class) lazyType;
				bindVariables((Class) lazyType);
			}
			else if (lazyType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) lazyType;
				rawType = (Class) pt.getRawType();
				bindVariables(pt, lazyScope);
			}
			else {
				throw new IllegalArgumentException("Unknown " + lazyType);
			}			
		}

		private void bindVariables(Class type) {
			for(Type tp : type.getGenericInterfaces()) {
				if (tp instanceof Class) {
					bindVariables((Class)tp);
				}
				else if (tp instanceof ParameterizedType) {
					bindVariables((ParameterizedType) tp, this);
				}
				else {
					throw new IllegalArgumentException("Unexpeted type " + tp);
				}
			}
		}
		
		private void bindVariables(ParameterizedType tp, TrackedType scope) {
			TypeVariable<?>[] vars = ((Class)tp.getRawType()).getTypeParameters();
			Type[] actual = tp.getActualTypeArguments();
			for(int i = 0; i != vars.length; ++i) {
				TypeVariable<?> v = vars[i];
				Type t = actual[i];
				if (t instanceof WildcardType) {
					t = ((WildcardType)t).getUpperBounds()[0];
				}
				variables.put(v, scope.resolve(t));
			}
			
			bindVariables((Class)tp.getRawType());
		}

		public Class getRawType() {
			if (delayed) {
				init();
			}
			return rawType;
		}
		
		public TrackedType resolve(Type var) {
			if (delayed) {
				init();
			}
			if (var instanceof Class) {
				return new TrackedType((Class)var, null, true);
			}
			else if (var instanceof TypeVariable<?>) {
				TrackedType tracked = variables.get(var);
				if (tracked == null) {
					throw new IllegalArgumentException("Failed to resolve type " + var);
				}
				return tracked;
			}
			else if (var instanceof ParameterizedType) {
				return new TrackedType(((ParameterizedType) var), this, true);
			}
			else {
				throw new IllegalArgumentException("Cannot resolve " + var);
			}
		}
	}
}
