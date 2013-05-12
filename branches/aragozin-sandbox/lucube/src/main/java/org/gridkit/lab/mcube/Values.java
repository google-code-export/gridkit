package org.gridkit.lab.mcube;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.gridkit.lab.tentacle.Sample;

public class Values {

	public static <S extends Sample> Value field(Class<S> type, String name) {
		if (!type.isInterface() || !(Sample.class.isAssignableFrom(type))) {
			throw new IllegalArgumentException("Type should be an interface extending Sample");
		}
		Method m;
		try {
			m = type.getMethod(name);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		Class<?> ft = m.getReturnType();
		if (ft == void.class) {
			throw new IllegalArgumentException("void method " + name + " cannot be field");
		}
		return new FieldValue(m);
	}

	private static ThreadLocal<FieldBinder<Sample>> BINDER = new ThreadLocal<FieldBinder<Sample>>();
	
	public static Value capture(Object obj) {
		FieldBinder<Sample> binder = BINDER.get();
		if (binder == null) {
			throw new IllegalStateException("Use Values.call() to prepare method to be captured");
		}
		BINDER.set(null);
		return binder.toBase();
	}
	
	@SuppressWarnings("unchecked")
	public static <S extends Sample> S call(Class<S> type) {
		if (BINDER.get() != null) {
			throw new IllegalStateException("Previous call wasn't consumed");
		}
		FieldBinder<S> binder = fieldBinder((Class<S>)type);
		BINDER.set((FieldBinder<Sample>)binder);
		return binder.bind();
	}
	
	@SuppressWarnings("unchecked")
	public static <S extends Sample> FieldBinder<S> fieldBinder(Class<S> type) {
		return (FieldBinder<S>) new Binder(type);
	}

	public interface FieldBinder<S extends Sample> extends Value {
		
		public S bind();

		public Value getField();
		
	}
	
	private static class Binder implements FieldBinder<Sample>, InvocationHandler {

		private static Map<Class<?>, Object> RETURN_VALUES = new HashMap<Class<?>, Object>();
		static {
			RETURN_VALUES.put(boolean.class, false);
			RETURN_VALUES.put(byte.class, (byte)0);
			RETURN_VALUES.put(short.class, (short)0);
			RETURN_VALUES.put(char.class, (char)0);
			RETURN_VALUES.put(int.class, (int)0);
			RETURN_VALUES.put(long.class, (long)0);
			RETURN_VALUES.put(float.class, (float)0);
			RETURN_VALUES.put(double.class, (double)0);
		}
		
		private final Class<? extends Sample> type;
		
		private Value bound;

		public Binder(Class<? extends Sample> type) {
			this.type = type;
		}

		@Override
		public Value getField() {
			if (bound == null) {
				throw new IllegalStateException("Sample method wasn't called yet");
			}
			return bound;
		}
		
		@Override
		public BaseValue toBase() {
			return getField().toBase();
		}

		@Override
		public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (Sample.class.isAssignableFrom(method.getDeclaringClass())) {
				if (bound == null) {
					bound = field(type, method.getName());
				}
				else {
					throw new IllegalStateException("Already bound");
				}
				return RETURN_VALUES.get(method.getReturnType());
			}
			else {
				return method.invoke(this, args);
			}
		}

		@Override
		public Sample bind() {
			return (Sample) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, this);
		}
	}
}
