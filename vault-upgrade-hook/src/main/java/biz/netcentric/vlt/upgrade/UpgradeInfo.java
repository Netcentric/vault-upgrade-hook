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
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;

import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;
import biz.netcentric.vlt.upgrade.handler.UpgradeType;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

/**
 * This class represents the wrapper to execute {@link UpgradeAction}s. It loads
 * and holds the configuration.
 */
public class UpgradeInfo {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(UpgradeInfo.class);

    /**
     * This property configures the handler to execute this {@link UpgradeInfo}s
     * {@link UpgradeAction}s.<br>
     * <br>
     * This configuration is optional and defaults to {@link #DEFAULT_HANDLER}.
     */
    public static final String PN_HANDLER = "handler";
    public static final String DEFAULT_HANDLER = UpgradeType.SCRIPT.toString();

    /**
     * {@link UpgradeAction}s are executed in one of the installation
     * {@link Phase}s. The phase configured via this property will be used if
     * the {@link UpgradeAction} is not prefixed by a specific phase, see
     * {@link UpgradeAction#getPhase()} and
     * {@link UpgradeAction#getPhaseFromPrefix(Phase, String)}.<br>
     * <br>
     * This configuration is optional and defaults to {@link #DEFAULT_PHASE}.
     */
    public static final String PN_DEFAULT_PHASE = "defaultPhase";
    public static final String DEFAULT_PHASE = Phase.PREPARE.toString();

    /**
     * @see RunMode
     */
    public static final String PN_RUN_MODE = "runMode";
    public static final String DEFAULT_RUN_MODE = RunMode.ON_CHANGE.toString();

    private final Node node;
    private final UpgradeStatus status;
    private final RunMode runMode;
    private final Phase defaultPhase;
    private final UpgradeHandler handler;
    private final Map<Phase, List<UpgradeAction>> actions = new EnumMap<>(Phase.class);
	private final Set<UpgradeAction> executedActions = new LinkedHashSet<>();

    public UpgradeInfo(InstallContext ctx, UpgradeStatus status, Node node) throws RepositoryException {
        this.status = status;
        this.node = node;
        runMode = RunMode.valueOf(JcrUtils.getStringProperty(node, PN_RUN_MODE, DEFAULT_RUN_MODE).toUpperCase());
        defaultPhase = Phase.valueOf(JcrUtils.getStringProperty(node, PN_DEFAULT_PHASE, DEFAULT_PHASE).toUpperCase());
        handler = UpgradeType.create(ctx, JcrUtils.getStringProperty(node, PN_HANDLER, DEFAULT_HANDLER));
        loadActions(ctx);
        LOG.debug(ctx, "UpgradeInfo loaded [{}]", this);
    }

    private void loadActions(InstallContext ctx) throws RepositoryException {
        for (Phase availablePhase : Phase.values()) {
            actions.put(availablePhase, new ArrayList<UpgradeAction>());
        }
        for (UpgradeAction action : handler.create(ctx, this)) {
            actions.get(action.getPhase()).add(action);
        }
        for (Phase availablePhase : Phase.values()) {
            Collections.sort(actions.get(availablePhase)); // make sure the
                                                           // scripts are
                                                           // correctly sorted
        }
    }

	public void executed(UpgradeAction action) {
		getExecutedActions().add(action);
	}

    /**
     * This configuration affects the installation behavior of
     * {@link UpgradeAction}s, see
     * 
     * <ul>
     * <li>{@link #ON_CHANGE} - actions will be executed once.</li>
     * <li>{@link #ALWAYS} - actions will be executed on every intallation.</li>
     * </ul>
     */
    public enum RunMode {
        /**
         * Run all new or changed actions.
         */
        ON_CHANGE,

        /**
         * Completely disregarding previous executions.
         */
        ALWAYS
    }

    @Override
    public String toString() {
        return super.toString() + " [node=" + node + ", status=" + status + ", runMode=" + runMode + ", defaultPhase="
                + defaultPhase + ", handler=" + handler + ", actions=" + actions + "]";
    }

    public Map<Phase, List<UpgradeAction>> getActions() throws RepositoryException {
        return actions;
    }

    public UpgradeStatus getStatus() {
        return status;
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

    public Set<UpgradeAction> getExecutedActions() {
        return executedActions;
    }

}
