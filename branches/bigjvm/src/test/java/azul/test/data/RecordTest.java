package azul.test.data;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class RecordTest {
	@Test
	public void test() {
		Random rand = new Random(0);
		
		Record r = new Record(rand, 10240, 1);
		
		int size = r.getBytes().length + r.getInts().size() * 4
			+ r.getFirst().length() * 2 + r.getSecond().length() * 2 + 4;
		
		Assert.assertEquals(10240, size);
	}
}
