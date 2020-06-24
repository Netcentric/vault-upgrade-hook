/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.slingpipes;

import org.apache.sling.testing.clients.ClientException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import biz.netcentric.vlt.upgrade.util.AbstractIT;

public class SlingPipesIT extends AbstractIT {

    private static String testPropertyName;
    private static String testPropertyValue;

    @BeforeClass
    public static void setUpClass() {
        AbstractIT.setUpClass();
        testPropertyName = System.getProperty("vaultUpgradeHook.testpackage.slingpipes.testProperty", "testResourceValue");
        testPropertyValue = System.getProperty("vaultUpgradeHook.testpackage.slingpipes.testPropertyValue", "TestValue");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cleanUpBefore();
        createResource(getTestResourcePath());
        installPackage(packageRelPath);
    }

    @After
    public void tearDown() throws Exception {
        cleanUpAfter();
    }

    @Test
    public void shouldExecuteAction() throws ClientException, InterruptedException {
        assertTestPropertyValue(getTestResourcePath(), testPropertyName, testPropertyValue);
        assertSuccessStatus();
    }

    @Override
    protected String getPackageName() {
        return System.getProperty("vaultUpgradeHook.testpackage.slingpipes.always", "it-slingpipes_always");
    }

    @Override
    protected String getTestResourcePath() {
        return System.getProperty("vaultUpgradeHook.testpackage.slingpipes.resourcePath", "/content/vault-upgrade-test-slingpipes-resource-name");
    }
}
