package org.gridkit.coherence.search.comparation;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;

import com.gemstone.gemfire.admin.RuntimeAdminException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.BetweenFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.InFilter;

public class MongoDBIndexTestSequence extends BaseTestSequence {

	private DB db;
	
	@Override
	protected void initCache() {
		try {
			DBAddress addr = new DBAddress("127.0.0.1", "test");
			db = Mongo.connect(addr);
			if (db.collectionExists("objects")) {
				db.getCollection("objects").drop();
			}
			db.createCollection("objects", new BasicDBObject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createIndexes(NamedCache cache, List<String> fields) {
		for (String field: fields) {
			db.getCollection("objects").ensureIndex(new BasicDBObject(field, 1));
		}
	}
	
	public Filter createFilter(QueryCondition[] conditions) {
		DBObject query;
		if (conditions.length == 1) {
			com.mongodb.QueryBuilder builder = new com.mongodb.QueryBuilder().start();
			createFilter(builder,  conditions[0]);
			query = builder.get();
		}
		else {
			com.mongodb.QueryBuilder builder = new com.mongodb.QueryBuilder().start();
			for(int i = 0; i != conditions.length; ++i) {
				createFilter(builder, conditions[i]);
			}
			query = builder.get();
		}
		return new FakeFilter(query);
	}

	private void createFilter(com.mongodb.QueryBuilder builder, QueryCondition qc) {
		
		if (qc.rangeQuery) {
			builder.put(qc.field).greaterThanEquals(qc.terms[0]).and(qc.field).lessThan(qc.terms[1]);
		}
		else if (qc.terms.length == 1) {
			builder.put(qc.field).is(qc.terms[0]);
		}
		else {
			builder.put(qc.field).in(qc.terms);
		}		
	}
	
	@Override
	protected void putToCache(Map<Integer, Object> batch) {
		List<DBObject> jbatch = new ArrayList<DBObject>(batch.size());
		for(Object obj: batch.values()) {
			Map map = (Map) obj;
			jbatch.add(new BasicDBObject(map));
		}
		db.getCollection("objects").insert(jbatch);
	}

	@Override
	protected int evaluateFilter(NamedCache cache, Filter filter) {
		DBObject query = ((FakeFilter)filter).query;
		DBCursor cursor = db.getCollection("objects").find(query);
		if (System.getProperty("no-fetch-data") !=null) {
			int n = 0;
			while(cursor.hasNext()) {
				cursor.next();
				++n;
			}
			return n;
		}
		else {
			return cursor.count();
		}
	}

	public static class FakeFilter implements Filter {

		DBObject query;
		
		public FakeFilter(DBObject query) {
			super();
			this.query = query;
		}

		@Override
		public boolean evaluate(Object arg0) {
			return false;
		}		
	}
	
	public static void main(String[] args) {
		new MongoDBIndexTestSequence().start();
	}
	
}
