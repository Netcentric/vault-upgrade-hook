/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler.groovy;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.citytechinc.aem.groovy.console.GroovyConsoleService;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.UpgradeInfo;
import biz.netcentric.vlt.upgrade.handler.OsgiUtil;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;

/**
 * This handler creates {@link GroovyScript} instances which are executed via
 * the {@link GroovyConsoleService}.
 * 
 * Each child of the {@link UpgradeInfo} node is checked to be a Groovy script.
 */
public class GroovyConsoleHandler implements UpgradeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GroovyConsoleHandler.class);

    private static OsgiUtil osgi = new OsgiUtil();
    private GroovyConsoleService service;

    @Override
    public boolean isAvailable() {
	return getService() != null;
    }

    @Override
    public List<UpgradeAction> create(UpgradeInfo info) throws RepositoryException {
	List<UpgradeAction> scripts = new ArrayList<>();

	NodeIterator nodes = info.getNode().getNodes();
	while (nodes.hasNext()) {
	    Node child = nodes.nextNode();
	    if (child.getName().endsWith(".groovy") && child.isNodeType("nt:file")) {
		scripts.add(new GroovyScript(getService(), child, info.getDefaultPhase()));
	    }
	}

	return scripts;
    }

    protected GroovyConsoleService getService() {
	if (service == null) {
	    try {
		service = osgi.getService(GroovyConsoleService.class);
	    } catch (NoClassDefFoundError e) {
		LOG.warn("Could not load GroovyConsoleService.", e);
	    }
	}
	return service;
    }

    public static void setOsgi(OsgiUtil osgi) {
	GroovyConsoleHandler.osgi = osgi;
    }

}
