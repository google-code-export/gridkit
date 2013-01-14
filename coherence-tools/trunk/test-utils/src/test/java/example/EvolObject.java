package example;

import java.io.IOException;

import com.tangosol.io.AbstractEvolvable;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

public class EvolObject extends AbstractEvolvable implements PortableObject {

	private String text;

	public EvolObject() {
	}
	
	public EvolObject(String text) {
		this.text = text;
	}

	@Override
	public int getImplVersion() {
		return 2;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		this.text = in.readString(1);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeString(1, text);
	}
	
	public static void main(String[] string) {
		ConfigurablePofContext ctx = new ConfigurablePofContext("/evol-object-pof-config.xml");
		EvolObject x = new EvolObject("test");
		Binary bin = ExternalizableHelper.toBinary(x, ctx);
		System.out.println(bin.toString());
	}
}
