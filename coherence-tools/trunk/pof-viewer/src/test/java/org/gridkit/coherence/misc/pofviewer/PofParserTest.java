package org.gridkit.coherence.misc.pofviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import junit.framework.Assert;

import org.gridkit.coherence.utils.pof.AutoPofSerializer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.io.ByteArrayWriteBuffer;
import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.EvolvablePortableObject;
import com.tangosol.io.pof.PofConstants;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.Binary;

public class PofParserTest {

	private static NamedCache AUTO_POF_CACHE;
	private static PofContext SERIALIZER;
	private static PofContext BASIC_POF = new ConfigurablePofContext();

	@BeforeClass
	public static void init_fake_type_cache() {
		String xmlText =
				"<cache-config>"
			+ 		"<caching-scheme-mapping>"
			+			"<cache-mapping><cache-name>AUTO_POF_MAPPING</cache-name><scheme-name>AUTO_POF_SCHEME</scheme-name></cache-mapping>"
			+		"</caching-scheme-mapping>"
			+ 		"<caching-schemes>"
			+			"<local-scheme>"
			+				"<scheme-name>AUTO_POF_SCHEME</scheme-name>"
			+			"</local-scheme>"
			+		"</caching-schemes>"
			+	"</cache-config>";

		XmlElement xml = XmlHelper.loadXml(xmlText);
		DefaultConfigurableCacheFactory cf = new DefaultConfigurableCacheFactory(xml);
		AUTO_POF_CACHE = cf.ensureCache("AUTO_POF_MAPPING", null);
	}

	@Before
	public void init_auto_serializer() {
		AUTO_POF_CACHE.clear();
		SERIALIZER = new AutoPofSerializer("pof-config.xml", AUTO_POF_CACHE);
//		SERIALIZER = new AutoPofSerializer("pof-config.xml");
	}

