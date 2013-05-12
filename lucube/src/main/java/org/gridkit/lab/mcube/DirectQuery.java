package org.gridkit.lab.mcube;

import java.util.Iterator;
import java.util.List;

public interface DirectQuery {

	public Iterator<Row> query(Cube cube, Value... values);

	public Iterator<Row> query(Cube cube, List<Value> values, List<Value> sortOrder);
	
}
