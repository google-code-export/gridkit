package azul.test.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

public class SmartRecord extends Record implements Externalizable {
	private static final long serialVersionUID = 2752720712140523020L;

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeUTF(first);
		out.writeUTF(second);
		out.writeObject(bytes);
		out.writeObject(ints);
		out.flush();
		out.close();
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readLong();
		first = in.readUTF();
		second = in.readUTF();
		bytes = (byte[]) in.readObject();
		ints = (List<Integer>) in.readObject();
		in.close();
	}
}
