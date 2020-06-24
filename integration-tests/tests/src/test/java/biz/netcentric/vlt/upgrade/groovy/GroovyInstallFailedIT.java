/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.groovy;

import org.apache.sling.testing.clients.ClientException;
import org.junit.Before;
import org.junit.Test;

public class GroovyInstallFailedIT extends GroovyAbstractIT {

    private String testPropertyValue;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testPropertyValue = System.getProperty("vaultUpgradeHook.testpackage.groovy.installfailed.testPropertyValue", "installfailed_end");
    }

    @Test
    public void shouldExecuteFailedScript() throws ClientException, InterruptedException {
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);
    }

    protected String getPackageName() {
        return System.getProperty("vaultUpgradeHook.testpackage.groovy.installfailed", "it-groovy_install_failed");
    }
}
