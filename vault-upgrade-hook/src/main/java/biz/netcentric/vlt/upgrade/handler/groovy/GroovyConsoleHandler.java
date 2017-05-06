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
import biz.netcentric.vlt.upgrade.handler.OsgiUtil.ServiceWrapper;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;

/**
 * This handler creates {@link GroovyScript} instances which are executed via
 * the {@link GroovyConsoleService}.
 * 
 * Each child of the {@link UpgradeInfo} node is checked to be a Groovy script.
 */
public class GroovyConsoleHandler implements UpgradeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GroovyConsoleHandler.class);

    OsgiUtil osgi = new OsgiUtil();

    @Override
    public boolean isAvailable() {
	ServiceWrapper<GroovyConsoleService> serviceWrapper = null;
	try {
	    serviceWrapper = osgi.getService(GroovyConsoleService.class);
	    return serviceWrapper != null;
	} catch (NoClassDefFoundError e) {
	    LOG.warn("Could not load GroovyConsoleService.", e);
	    return false;
	} finally {
	    osgi.close(serviceWrapper);
	}
    }

    @Override
    public List<UpgradeAction> create(UpgradeInfo info) throws RepositoryException {
        List<UpgradeAction> scripts = new ArrayList<>();

        NodeIterator nodes = info.getNode().getNodes();
	while (nodes.hasNext()) {
	    Node child = nodes.nextNode();
	    if (child.getName().endsWith(".groovy") && child.isNodeType("nt:file")) {
		scripts.add(new GroovyScript(child, info.getDefaultPhase()));
	    }
	}

        return scripts;
    }

}
