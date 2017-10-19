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

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.pipes.Plumber;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.handler.SlingUtils;
import biz.netcentric.vlt.upgrade.handler.UpgradeActionInfo;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;
import biz.netcentric.vlt.upgrade.util.PackageInstallLoggerImpl;

public class SlingPipesHandler implements UpgradeHandler {

    private static final PackageInstallLogger LOG = PackageInstallLoggerImpl.create(SlingPipesHandler.class);

    SlingUtils sling = new SlingUtils();

    @Override
    public boolean isAvailable(InstallContext ctx) {
        boolean available = sling.hasService(Plumber.class);
        if (!available) {
            LOG.warn(ctx, "Could not load Plumber.");
        }
        return available;
    }

    @Override
    public Iterable<UpgradeAction> create(InstallContext ctx, UpgradeActionInfo info) throws RepositoryException {
        Collection<UpgradeAction> pipes = new ArrayList<>();
        for (Resource child : sling.getResourceResolver(info.getNode().getSession())
                .getResource(info.getNode().getPath()).getChildren()) {
            if (child.getResourceType().startsWith("slingPipes/")) {
                pipes.add(new SlingPipe(child, info.getDefaultPhase()));
            }
        }
        return pipes;
    }

}
