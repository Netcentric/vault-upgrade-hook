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

import be.orbinson.aem.groovy.console.GroovyConsoleService;
import org.apache.jackrabbit.vault.packaging.InstallContext;


import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.UpgradeInfo;
import biz.netcentric.vlt.upgrade.handler.OsgiUtil;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

/**
 * This handler creates {@link GroovyScript} instances which are executed via
 * the {@link GroovyConsoleService}.
 * 
 * Each child of the {@link UpgradeInfo} node is checked to be a Groovy script.
 */
public class GroovyConsoleHandler implements UpgradeHandler {

    static PackageInstallLogger log = PackageInstallLogger.create(GroovyConsoleHandler.class);

    OsgiUtil osgi = new OsgiUtil();

    @Override
    public boolean isAvailable(InstallContext ctx) {
        boolean available = osgi.hasService(GroovyConsoleService.class);
        if (!available) {
            log.warn(ctx, "Could not load GroovyConsoleService.");
        }
        return available;

    }


    @Override
    public List<UpgradeAction> create(InstallContext ctx, UpgradeInfo info) throws RepositoryException {
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
