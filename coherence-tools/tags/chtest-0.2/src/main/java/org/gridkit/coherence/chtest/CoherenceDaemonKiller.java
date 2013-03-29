package org.gridkit.coherence.chtest;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.gridkit.vicluster.isolate.Isolate;
import org.gridkit.vicluster.isolate.ThreadKiller;

import com.tangosol.util.Daemon;

/**
 * This is heuristic thread killer which tries to halt daemons, 
 * which may be resistant to interrupt and exceptions.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("serial")
class CoherenceDaemonKiller implements ThreadKiller, Runnable, Serializable {
	
	@Override
	public void run() {
		Isolate isolate = Isolate.currentIsolate();
		if (isolate != null) {
			isolate.addThreadKiller(this);
		}
	}

	@Override
	public boolean tryToKill(Isolate isolate, Thread t) {
		Object target = getField(t, "target");
		if (target == null) {
			return false;
		}
		if (target instanceof Daemon) {
			((Daemon)target).stop();
			return true;
		}
		return false;
	}
	
	public static Object getField(Object x, String field) {
		try {
			Field f = null;
			Class<?> c = x.getClass();
			while(f == null && c != Object.class) {
				try {
					f = c.getDeclaredField(field);
				} catch (NoSuchFieldException e) {
				}
				if (f == null) {
					c = c.getSuperclass();
				}
			}
			if (f != null) {
				f.setAccessible(true);
				return f.get(x);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		throw new IllegalArgumentException("Cannot get '" + field + "' from " + x.getClass().getName());
	}	
}
