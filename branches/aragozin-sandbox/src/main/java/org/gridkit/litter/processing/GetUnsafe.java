package org.gridkit.litter.processing;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import sun.misc.Unsafe;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class GetUnsafe {

	
	public static void main(String[] args) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field f = AtomicReference.class.getDeclaredField("unsafe");
		f.setAccessible(true);
		Unsafe unsafe = (Unsafe) f.get(null);
		System.out.println("Base offset: byte[] " + unsafe.arrayBaseOffset(byte[].class));
		System.out.println("Index scale: byte[] " + unsafe.arrayIndexScale(byte[].class));
		System.out.println("Base offset: int[] " + unsafe.arrayBaseOffset(int[].class));
		System.out.println("Index scale: int[] " + unsafe.arrayIndexScale(int[].class));
		System.out.println("Base offset: long[] " + unsafe.arrayBaseOffset(long[].class));
		System.out.println("Index scale: long[] " + unsafe.arrayIndexScale(long[].class));
		
		byte[] bytes = new byte[32];
		
		unsafe.compareAndSwapLong(bytes, 16, 0, 16);
		
		System.out.println(Arrays.toString(bytes));

		unsafe.compareAndSwapLong(bytes, 16, 0, 17);
		unsafe.compareAndSwapLong(bytes, 16, 16, -1);

		System.out.println(Arrays.toString(bytes));
		
		unsafe.getLong(bytes, 16l);
		unsafe.getLong(bytes, 20l);
		unsafe.getLong(bytes, 22l);
		unsafe.getLong(bytes, 23l);
		
		unsafe.putLongVolatile(bytes, 20, -1);
		System.out.println(Arrays.toString(bytes));
		unsafe.putLongVolatile(bytes, 21, -1);
		System.out.println(Arrays.toString(bytes));
		unsafe.compareAndSwapLong(bytes, 19, -1, 0);
		System.out.println(Arrays.toString(bytes));
	}
}
