package org.gridkit.coherence.misc.pofviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.ReadBuffer.BufferInput;
import com.tangosol.io.pof.PofConstants;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofHelper;
import com.tangosol.io.pof.reflect.AbstractPofValue;
import com.tangosol.io.pof.reflect.ComplexPofValue;
import com.tangosol.io.pof.reflect.PofArray;
import com.tangosol.io.pof.reflect.PofUserType;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofValue;
import com.tangosol.util.Binary;
import com.tangosol.util.WrapperException;

public class PofParser {

	public static int T_PSEUDO_MAP_ENTRY = -100000;
	
	public static List<PofEntry> parsePof(Binary bin, PofContext context) {
		try {
			PofValue valueRoot = PofValueParser.parse(bin, context);
			List<PofEntry> result = new ArrayList<PofEntry>();
			parsePof(valueRoot, PofPath.root(), result);
			return result;
		} catch (IOException e) {
			throw new WrapperException(e);
		}
	}

	private static void parsePof(PofValue value, PofPath path, List<PofEntry> result) throws IOException {
		if (value instanceof SimplePofValue) {
			int typeId = value.getTypeId();
			switch(typeId) {
			case PofConstants.T_MAP:
				parseGenericMap((SimplePofValue) value, path, result);
				break;
			case PofConstants.T_UNIFORM_KEYS_MAP:
				parseUniformKeysMap((SimplePofValue) value, path, result);
				break;
			case PofConstants.T_UNIFORM_MAP:
				parseUniformMap((SimplePofValue) value, path, result);
				break;
			default:
				Object x = ((SimplePofValue)value).getValue();
				PofEntry entry = new PofEntry(path, typeId, x);
				result.add(entry);
			}
		}
		else if (value instanceof ComplexPofValue) {
			ComplexPofValue complex = (ComplexPofValue) value;
			int typeId = complex.getTypeId();
			PofEntry entry = new PofEntry(path, typeId, null);
			result.add(entry);
			
			boolean a = complex instanceof PofUserType;
			
			// iterate through children
			int offs = complex.getOffset();
			int len = length(complex);
			int size = complex.getSize();
			int parsedSize = 0;
			for(int i = 0; i != len; ++i) {
				if (parsedSize == size) {
					break;
				}
				AbstractPofValue val;
				try {
					val = (AbstractPofValue) complex.getChild(i);
				}
				catch(IndexOutOfBoundsException e) {
					break;
				}
				if (val == null) {
					continue;
				}
				if ((val.getOffset() - offs) > size) {
					break;
				}
				if (isArrayFiller(val)) {
					// hack to skip terminator prop
					if (offs + size - val.getOffset() <= 1) {
						break;
					}
					continue;
				}
				PofPath attrPath = a ? path.a(i) : path.i(i);
				parsePof(val, attrPath, result);
				parsedSize += val.getSize();
			}
		}
		else {
			int typeId = value.getTypeId();
			PofEntry entry = new PofEntry(path, typeId, "Unknown PofValue: " + value.getClass().getSimpleName());
			result.add(entry);
		}		
	}

	private static AbstractPofValue parseValue(BufferInput bi, int type, PofContext context) throws IOException {
		int offs = bi.getOffset();
		if (type == PofConstants.T_UNKNOWN) {
			PofHelper.skipValue(bi);
		}
		else {
			PofHelper.skipUniformValue(bi, type);
		}
		int cb = bi.getOffset() - offs;
		ReadBuffer chunk = bi.getBuffer().getReadBuffer(offs, cb);
		if (type == PofConstants.T_UNKNOWN) {
			return (AbstractPofValue) PofValueParser.parse(chunk, context);
		}
		else {
			return (AbstractPofValue) PofParserHelper.parseUniformValue(chunk, type, context);
		}
	}
	
	private static void parseGenericMap(SimplePofValue map, PofPath path, List<PofEntry> result) throws IOException {
		result.add(new PofEntry(path, map.getTypeId(), null));
		ReadBuffer rb = map.getSerializedValue();
		BufferInput ib = rb.getBufferInput();
		ib.readPackedInt(); // typeId
		ib.readPackedInt(); // length
		int n = 0;
		while(true) {
			if (ib.getOffset() >= rb.length()) {
				break;
			}
			AbstractPofValue key = parseValue(ib, PofConstants.T_UNKNOWN, map.getPofContext());
			result.add(new PofEntry(path.i(n), T_PSEUDO_MAP_ENTRY, null));
			parsePof(key, path.i(n).a(0), result);
			AbstractPofValue value = parseValue(ib, PofConstants.T_UNKNOWN, map.getPofContext());
			parsePof(value, path.i(n).a(1), result);
			++n;
		}
	}

	private static void parseUniformKeysMap(SimplePofValue map, PofPath path, List<PofEntry> result) throws IOException {
		result.add(new PofEntry(path, map.getTypeId(), null));
		ReadBuffer rb = map.getSerializedValue();
		BufferInput ib = rb.getBufferInput();
		ib.readPackedInt(); // typeId
		int keyType = ib.readPackedInt(); // key type
		ib.readPackedInt(); // length
		int n = 0;
		while(true) {
			if (ib.getOffset() >= rb.length()) {
				break;
			}
			AbstractPofValue key = parseValue(ib, keyType, map.getPofContext());
			result.add(new PofEntry(path.i(n), T_PSEUDO_MAP_ENTRY, null));
			parsePof(key, path.i(n).a(0), result);
			AbstractPofValue value = parseValue(ib, PofConstants.T_UNKNOWN, map.getPofContext());
			parsePof(value, path.i(n).a(1), result);
			++n;
		}
	}	

	private static void parseUniformMap(SimplePofValue map, PofPath path, List<PofEntry> result) throws IOException {
		result.add(new PofEntry(path, map.getTypeId(), null));
		ReadBuffer rb = map.getSerializedValue();
		BufferInput ib = rb.getBufferInput();
		ib.readPackedInt(); // typeId
		int keyType = ib.readPackedInt(); // key type
		int valType = ib.readPackedInt(); // key type
		ib.readPackedInt(); // length
		int n = 0;
		while(true) {
			if (ib.getOffset() >= rb.length()) {
				break;
			}
			AbstractPofValue key = parseValue(ib, keyType, map.getPofContext());
			result.add(new PofEntry(path.i(n), T_PSEUDO_MAP_ENTRY, null));
			parsePof(key, path.i(n).a(0), result);
			AbstractPofValue value = parseValue(ib, valType, map.getPofContext());
			parsePof(value, path.i(n).a(1), result);
			++n;
		}
	}

	private static boolean isArrayFiller(AbstractPofValue val) {
		return val instanceof SimplePofValue && val.getClass() != SimplePofValue.class;
	}

	private static int length(ComplexPofValue complex) {
		if (complex instanceof PofArray) {
			return ((PofArray)complex).getLength();
		}
		else {
			return Integer.MAX_VALUE;
		}
	}
	
	private static class PofParserHelper extends PofValueParser {
		
		public static PofValue parseUniformValue(ReadBuffer rb, int type, PofContext ctx) {
			return PofValueParser.parseUniformValue(null, type, rb, ctx, 0);
		}
		
	}
}
