/*
 * Copyright 2006-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.osgi.samples.simplewebapp.servlet;

import com.tangosol.net.NamedCache;
import org.osgi.framework.BundleContext;
import ru.questora.coherence.osgi.Activator;
import ru.questora.research.coherence.osgi.support.api.two.CacheFactoryService;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloOsgiWorldServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addHelloWorld(resp, req.getMethod());
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addHelloWorld(resp, req.getMethod());
    }

    public String getServletInfo() {
        return "Simple Osgi World Servlet";
    }

    private void addHelloWorld(HttpServletResponse response, String method) throws IOException {
        response.setContentType("text/html");

        ServletOutputStream out = response.getOutputStream();
        out.println("<html>");
        final BundleContext bundleContext = Activator.getBundleContext();
        final Object cacheService = bundleContext.getService(bundleContext.getServiceReference(CacheFactoryService.class.getName()));

        final NamedCache testCache = ((CacheFactoryService) cacheService).getCache("TestCache");
        out.println("Cache: <br><pre>" + testCache + "</pre>");
        out.println("Cache Service: <br><pre>" + testCache.getCacheService() + "</pre>");

        final String key = "key";
        testCache.put(key, 838);
        out.println("testCache[" + key + "]: <br><pre>" + testCache.getCacheService() + "</pre>");
        out.println(String.format("testCache[%1s] = %2s<br>", key, testCache.get(key)));

//		out.println("<head><title>Hello Osgi World</title></head>");
//		out.println("<body>");
//		out.println("<h1>Hello OSGi World</h1>");
//		out.println("<h2>http method used:" + method + "</h2>");
        out.println("</body></html>");
    }
}
