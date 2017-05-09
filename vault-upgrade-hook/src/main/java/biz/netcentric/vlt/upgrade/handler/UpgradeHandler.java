/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import javax.jcr.RepositoryException;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.UpgradeInfo;

/**
 * Interface to provide different types of upgrades, e.g. via Groovy script or
 * Sling Pipes. Implement this interface and set {@link UpgradeInfo#PN_HANDLER}
 * to provide a custom type.
 */
public interface UpgradeHandler {

    /**
     * Is called to check if all requirements are met to create and run
     * {@link UpgradeAction}s provided by this handler.
     * 
     * @return
     */
    boolean isAvailable();

    /**
     * Called by {@link UpgradeInfo} to get the {@link UpgradeAction}s which are
     * configured in the content package.
     * 
     * @param info
     * @return
     * @throws RepositoryException
     */
    Iterable<UpgradeAction> create(UpgradeInfo info) throws RepositoryException;

}
