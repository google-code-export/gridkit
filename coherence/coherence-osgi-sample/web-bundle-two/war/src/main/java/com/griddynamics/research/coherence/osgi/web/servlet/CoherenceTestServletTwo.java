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

package com.griddynamics.research.coherence.osgi.web.servlet;

import com.tangosol.net.NamedCache;
import com.griddynamics.research.coherence.osgi.Activator;
import com.griddynamics.research.coherence.osgi.domain.Computer;
import com.griddynamics.research.coherence.osgi.service.service.api.CacheFactoryService;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CoherenceTestServletTwo extends HttpServlet {

    private static final String CACHE_NAME = "osgi-test-cache";

    private NamedCache testCache = null;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(resp, req);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Object key = req.getParameter("key");
        if (key != null) {
            testCache.put(key, req.getParameter("val"));
        }
        process(resp, req);
    }

    public String getServletInfo() {
        return "Simple Osgi World Servlet";
    }

    private void process(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ServletOutputStream out = response.getOutputStream();

        if (testCache == null) {
            final CacheFactoryService cacheFactoryService = (CacheFactoryService) Activator.getCacheFactoryService();
            if (cacheFactoryService == null) {
                out.println("<h1>Cache Service Is Not Registered</h1>");
                return;
            }

            testCache = cacheFactoryService.getCache(CACHE_NAME);
            testCache.clear();
            for (int i = 0; i < 20; i += 2)
                testCache.put(i, "val_" + i);

            testCache.put("myComp", new Computer(1 << 20, "Indel Core2Duo 3.15 GHz", 2 << 12));
        }

        response.setContentType("text/html");

        out.println("<html><pre>");

        out.println("Cluster: <br>" + testCache.getCacheService().getCluster() + "");
        out.println("Cache: <br>" + testCache + "");
        out.println("Cache Service: <br>" + testCache.getCacheService() + "");

        for (Map.Entry e : (Set<Map.Entry>) testCache.entrySet())
            out.println(String.format("testCache[%1s] = %2s", e.getKey(), e.getValue()));

        out.println("<h3>Add value to cache</h3><br>");
        out.println("<form method=post>" +
                "<input type=text name=\"key\" />" +
                ": <input type=text name=\"val\" />" +
                ": <input type=submit value=\"Add Value\" />" +
                "</form>");

        out.println("</pre></html>");
    }
}
