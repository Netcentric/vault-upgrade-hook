/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.script;

import org.apache.sling.testing.clients.ClientException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import biz.netcentric.vlt.upgrade.util.AbstractIT;

public class ScriptIT extends AbstractIT {

    private String testResourcePath;
    private String testPropertyName;
    private String testPropertyValue;

    @Before
    public void setUp() throws Exception {
        testResourcePath = getTestResourcePath();
        testPropertyName = System.getProperty("vaultUpgradeHook.testpackage.script.testProperty");
        testPropertyValue = System.getProperty("vaultUpgradeHook.testpackage.script.testPropertyValue");

        super.setUp();
        cleanUpBefore();
        createResource(testResourcePath);
        installPackage(packageRelPath);
    }

    @After
    public void tearDown() throws Exception {
        cleanUpAfter();
    }

    @Test
    public void shouldExecuteAction() throws ClientException, InterruptedException {
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);
        assertSuccessStatus();
    }

    @Override
    protected String getPackageName() {
        return System.getProperty("vaultUpgradeHook.testpackage.script.always");
    }

    @Override
    protected String getTestResourcePath() {
        return System.getProperty("vaultUpgradeHook.testpackage.script.resourcePath");
    }
}
