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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.VaultPackage;
import org.apache.jackrabbit.vault.packaging.Version;

import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;
import biz.netcentric.vlt.upgrade.handler.UpgradeType;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

/**
 * This class represents the wrapper to execute {@link UpgradeAction}s. It loads
 * and holds the configuration.
 */
public class UpgradeInfo implements Comparable<UpgradeInfo> {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(UpgradeInfo.class);

    /**
     * <i>Optional configuration property.</i> If not set the content package
     * version is the default:
     * {@link InstallContext#getPackage()}->{@link VaultPackage#getId()}->{@link PackageId#getVersionString()}<br>
     * <br>
     * The target version of the upgrade.
     */
    public static final String PN_TARGET_VERSION = "targetVersion";

    public static final String PN_SAVE_THRESHOLD = "saveThreshold";
    public static final long DEFAULT_SAVE_THRESHOLD = 1000l;

    public static final String PN_PRIORITY = "priority";
    public static final long DEFAULT_PRIORITY = Long.MAX_VALUE;

    public static final String PN_HANDLER = "handler";
    public static final String DEFAULT_HANDLER = UpgradeType.GROOVY.toString();

    public static final String PN_DEFAULT_PHASE = "defaultPhase";
    public static final String DEFAULT_PHASE = Phase.INSTALLED.toString();

    public static final String PN_RUN_MODE = "runMode";
    public static final String DEFAULT_RUN_MODE = RunMode.INCREMENTAL.toString();

    public static final String PN_SKIP_ON_INITIAL = "skipOnInitial";
    public static final boolean DEFAULT_SKIP_ON_INITIAL = true;

    private final Node node;
    private final UpgradeStatus status;
    private final long priority;
    private final long saveThreshold;
    private final Version targetVersion;
    private final RunMode runMode;
    private final boolean skipOnInitial;
    private final Phase defaultPhase;
    private final UpgradeHandler handler;
    private final List<String> executedActions = new ArrayList<>();
    private final Map<Phase, List<UpgradeAction>> actions = new HashMap<>();
    private long counter = 0;

    public UpgradeInfo(UpgradeStatus status, Node node, String packageVersion) throws RepositoryException {
	this.status = status;
	this.node = node;
	priority = JcrUtils.getLongProperty(node, PN_PRIORITY, DEFAULT_PRIORITY);
	saveThreshold = JcrUtils.getLongProperty(node, PN_SAVE_THRESHOLD, DEFAULT_SAVE_THRESHOLD);
	targetVersion = Version.create(JcrUtils.getStringProperty(node, PN_TARGET_VERSION, packageVersion));
	runMode = RunMode.valueOf(JcrUtils.getStringProperty(node, PN_RUN_MODE, DEFAULT_RUN_MODE).toUpperCase());
	skipOnInitial = JcrUtils.getBooleanProperty(node, PN_SKIP_ON_INITIAL, DEFAULT_SKIP_ON_INITIAL);
	defaultPhase = Phase.valueOf(JcrUtils.getStringProperty(node, PN_DEFAULT_PHASE, DEFAULT_PHASE).toUpperCase());
	handler = UpgradeType.create(JcrUtils.getStringProperty(node, PN_HANDLER, DEFAULT_HANDLER));
	loadActions();
    }

    private void loadActions() throws RepositoryException {
	for (Phase availablePhase : Phase.values()) {
	    actions.put(availablePhase, new ArrayList<UpgradeAction>());
	}
	for (UpgradeAction action : handler.create(this)) {
	    actions.get(action.getPhase()).add(action);
	}
	for (Phase availablePhase : Phase.values()) {
	    Collections.sort(actions.get(availablePhase)); // make sure the
							   // scripts are
							   // correctly sorted
	}
    }

    public void execute(InstallContext ctx) throws RepositoryException {
	List<UpgradeAction> actionsOfPhase = getActions().get(ctx.getPhase());
	LOG.debug(ctx, "executing [{}]: [{}]", this, actionsOfPhase);
	boolean reinstall = false;
	for (UpgradeAction action : actionsOfPhase) {
	    if (reinstall || getRunMode() == RunMode.ALWAYS || action.isRelevant(ctx, this)) {
		reinstall = true; // if the one action was regarded relevant all
				  // following actions are also executed no
				  // matter what their status is
		action.execute(ctx);
		executedActions.add(action.getName());
		saveOnThreshold(ctx, ++counter);
	    }
	}
    }

    public Map<Phase, List<UpgradeAction>> getActions() throws RepositoryException {
	return actions;
    }

    protected void saveOnThreshold(InstallContext ctx, long count) throws RepositoryException {
	if (saveThreshold > 0 && count % saveThreshold == 0) {
	    LOG.info(ctx, "saving [{}]", count);
	    ctx.getSession().save();
	}
    }

    public boolean isRelevant() {
	if (runMode == UpgradeInfo.RunMode.ALWAYS) {
	    return true;
	}
	if (skipOnInitial && status.isInitial()) {
	    return false; // don't spool all upgrades on a new installation
	}
	return status.getLastExecution().compareTo(getTargetVersion()) <= 0;
    }

    public enum RunMode {
	/**
	 * Run if current version >= version of the last execution and run each
	 * action exactly once.
	 */
	INCREMENTAL,

	/**
	 * Completely disregarding previous executions.
	 */
	ALWAYS
    }

    @Override
    public int compareTo(UpgradeInfo other) {
	// first sorting criterion: version
	int versionCompare = targetVersion.compareTo(other.targetVersion);
	if (versionCompare == 0) {
	    // second sorting criterion: priority
	    return (priority < other.priority) ? -1 : ((priority == other.priority) ? 0 : 1);
	} else {
	    return versionCompare;
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((node == null) ? 0 : node.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	UpgradeInfo other = (UpgradeInfo) obj;
	if (node == null) {
	    if (other.node != null)
		return false;
	} else if (!node.equals(other.node))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return super.toString() + " [node=" + node + ", status=" + status + ", priority=" + priority
		+ ", saveThreshold=" + saveThreshold + ", version=" + targetVersion + ", runMode=" + runMode
		+ ", skipOnInitial=" + skipOnInitial + ", defaultPhase=" + defaultPhase + ", handler=" + handler
		+ ", executedActions=" + executedActions + ", actions=" + actions + ", counter=" + counter + "]";
    }

    public List<String> getExecutedActions() {
	return executedActions;
    }

    public long getCounter() {
	return counter;
    }

    public UpgradeStatus getStatus() {
	return status;
    }

    public long getPriority() {
	return priority;
    }

    public long getSaveThreshold() {
	return saveThreshold;
    }

    public Version getTargetVersion() {
	return targetVersion;
    }

    public RunMode getRunMode() {
	return runMode;
    }

    public Phase getDefaultPhase() {
	return defaultPhase;
    }

    public UpgradeHandler getHandler() {
	return handler;
    }

    public Node getNode() {
	return node;
    }

    public boolean isSkipOnInitial() {
	return skipOnInitial;
    }

}
