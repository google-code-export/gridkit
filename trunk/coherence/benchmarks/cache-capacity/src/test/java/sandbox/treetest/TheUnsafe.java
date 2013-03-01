package sandbox.treetest;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class TheUnsafe {

    public static Unsafe UNSAFE;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
