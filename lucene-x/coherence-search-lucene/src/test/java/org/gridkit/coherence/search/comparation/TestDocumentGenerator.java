package org.gridkit.coherence.search.comparation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

public class TestDocumentGenerator {

	Map<String, FieldDescription> fields = new LinkedHashMap<String, TestDocumentGenerator.FieldDescription>();
	int documentCount;
	
	public int getDocCount() {
		return documentCount;
	}
	
	public void setDocCount(int docs) {
		documentCount = docs;
	}
	
	public List<String> getFieldList() {
		return new ArrayList<String>(fields.keySet());
	}
	
	public void addField(String field, double selectivity) {
		fields.put(field, new FieldDescription(field, selectivity));
	}
	
	public double getSelectivity(String field) {
		return fields.get(field).selectivity;
	}
	
	public Map<String, String> getDoc(int id) {
		Random rnd = new Random(id);
		Map<String, String> map = new HashMap<String, String>();
		map.put("ID", String.valueOf(id));
		for(FieldDescription fd: fields.values()) {
			String fieldName = fd.fieldName;
			int range = (int)(documentCount / fd.selectivity);
			int sn = rnd.nextInt(range);
			map.put(fieldName, getTerm(fieldName, sn));
		}
		return map;
	}
	
	public Document getLuceneDoc(int id) {
		Document doc = new Document();
		for(Map.Entry<String, String> entry: getDoc(id).entrySet()) {
			Field field = new Field(entry.getKey(), entry.getValue(), Store.NO, Index.ANALYZED);
			doc.add(field);
		}
		doc.add(new Field("#DOC_KEY#", String.valueOf(id), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		return doc;
	}
	
	public String getRandomTerm(Random rnd, String field) {
		FieldDescription fd = fields.get(field);
		int range = (int)(documentCount / fd.selectivity);
		return getTerm(field, rnd.nextInt(range));
	}

	public String[] getRandomRange(Random rnd, String field, int len) {
		FieldDescription fd = fields.get(field);
		int range = (int)(documentCount / fd.selectivity);
		String[] r = new String[2];
		int low = rnd.nextInt(range - len);
		r[0] = getTerm(field, low);
		r[1] = getTerm(field, low + len);
		
		return r;
	}
	
	public String getTerm(String field, int sn) {
		Random rnd = new Random(field.hashCode() ^ sn);
		int len = rnd.nextInt(16) + 1;
		char[] buf = new char[len];
		for(int i = 0; i != len; ++i) {
			buf[i] = (char) ('A' + rnd.nextInt(26));
		}
		int n = 0x40000000 | sn;
		return Integer.toHexString(n) + "-" + new String(buf);
	}
	
	private static class FieldDescription {
		
		String fieldName;
		double selectivity;		
		
		public FieldDescription(String fieldName, double selectivity) {
			this.fieldName = fieldName;
			this.selectivity = selectivity;
		}
	}
}
