package org.gridkit.coherence.misc.pofviewer;

import java.io.IOException;
import java.util.List;

import org.gridkit.coherence.utils.pof.AutoPofSerializer;
import org.junit.Test;

import com.tangosol.io.ByteArrayWriteBuffer;
import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofContext;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryWriteBuffer;
import com.tangosol.util.ExternalizableHelper;

public class PofParserTest {
	
	private static PofContext SERIALIZER = new AutoPofSerializer();
	private static PofContext BASIC_POF = new ConfigurablePofContext();

	public Binary serializeBasic(Object object) {
		try {
			WriteBuffer buf = new ByteArrayWriteBuffer(4096);		
			WriteBuffer.BufferOutput out = buf.getBufferOutput();
			BASIC_POF.serialize(out, object);

			return buf.toBinary();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		return ExternalizableHelper.toBinary(object, BASIC_POF);
	}

	public Binary serializeAuto(Object object) {
		try {
			WriteBuffer buf = new ByteArrayWriteBuffer(4096);		
			WriteBuffer.BufferOutput out = buf.getBufferOutput();
			SERIALIZER.serialize(out, object);

			return buf.toBinary();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void test_basics() {

		Binary bin = serializeBasic(new Exception(new Exception("New exception")));
		Object x = ExternalizableHelper.fromBinary(bin, BASIC_POF);
		List<PofEntry> entries = PofParser.parsePof(bin, BASIC_POF);
		
		// should not crash
		Binary bin2 = serializeAuto(new Exception(new Exception("New exception")));
		List<PofEntry> entries2 = PofParser.parsePof(bin2, BASIC_POF);
	}
	
}
