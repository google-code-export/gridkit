package org.gridkit.coherence.misc.pofviewer;

import java.util.ArrayList;
import java.util.List;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.nio.ByteBufferReadBuffer;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.ComplexPofValue;
import com.tangosol.io.pof.reflect.PofUserType;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofValue;
import com.tangosol.util.Binary;
import com.tangosol.util.LongArray;

public class PofParser {

	public static List<PofEntry> parsePof(Binary bin, PofContext context) {
		PofValue valueRoot = PofValueParser.parse(bin, context);
		List<PofEntry> result = new ArrayList<PofEntry>();
		parsePof(valueRoot, PofPath.root(), result);
		return result;
	}

	private static void parsePof(PofValue value, PofPath path, List<PofEntry> result) {
		if (value instanceof SimplePofValue) {
			int typeId = value.getTypeId();
			Object x = ((SimplePofValue)value).getValue();
			PofEntry entry = new PofEntry(path, typeId, x);
			result.add(entry);
		}
		else if (value instanceof PofUserType){
			PofUserType ut = (PofUserType) value;
			int typeId = ut.getTypeId();
			LongArray.Iterator it = (com.tangosol.util.LongArray.Iterator) ut.getChildrenIterator();
			PofEntry entry = new PofEntry(path, typeId, null);
			result.add(entry);
			while(it.hasNext()) {
				PofValue val = (PofValue) it.next();
				int id = (int) it.getIndex();
				PofPath attrPath = path.a(id);
				parsePof(val, attrPath, result);
			}
		}
		else if (value instanceof ComplexPofValue) {
			PofUserType ut = (PofUserType) value;
			int typeId = ut.getTypeId();
			LongArray.Iterator it = (com.tangosol.util.LongArray.Iterator) ut.getChildrenIterator();
			PofEntry entry = new PofEntry(path, typeId, null);
			result.add(entry);
			while(it.hasNext()) {
				PofValue val = (PofValue) it.next();
				int id = (int) it.getIndex();
				PofPath attrPath = path.i(id);
				parsePof(val, attrPath, result);
			}
		}
		else {
			int typeId = value.getTypeId();
			PofEntry entry = new PofEntry(path, typeId, "Unknown PofValue: " + value.getClass().getSimpleName());
			result.add(entry);
		}		
	}
}
