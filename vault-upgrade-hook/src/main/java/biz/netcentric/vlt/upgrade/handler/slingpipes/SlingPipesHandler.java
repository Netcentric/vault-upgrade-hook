/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler.slingpipes;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.pipes.Plumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.UpgradeInfo;
import biz.netcentric.vlt.upgrade.handler.SlingUtils;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;

public class SlingPipesHandler implements UpgradeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SlingPipesHandler.class);

    SlingUtils sling = new SlingUtils();
    private Plumber service;

    @Override
    public boolean isAvailable() {
	return getService() != null;
    }

    @Override
    public Iterable<UpgradeAction> create(UpgradeInfo info) throws RepositoryException {
	Collection<UpgradeAction> pipes = new ArrayList<>();
	for (Resource child : sling.getResourceResolver(info.getNode().getSession())
		.getResource(info.getNode().getPath()).getChildren()) {
	    if (child.getResourceType().startsWith("slingPipes/")) {
		pipes.add(new SlingPipe(getService(), child, info.getDefaultPhase()));
	    }
	}
	return pipes;
    }

    protected Plumber getService() {
	if (service == null) {
	    try {
		service = sling.getService(Plumber.class);
	    } catch (NoClassDefFoundError e) {
		LOG.warn("Could not load Plumber.", e);
	    }
	}
	return service;
    }

}
