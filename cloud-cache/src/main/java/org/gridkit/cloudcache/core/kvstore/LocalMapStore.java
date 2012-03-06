package org.gridkit.cloudcache.core.kvstore;

import java.util.concurrent.ConcurrentHashMap;

import org.gridkit.cloudcache.core.data.Binary;
import org.gridkit.cloudcache.core.processing.Codec;
import org.gridkit.cloudcache.core.processing.Failure;
import org.gridkit.cloudcache.core.processing.Request;
import org.gridkit.cloudcache.core.processing.RequestProcessor;
import org.gridkit.cloudcache.core.processing.RequestTypes;

public class LocalMapStore implements RequestProcessor {

	private ConcurrentHashMap<Binary, Binary> content;

	private GetProcessor getProcessor = new GetProcessor();
	private PutProcessor putProcessor = new PutProcessor();
	
	public void process(Request request) {
		String type = (String) request.getRequestType();
		if (RequestTypes.GET.equals(type)) {
			getProcessor.process(request);
		}
		else if (RequestTypes.PUT.equals(type)) {
			putProcessor.process(request);
		}
		else {
			request.fail(Failure.unsupportedRequestType());
		}
	}
	
	
	private class GetProcessor implements RequestProcessor {
		public void process(Request request) {
			Codec codec = request.getBinaryCodec();
			Binary key = request.getKey(codec);
			Binary result = content.get(key);
			if (result == null) {
				request.fail(Fail)
			}
			
		}
	}
	
	private class PutProcessor implements RequestProcessor {
		public void process(Request request) {
			// TODO Auto-generated method stub
			
		}
	}

}
