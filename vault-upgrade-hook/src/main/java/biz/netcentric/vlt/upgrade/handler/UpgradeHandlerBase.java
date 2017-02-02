/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import static biz.netcentric.vlt.upgrade.util.LogUtil.info;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.sling.api.resource.ResourceResolver;

import biz.netcentric.vlt.upgrade.UpgradeInfo;
import biz.netcentric.vlt.upgrade.util.Util;

public abstract class UpgradeHandlerBase {

    public static final long SAVE_THRESHOLD = 1000;

    protected InstallContext ctx;
    protected UpgradeInfo upgradeInfo;
    protected static final InstallContext.Phase[] phases = InstallContext.Phase.values();
    private Session session;


	// ----< lifecycle >--------------------------------------------------------

    public void execute(InstallContext ctx) throws RepositoryException {
        this.ctx = ctx;

        switch (ctx.getPhase()) {
            case PREPARE:
                doPrepare(ctx);
                break;
            case PREPARE_FAILED:
                doPrepareFailed(ctx);
                break;
            case INSTALLED:
                doInstalled(ctx);
                break;
            case INSTALL_FAILED:
                doInstallFailed(ctx);
                break;
            case END:
                doEnd(ctx);
                break;
        }
    }

	protected void doPrepare(InstallContext ctx) throws RepositoryException {
		// implement in subclasses
	}

    protected void doPrepareFailed(InstallContext ctx) throws RepositoryException {
		// implement in subclasses
	}

    protected void doInstalled(InstallContext ctx) throws RepositoryException {
		// implement in subclasses
	}

    protected void doInstallFailed(InstallContext ctx) throws RepositoryException {
		// implement in subclasses
	}

    protected void doEnd(InstallContext ctx) throws RepositoryException {
		// implement in subclasses
	}


    // ----< saving >--------------------------------------------------------

    /**
     * Save the JCR session.
     */
    public void save() {
        try {
            session.save();
        } catch (RepositoryException e) {
            info("E", "Failed to save changes." ,ctx);
        }
    }

    /**
     * Save the JCR session, if the specified count of changes exceeds our saving threshold.
     * @param count The count of changes.
     * @return The remaining count of changes.
     */
    public long saveOnThreshold(long count) {
        long returnCount = count;
        if(count >= UpgradeHandlerBase.SAVE_THRESHOLD) {
            info("", "Saving approx " + count + " nodes." ,ctx);
            save();
            returnCount = 0;
        }

        return returnCount;
    }

    // ----< accessors >--------------------------------------------------------

    public void setUpgradeInfo(UpgradeInfo upgradeInfo) {
        this.upgradeInfo = upgradeInfo;
    }

    public InstallContext getCtx() {
        return ctx;
    }

    protected Session getSession() {
        if(session == null) {
            session = ctx.getSession();
        }
        return session;
    }

    protected ResourceResolver getResourceResolver() {
        return Util.getResourceResolver(ctx);
    }

    /**
     * returns the correct Phase for a script name by its prefix.
     * Important to handle PREPARE_FAILED and PREPARE correctly
     * @param text  the script name
     * @return      related phase. defaults to INSTALLED
     */
    protected InstallContext.Phase getPhaseFromPrefix(String text) {
        String scriptName = text.toLowerCase();
        InstallContext.Phase phase = InstallContext.Phase.INSTALLED;
        for (int i = phases.length - 1; i >= 0; i--) {
            if (StringUtils.startsWithIgnoreCase(scriptName, phases[i].name())) {
                phase = phases[i];
                break;
            }
        }
        return phase;
    }
}
