import java.io.IOException;

import org.junit.Test;


public class ThrowTest {

	public static class AnyThrow {

	    public static void throwUncheked(Throwable e) {
	        AnyThrow.<RuntimeException>throwAny(e);
	    }
	   
	    @SuppressWarnings("unchecked")
	    private static <E extends Throwable> void throwAny(Throwable e) throws E {
	        throw (E)e;
	    }
	}

	@Test(expected=IOException.class)
	public void testThrow() {
		AnyThrow.throwUncheked(new IOException());
	}
	
}
