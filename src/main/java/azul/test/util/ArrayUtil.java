package azul.test.util;

import java.util.Random;

public class ArrayUtil {
	public static byte[] createRandomArray(Random rand, int arraySize, int dispersion) {
        byte[] ret;
        
        if(rand.nextBoolean())
            ret = new byte[arraySize + rand.nextInt(dispersion)];
        else
            ret = new byte[arraySize - rand.nextInt(dispersion)];
        
        return ret;
	}
}
