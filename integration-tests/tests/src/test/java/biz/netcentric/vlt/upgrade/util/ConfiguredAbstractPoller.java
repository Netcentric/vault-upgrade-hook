/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import org.apache.sling.testing.clients.util.poller.AbstractPoller;

public abstract class ConfiguredAbstractPoller extends AbstractPoller {

    private static final int WAIT = Integer.getInteger("vaultUpgradeHook.testsettings.request.wait", 0);
    private static final int RETRIES = Integer.getInteger("vaultUpgradeHook.testsettings.request.retries", 0);

    public ConfiguredAbstractPoller() {
        super(WAIT, RETRIES);
    }

    public boolean call() {
        return true;
    }
}
