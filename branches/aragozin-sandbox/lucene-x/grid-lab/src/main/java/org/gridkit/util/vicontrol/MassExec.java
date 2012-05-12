package org.gridkit.util.vicontrol;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Helper class, hosting and number of methods for handling futures etc.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class MassExec {

	public static <T> List<? super T> waitAll(List<Future<T>> futures) {
		try {
			Object[] results = new Object[futures.size()];
			int n = 0;
			Exception e = null;
			for(Future<T> f : futures) {
				try {
					try {
						results[n] = f.get();
					}
					catch(ExecutionException ee) {
						// unwrapping ExecutionException
						if (ee.getCause() instanceof Exception) {
							throw (Exception)ee.getCause();
						}
						else {
							throw ee;
						}
					}
				}
				catch(Exception ee) {
					if (e == null) {
						e = ee; // only first exception will be thrown
					}
				}
				++n;
			}
			if (e != null) {
				throw e;
			}
			return Arrays.asList(results);
		} catch (Exception e) {
			AnyThrow.throwUncheked(e);
			return null;
		}
	}

	public static List<Object> collectAll(List<Future<?>> futures) {
		Object[] results = new Object[futures.size()];
		int n = 0;
		for(Future<?> f : futures) {
			try {
				try {
					results[n] = f.get();
				}
				catch(ExecutionException e) {
					// unwrapping ExecutionException
					if (e.getCause() instanceof Exception) {
						throw (Exception)e.getCause();
					}
					else {
						throw e;
					}
				}
			}
			catch(Exception e) {
				results[n] = e;
			}
			++n;
		}
		return Arrays.asList(results);
	}
	
	private static class AnyThrow {

	    public static void throwUncheked(Throwable e) {
	        AnyThrow.<RuntimeException>throwAny(e);
	    }
	   
	    @SuppressWarnings("unchecked")
	    private static <E extends Throwable> void throwAny(Throwable e) throws E {
	        throw (E)e;
	    }
	}
}
