package org.gridkit.cloudcache.core.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gridkit.cloudcache.core.processing.Request;

public interface HttpEntryPoint {

	public Request initRequest(HttpServletRequest request, HttpServletResponse response);
}
