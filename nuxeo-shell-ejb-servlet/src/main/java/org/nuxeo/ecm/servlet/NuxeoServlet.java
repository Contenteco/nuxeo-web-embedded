/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nuxeo.osgi.application.FrameworkBootstrap;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class NuxeoServlet extends HttpServlet {

    private static final long serialVersionUID = 7547124250731789991L;

    protected ServletHandler handler;

    protected FrameworkBootstrap fb;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        if (fb == null) {
            try {
                fb = loadRuntime(config);
            } catch (Exception e) {
                throw new ServletException("Cannot load nuxeo", e);
            }
        }

        String handlerClass = getInitParameter("handler");
        if (handlerClass == null) {
            throw new ServletException(
                    "No servlet handler class specified. "
                            + "In web.xml you need to add an init parameter to that server "
                            + "named 'handler' that points to a servlet handler");
        }

        try {
            final Object newInstance = fb.getClassLoader().loadClass(
                    handlerClass).newInstance();
            handler = (ServletHandler) newInstance;
            initHandler();
        } catch (Exception e) {
            throw new ServletException("Failed to instantiate handler", e);
        }
    }

    @Override
    public void destroy() {
        destroyHandler();
        handler = null;
        super.destroy();
    }

    private void initHandler() throws Exception {
        Thread cthread = Thread.currentThread();
        ClassLoader cl = cthread.getContextClassLoader();
        cthread.setContextClassLoader(fb.getClassLoader());
        try {
            handler.init(this);
        } finally {
            cthread.setContextClassLoader(cl);
        }
    }

    private void destroyHandler() {
        Thread cthread = Thread.currentThread();
        ClassLoader cl = cthread.getContextClassLoader();
        cthread.setContextClassLoader(fb.getClassLoader());
        try {
            handler.destroy();
        } finally {
            cthread.setContextClassLoader(cl);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Thread cthread = Thread.currentThread();
        ClassLoader cl = cthread.getContextClassLoader();
        cthread.setContextClassLoader(fb.getClassLoader());
        try {
            if (req.getAuthType() != null) {
                if (req.getUserPrincipal() == null) {
                    throw new ServletException("Not authenticated");
                }
            }
            super.service(req,resp);
        } finally {
            cthread.setContextClassLoader(cl);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handler.doDelete(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handler.doGet(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handler.doHead(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        handler.doOptions(arg0, arg1);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handler.doPost(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handler.doPut(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        handler.doTrace(arg0, arg1);
    }

    protected static FrameworkBootstrap loadRuntime(ServletConfig config)
            throws Exception {
        String warFile = config.getServletContext().getRealPath("");
        String root = config.getInitParameter("nxhome");
        if (root == null) {
            root = "";
        }
        File appRoot = null;
        if (root.startsWith("/")) {
            appRoot = new File(root);
        } else {
            appRoot = new File(warFile + "/" + root);
        }
        root = appRoot.getAbsolutePath();

        FrameworkBootstrap fb;
        fb = new FrameworkBootstrap(NuxeoServlet.class.getClassLoader(),
                appRoot);
        fb.initialize();
        fb.start();
        return fb;
    }

}
