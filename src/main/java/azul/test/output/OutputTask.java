package azul.test.output;

import java.io.Writer;
import java.util.List;

public class OutputTask {
	final Writer writer;
	
	final List<Long> data;
	final List<Long> timestamp;
	
	final boolean isLast;
	
	public OutputTask(Writer writer, List<Long> data, List<Long> timestamp, boolean isLast) {
		if(data.size() != timestamp.size())
			throw new IllegalArgumentException("data || data");
		
		if (writer == null)
			throw new IllegalArgumentException("writer");
		
		this.writer = writer;
		
		this.data = data;
		this.timestamp = timestamp;
		
		this.isLast = isLast;
	}
}