	public Binary serializeBasic(Object object) {
		try {
			WriteBuffer buf = new ByteArrayWriteBuffer(4096);		
			WriteBuffer.BufferOutput out = buf.getBufferOutput();
			BASIC_POF.serialize(out, object);

			return buf.toBinary();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
	public List<PofEntry> toAutoPof(Object obj) {
		Binary bin = serializeAuto(obj);
		List<PofEntry> entries = PofParser.parsePof(bin, BASIC_POF);
		return entries;
	}

	public List<PofEntry> toBasicPof(Object obj) {
		Binary bin = serializeBasic(obj);
		List<PofEntry> entries = PofParser.parsePof(bin, BASIC_POF);
		return entries;
	}
	
	public String finePrint(PofFinePrinter fp, List<PofEntry> entries) {
		Map<PofPath, String> aliases = fp.findAliases(entries);
		
		StringBuilder sb = new StringBuilder();
		for(PofEntry entry : entries) {
			String type = fp.getClassName(entry.getTypeId());
			if (type == null) {
				type = String.valueOf(entry.getTypeId());
			}
			String path = aliases.get(entry.getPath());
			if (path == null) {
				path = entry.getPath().toString();
			}
			String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
			sb.append(path).append(", ").append(type).append(", ").append(value).append('\n');
		}

		return sb.toString();
	}
	
	@Test
	public void test_exception() {

		Binary bin = serializeBasic(Arrays.asList(new Object[]{new Exception(new Exception("New exception"))}));
		List<PofEntry> entries = PofParser.parsePof(bin, BASIC_POF);
		Assert.assertTrue(entries.size() > 3);
		
		// should not crash
		Binary bin2 = serializeAuto(new Exception(new Exception("New exception")));
		List<PofEntry> entries2 = PofParser.parsePof(bin2, BASIC_POF);
		Assert.assertTrue(entries2.size() > 3);
	}

	@Test
	public void test_default_list() {
		
		List<String> strings = Arrays.asList("One", "Two", "Three");
		
		List<PofEntry> entries = toBasicPof(strings);

		List<PofEntry> expected = new ArrayList<PofEntry>();
		PofPath r = PofPath.root();
		expected.add(new PofEntry(r, PofConstants.T_COLLECTION, null));
		expected.add(new PofEntry(r.i(0), -15, "One"));
		expected.add(new PofEntry(r.i(1), -15, "Two"));
		expected.add(new PofEntry(r.i(2), -15, "Three"));
		
		assertEqual(expected, entries);
	}

	@Test
	public void test_default_null() {
		
		List<PofEntry> entries = toBasicPof(null);
		
		List<PofEntry> expected = new ArrayList<PofEntry>();
		PofPath r = PofPath.root();
		expected.add(new PofEntry(r, PofConstants.V_REFERENCE_NULL, null));
		
		assertEqual(expected, entries);
	}

	@Test
	public void test_default_map() {
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("One", 1);
		map.put("Two", 2);
		map.put("Three", 3);
		
		List<PofEntry> entries = toBasicPof(map);
		
		List<PofEntry> expected = new ArrayList<PofEntry>();
		PofPath r = PofPath.root();
		expected.add(new PofEntry(r, PofConstants.T_MAP, null));
		expected.add(new PofEntry(r.i(0), -100000, null));
		expected.add(new PofEntry(r.i(0).a(0), -15, "Three"));
		expected.add(new PofEntry(r.i(0).a(1), -2, 3));
		expected.add(new PofEntry(r.i(1), -100000, null));
		expected.add(new PofEntry(r.i(1).a(0), -15, "One"));
		expected.add(new PofEntry(r.i(1).a(1), -2, 1));
		expected.add(new PofEntry(r.i(2), -100000, null));
		expected.add(new PofEntry(r.i(2).a(0), -15, "Two"));
		expected.add(new PofEntry(r.i(2).a(1), -2, 2));
		
		assertEqual(expected, entries);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_default_list_of_maps() {
		
		Map<String, Integer> map1 = new HashMap<String, Integer>();
		map1.put("One", 1);
		map1.put("Two", 2);

		Map<String, Integer> map2 = new HashMap<String, Integer>();
		map2.put("Three", 3);
		map2.put("Four", 4);
		
		Object list = Arrays.asList(map1, map2);
		
		List<PofEntry> entries = toBasicPof(list);
		
		List<PofEntry> expected = new ArrayList<PofEntry>();
		PofPath r = PofPath.root();
		expected.add(new PofEntry(r, PofConstants.T_COLLECTION, null));
		expected.add(new PofEntry(r.i(0), PofConstants.T_MAP, null));
		expected.add(new PofEntry(r.i(0).i(0), -100000, null));
		expected.add(new PofEntry(r.i(0).i(0).a(0), -15, "One"));
		expected.add(new PofEntry(r.i(0).i(0).a(1), -2, 1));
		expected.add(new PofEntry(r.i(0).i(1), -100000, null));
		expected.add(new PofEntry(r.i(0).i(1).a(0), -15, "Two"));
		expected.add(new PofEntry(r.i(0).i(1).a(1), -2, 2));
		expected.add(new PofEntry(r.i(1), PofConstants.T_MAP, null));
		expected.add(new PofEntry(r.i(1).i(0), -100000, null));
		expected.add(new PofEntry(r.i(1).i(0).a(0), -15, "Three"));
		expected.add(new PofEntry(r.i(1).i(0).a(1), -2, 3));
		expected.add(new PofEntry(r.i(1).i(1), -100000, null));
		expected.add(new PofEntry(r.i(1).i(1).a(0), -15, "Four"));
		expected.add(new PofEntry(r.i(1).i(1).a(1), -2, 4));
		
		assertEqual(expected, entries);
	}
	
	@Test
	public void test_complex_object_format() {
		
		MyBean bean = new MyBean(true);
		List<PofEntry> entries = toAutoPof(bean);
		
		List<PofEntry> expected = new ArrayList<PofEntry>();
		
		PofPath p = PofPath.root();
		expected.add(new PofEntry(p, 10001, null));
		PofPath a0 = p.a(0);
		expected.add(new PofEntry(a0, -25, null));
		expected.add(new PofEntry(a0.i(0), -11, Boolean.FALSE));
		expected.add(new PofEntry(a0.i(1), -11, Boolean.FALSE));
		expected.add(new PofEntry(a0.i(2), -11, Boolean.FALSE));
		PofPath a1 = p.a(1);
		expected.add(new PofEntry(a1, -15, "test-bean"));
		PofPath a2 = p.a(2);
		expected.add(new PofEntry(a2, -24, null));
		PofPath a2a;
		a2a = a2.i(0);
		expected.add(new PofEntry(a2a, 10002, null));
		expected.add(new PofEntry(a2a.a(0), -25, null));
		expected.add(new PofEntry(a2a.a(0).i(0), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(0).i(1), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(0).i(2), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(0).i(3), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(1), -15, "attr-1"));
		expected.add(new PofEntry(a2a.a(2), -43, 1));
		expected.add(new PofEntry(a2a.a(3), -43, 1));
		expected.add(new PofEntry(a2a.a(4), -11, Boolean.TRUE));
		a2a = a2.i(1);
		expected.add(new PofEntry(a2a, 10002, null));
		expected.add(new PofEntry(a2a.a(0), -25, null));
		expected.add(new PofEntry(a2a.a(0).i(0), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(0).i(1), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(0).i(2), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(0).i(3), -11, Boolean.FALSE));
		expected.add(new PofEntry(a2a.a(1), -15, "attr-2"));
		expected.add(new PofEntry(a2a.a(2), -44, 2));
		expected.add(new PofEntry(a2a.a(3), -44, 2));
		expected.add(new PofEntry(a2a.a(4), -11, Boolean.FALSE));
		PofPath a3 = p.a(3);
		expected.add(new PofEntry(a3, 10003, null));
		expected.add(new PofEntry(a3.a(0), 10003, null));  // nested POF
		expected.add(new PofEntry(a3.a(0).a(0), -25, null));
		expected.add(new PofEntry(a3.a(0).a(0).i(0), -11, Boolean.TRUE));
		expected.add(new PofEntry(a3.a(1), -28, null));
		expected.add(new PofEntry(a3.a(1).i(0), PofParser.T_PSEUDO_MAP_ENTRY, null));
		expected.add(new PofEntry(a3.a(1).i(0).a(0), -15, 1));
		expected.add(new PofEntry(a3.a(1).i(0).a(1), -25, null));
		expected.add(new PofEntry(a3.a(1).i(0).a(1).i(0), -12, 49));
		expected.add(new PofEntry(a3.a(1).i(0).a(1).i(1), -12, 49));
		expected.add(new PofEntry(a3.a(1).i(0).a(1).i(2), -12, 49));
		expected.add(new PofEntry(a3.a(1).i(1), PofParser.T_PSEUDO_MAP_ENTRY, null));
		expected.add(new PofEntry(a3.a(1).i(1).a(0), -15, "Y"));
		expected.add(new PofEntry(a3.a(1).i(1).a(1), -25, null));
		expected.add(new PofEntry(a3.a(1).i(1).a(1).i(0), -12, 89));
		expected.add(new PofEntry(a3.a(1).i(2), PofParser.T_PSEUDO_MAP_ENTRY, null));
		expected.add(new PofEntry(a3.a(1).i(2).a(0), -15, "x"));
		expected.add(new PofEntry(a3.a(1).i(2).a(1), -25, null));
		expected.add(new PofEntry(a3.a(1).i(2).a(1).i(0), -12, 120));		
		
		assertEqual(expected, entries);
	}
	
	@Test
	public void test_fine_print_complex_object() {
		
		PofFinePrinter fp = new PofFinePrinter();
		MyBean bean = new MyBean(true);
		List<PofEntry> entries = toAutoPof(bean);
		String out = finePrint(fp, entries);
		
		String expected = readText("test_fine_print_complex_object.pof-txt");
		
		Assert.assertEquals(expected, out);
		
	}

	@Test
	public void test_fine_print_config_complex_object() throws IOException {
		
		List<MyPofBean> beans = Arrays.asList(new MyPofBean("12"), new MyPofBean("24"));
		PofFinePrinter fp = new PofFinePrinter();
		Properties config = new Properties();
		config.load(getClass().getResourceAsStream("my-pof-bean.config.props"));
		fp.loadConfiguration(config);
		
		List<PofEntry> entries = toAutoPof(beans);
		String out = finePrint(fp, entries);
		
		String expected = readText("test_fine_print_config_complex_object.pof-txt");
		
		Assert.assertEquals(expected, out);
		
	}

	@Test
	public void test_fine_print_config_complex_object2() throws IOException {
		
		List<MyEvolvableBean> beans = Arrays.asList(new MyEvolvableBean("12"), new MyEvolvableBean("24"));
		PofFinePrinter fp = new PofFinePrinter();
		Properties config = new Properties();
		config.load(getClass().getResourceAsStream("my-pof-bean.config.props"));
		fp.loadConfiguration(config);
		
		List<PofEntry> entries = toAutoPof(beans);
		String out = finePrint(fp, entries);
		
		String expected = readText("test_fine_print_config_complex_object2.pof-txt");
		
		Assert.assertEquals(expected, out);
		
	}
	
	private String readText(String string) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(string)));
			StringBuilder sb = new StringBuilder();
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;					
				}
				else {
					sb.append(line).append('\n');
				}
			}
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void assertEqual(List<PofEntry> expected, List<PofEntry> actual) {
		StringBuilder et = new StringBuilder();
		for(PofEntry e: expected) {
			et.append(e.toString()).append('\n');
		}
		
		StringBuilder at = new StringBuilder();
		for(PofEntry a: actual) {
			at.append(a.toString()).append('\n');
		}
		
		Assert.assertEquals(et.toString(), at.toString());
	}
	
	
	public static class MyBean {
		
		String name;
		MyAttr[] attrs;
		Map<String, byte[]> data;
		
		public MyBean() {			
		}
		
		public MyBean(boolean test) {
			name = "test-bean";
			attrs = new MyAttr[]{new MyAttr(1), new MyAttr(2)};
			data = new TreeMap<String, byte[]>();
			data.put("x", "x".getBytes());
			data.put("Y", "Y".getBytes());
			data.put("1", "111".getBytes());
		}
	}
	
	public static class MyAttr {
		
		String name;
		int intValue;
		char charValue;
		boolean booleanValue;

		public MyAttr() {
		}

		public MyAttr(int i) {
			name = "attr-" + i;
			intValue = i;
			charValue = (char) i;
			booleanValue = i % 2 == 1;
		}
	}
	
	public static class MyPofBean implements PortableObject {
		
		String id;
		int quantity;
		int price;
		Map<String, MyPofAttr> attributes;
		
		public MyPofBean() {
		}

		public MyPofBean(String id) {
			this.id = id;
			this.quantity = (Math.abs(id.hashCode()) % 10) * 10000;
			this.price = (Math.abs(id.hashCode()) % 100) * 10;
			this.attributes = new HashMap<String, PofParserTest.MyPofAttr>();
			attributes.put("a", new MyPofAttr("A-" + id, "x", "y"));
			attributes.put("b", new MyPofAttr("B-" + id));
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void readExternal(PofReader in) throws IOException {
			id = in.readString(1);
			quantity = in.readInt(2);
			price = in.readInt(3);
			attributes = in.readMap(4, new HashMap());
		}

		@Override
		public void writeExternal(PofWriter out) throws IOException {
			out.writeString(1, id);
			out.writeInt(2, quantity);
			out.writeInt(3, price);
			out.writeMap(4, attributes);
		}
	}

	public static class MyEvolvableBean implements EvolvablePortableObject {
		
		String id;
		int quantity;
		int price;
		Map<String, MyPofAttr> attributes;
		
		public MyEvolvableBean() {
		}
		
		public MyEvolvableBean(String id) {
			this.id = id;
			this.quantity = (Math.abs(id.hashCode()) % 10) * 10000;
			this.price = (Math.abs(id.hashCode()) % 100) * 10;
			this.attributes = new HashMap<String, PofParserTest.MyPofAttr>();
			attributes.put("a", new MyPofAttr("A-" + id, "x", "y"));
			attributes.put("b", new MyPofAttr("B-" + id));
		}
		
		@Override
		public int getImplVersion() {
			return 1;
		}

		@Override
		public int getDataVersion() {
			return 1;
		}

		@Override
		public void setDataVersion(int n) {
		}

		@Override
		public Binary getFutureData() {
			return null;
		}

		@Override
		public void setFutureData(Binary reminder) {
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void readExternal(PofReader in) throws IOException {
			id = in.readString(1);
			quantity = in.readInt(2);
			price = in.readInt(3);
			attributes = in.readMap(4, new HashMap());
		}
		
		@Override
		public void writeExternal(PofWriter out) throws IOException {
			out.writeString(1, id);
			out.writeInt(2, quantity);
			out.writeInt(3, price);
			out.writeMap(4, attributes, String.class);
		}
	}
	
	public static class MyPofAttr implements PortableObject {
		
		String attrName;
		String attrType;
		String[] attrValues;
		
		public MyPofAttr() {
		}
		
		public MyPofAttr(String n, String... vals) {
			this.attrName = n;
			this.attrType = "text";
			this.attrValues = vals;
		}

		@Override
		public void readExternal(PofReader in) throws IOException {
			attrName = in.readString(1);
			attrType = in.readString(2);
			attrValues = (String[]) in.readObjectArray(3, new String[0]);
		}

		@Override
		public void writeExternal(PofWriter out) throws IOException {
			out.writeString(1, attrName); 
			out.writeString(2, attrType);
			out.writeObjectArray(3, attrValues);			
		}
	}
}
