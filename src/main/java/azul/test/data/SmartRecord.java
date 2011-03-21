package azul.test.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;
import java.util.Random;

public class SmartRecord extends Record implements Externalizable {
	private static final long serialVersionUID = 2752720712140523020L;

	public SmartRecord() {
		
	}
	
	public SmartRecord(Random rand, int size, int dispersion) {
		super(rand, size, dispersion);
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		
		out.writeUTF(first);
		out.writeUTF(second);
		
		out.writeInt(bytes.length);
		out.write(bytes);
		
		out.writeInt(ints.size());
		
		for (Integer i : ints)
			out.writeInt(i);	
		
		out.close();
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readLong();
		first = in.readUTF();
		second = in.readUTF();
		
		bytes = new byte[in.readInt()];
		in.readFully(bytes);
		
		ints = new LinkedList<Integer>();
		int size = in.readInt();
		
		for (int i = 0; i < size; ++i)
			ints.add(in.readInt());
		
		in.close();
	}
}
