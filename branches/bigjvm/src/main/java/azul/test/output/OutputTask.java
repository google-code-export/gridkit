package azul.test.output;

import java.io.Writer;
import java.util.List;

public class OutputTask implements Task {
	final Writer writer;
	
	final long[] data;
	final long[] timestamp;
    final int datasize;
	
	final boolean isLast;
	
	public OutputTask(Writer writer, long[] data, int datasize, long[] timestamp, int timestampsize, boolean isLast) {
		if(datasize != timestampsize)
			throw new IllegalArgumentException("data || data");
		
		if (writer == null)
			throw new IllegalArgumentException("writer");
		
		this.writer = writer;
		
		this.data = data;
		this.timestamp = timestamp;
        this.datasize = datasize;
		
		this.isLast = isLast;
	}

	@Override
	public boolean isPoison() {
		return false;
	}
}
