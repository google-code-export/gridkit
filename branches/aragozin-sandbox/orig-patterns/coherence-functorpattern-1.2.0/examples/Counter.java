import java.io.Serializable;

import com.oracle.coherence.patterns.command.Context;

public class Counter implements Context, Serializable {

	private static final long serialVersionUID = 3245746282404451450L;

	private long next;
	
	public Counter(long initialValue) {
		this.next = initialValue;
	}
	
	public long next() {
		return next++;
	}
	
	public String toString() {
		return String.format("Counter{next=%d}", next);
	}
}
