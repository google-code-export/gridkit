package org.gridkit.util.vicontrol.marshal;

import java.io.Serializable;

public interface Marshaled extends Serializable {
	
	public Object unmarshal();
	
}
