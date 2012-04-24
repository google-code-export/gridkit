/*
* Copyright (c) 2006-2009 Grid Dynamics, Inc.
* 2030 Bent Creek Dr., San Ramon, CA 94582
* All Rights Reserved.
*
* This software is the confidential and proprietary information of
* Grid Dynamics, Inc. ("Confidential Information"). You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Grid Dynamics.
*/
package com.griddynamics.research.coherence.osgi.servicelister;

import com.griddynamics.research.coherence.osgi.corebundle.MyCacheFactoryBuilder;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class ViewCoherenceServicesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        showServices(httpServletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

    }

    private void showServices(HttpServletResponse httpServletResponse) throws IOException {
        final ServletOutputStream out = httpServletResponse.getOutputStream();
//        CacheFactory cf = ((MyCacheFactoryBuilder)CacheFactory.getCacheFactoryBuilder()).getSingletonFactory();
        final Cluster cluster = CacheFactory.getCluster();
        final Enumeration serviceNames = cluster.getServiceNames();
        out.println("<html><body><pre>");

        out.println("<h3>Services runned at the cluster:</h3>");
        for (
                String name = (String) serviceNames.nextElement();
                serviceNames.hasMoreElements();
                name = (String) serviceNames.nextElement()) {
            out.println(cluster.getService(name).toString() + "<br/>");
        }

        out.println("</pre></body></html>");
    }
}
