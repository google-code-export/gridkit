import java.io.Serializable;

import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.functor.Functor;


@SuppressWarnings("serial")
public class NextValueFunctor implements Functor<Counter, Long>, Serializable {

	public Long execute(ExecutionEnvironment<Counter> executionEnvironment) {

		Counter counter = executionEnvironment.getContext();
		
		long next = counter.next();
		
		executionEnvironment.setContext(counter);
		
		return next;
	}
}
