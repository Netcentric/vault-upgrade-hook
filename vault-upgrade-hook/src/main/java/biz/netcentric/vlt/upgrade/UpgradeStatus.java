/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.Version;

import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

/**
 * This class represents a previous upgrade execution and has methods to compare
 * the current execution with the former.
 */
public class UpgradeStatus {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(UpgradeStatus.class);

    public static final String PN_UPGRADE_TIME = "time";
    public static final String PN_VERSION = "version";
    public static final String PN_ACTIONS = "actions";

    private final Node node;
    private final Version version;

    public UpgradeStatus(InstallContext ctx, String path) throws RepositoryException {
	LOG.debug(ctx, "loading status [{}]", path);
	node = JcrUtils.getOrCreateByPath(path, JcrConstants.NT_FOLDER, ctx.getSession());
	version = createVersion(node);
	LOG.info(ctx, "loaded status [{}]", this);
    }

    protected static Version createVersion(Node node) throws RepositoryException {
	if (node.hasProperty(PN_VERSION)) {
	    return Version.create(node.getProperty(PN_VERSION).getString());
	} else {
	    return null;
	}
    }

    public boolean isInitial() {
	return version == null;
    }

    public boolean notExecuted(InstallContext ctx, UpgradeInfo info, UpgradeAction action) throws RepositoryException {
	Node infoStatus = getInfoStatus(info);
	if (infoStatus != null && infoStatus.hasProperty(PN_ACTIONS)) {
	    for (Value executedAction : infoStatus.getProperty(PN_ACTIONS).getValues()) {
		if (executedAction.getString().equals(action.getName())) {
		    LOG.debug(ctx, "action [{}] already exected: [{}]", action, infoStatus);
		    return false;
		}
	    }
	}
	LOG.debug(ctx, "action [{}] not exected yet: [{}]", action, infoStatus);
	return true;
    }

    protected Node getInfoStatus(UpgradeInfo info) throws RepositoryException {
	String packagePath = node.getPath() + "/" + info.getNode().getName();
	JcrUtils.getOrCreateByPath(packagePath, JcrConstants.NT_FOLDER, node.getSession());
	return JcrUtils.getNodeIfExists(packagePath, node.getSession());
    }

    public void update(InstallContext ctx) throws RepositoryException {
	node.setProperty(PN_UPGRADE_TIME, Calendar.getInstance());
	String versionString = ctx.getPackage().getId().getVersionString();
	node.setProperty(PN_VERSION, versionString);
	LOG.info(ctx, "stored new status [{}]: [{}]", node, versionString);
    }

    public void updateActions(InstallContext ctx, UpgradeInfo info) throws RepositoryException {
	Node infoStatus = getInfoStatus(info);
	String[] executedActions = info.getExecutedActions().toArray(new String[info.getExecutedActions().size()]);
	infoStatus.setProperty(PN_ACTIONS, executedActions);
	LOG.info(ctx, "stored executed actions [{}]: [{}]", infoStatus, executedActions);
    }

    public Version getLastExecution() {
	checkStatus();
	return version;
    }

    protected void checkStatus() {
	if (isInitial()) {
	    throw new IllegalStateException("Cannot check values of an initial status.");
	}
    }

    @Override
    public String toString() {
	return super.toString() + " [node=" + node + ", version=" + version + "]";
    }

}
