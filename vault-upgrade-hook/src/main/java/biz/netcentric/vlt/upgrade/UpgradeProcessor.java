/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.jackrabbit.vault.packaging.InstallHook;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageId;

import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

/**
 * This class is the main entry point for the <b>vault-upgrade-hook</b>
 * execution. {@link #execute(InstallContext)} is called for each content
 * package installation {@link Phase}. On {@link Phase#PREPARE} the environment
 * consisting of {@link UpgradeStatus}, {@link UpgradeInfo}s and
 * {@link UpgradeAction}s is created. The packages are executed for each phase.
 * On {@link Phase#END} the provided {@link Session} will be saved and the
 * status of the upgrade will be stored to {@code /var/upgrade}.
 */
public class UpgradeProcessor implements InstallHook {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(UpgradeProcessor.class);

    /**
     * Absolute path where information of the {@link UpgradeInfo} execution are
     * stored.
     */
    public static final String STATUS_PATH = "/var/upgrade";

    /**
     * Relative path within a content package where {@link UpgradeInfo}
     * definitions are stored.
     */
    public static final String UPGRADER_PATH_IN_PACKAGE = ".zip/jcr:content/vlt:definition/upgrader";

    // fields are package private for unit tests
    UpgradeStatus status;
    List<UpgradeInfo> infos;

    @Override
    public void execute(InstallContext ctx) throws PackageException {
	LOG.info(ctx, "starting [{}]", ctx.getPhase());

	try {
	    switch (ctx.getPhase()) {
	    case PREPARE:
		loadStatus(ctx);
		loadInfos(ctx);
		executeInfos(ctx);
		break;
	    case INSTALLED:
	    case PREPARE_FAILED:
	    case INSTALL_FAILED:
		executeInfos(ctx);
		break;
	    case END:
		executeInfos(ctx);
		status.update(ctx);
		updateActions(ctx);
		ctx.getSession().save();
		break;
	    }
	} catch (Exception e) {
	    LOG.error(ctx, "Error during content upgrade", e);
	    throw new PackageException(e);
	} finally {
	    LOG.debug(ctx, "finished [{}]", ctx.getPhase());
	}
    }

    protected void updateActions(InstallContext ctx) throws RepositoryException {
	LOG.debug(ctx, "updating actions [{}]", infos);
	for (UpgradeInfo info : infos) {
	    status.updateActions(ctx, info);
	}
    }

    protected void executeInfos(InstallContext ctx) throws RepositoryException {
	LOG.debug(ctx, "starting package execution [{}]", infos);
	for (UpgradeInfo info : infos) {
	    info.execute(ctx);
	}
    }

    protected void loadInfos(InstallContext ctx) throws RepositoryException {

	String upgradeInfoPath = ctx.getPackage().getId().getInstallationPath() + UPGRADER_PATH_IN_PACKAGE;
	Node upgradeInfoNode = ctx.getSession().getNode(upgradeInfoPath);
	LOG.debug(ctx, "loading packages [{}]: [{}]", upgradeInfoPath, upgradeInfoNode);

	infos = new ArrayList<>();

	if (upgradeInfoNode != null) {

	    NodeIterator nodes = upgradeInfoNode.getNodes();
	    while (nodes.hasNext()) {
		Node child = nodes.nextNode();
		final UpgradeInfo info = new UpgradeInfo(status, child, ctx.getPackage().getId().getVersionString());
		LOG.debug(ctx, "info [{}]", info);
		if (info.isRelevant()) {
		    infos.add(info);
		} else {
		    LOG.debug(ctx, "package not relevant: [{}]", child);
		}
	    }

	    // sort upgrade infos according to their version and priority
	    Collections.sort(infos);
	} else {
	    LOG.warn(ctx, "Could not load upgrade info [{}]", upgradeInfoPath);
	}
    }

    protected void loadStatus(InstallContext ctx) throws RepositoryException {
	String statusPath = getStatusPath(ctx.getPackage().getId());
	status = new UpgradeStatus(ctx, statusPath);
    }

    protected String getStatusPath(PackageId packageId) {
	return STATUS_PATH + "/" + packageId.getGroup() + "/" + packageId.getName();
    }

}
