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
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import biz.netcentric.vlt.upgrade.util.AbstractIT;

public abstract class GroovyAbstractIT extends AbstractIT {

    static String testResourcePath;
    static String testPropertyName;

    @BeforeClass
    public static void setUpClass() {
        AbstractIT.setUpClass();
        testPropertyName = System.getProperty("vaultUpgradeHook.testpackage.groovy.testProperty", "testResourceValue");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cleanUpBefore();

        testResourcePath = getTestResourcePath();
        createResource(testResourcePath);
        installPackage(packageRelPath);
    }

    @After
    public void tearDown() throws ClientException, InterruptedException {
        cleanUpAfter();
    }

    @Override
    protected String getTestResourcePath() {
        return System.getProperty("vaultUpgradeHook.testpackage.groovy.resourcePath", "/content/vault-upgrade-test-resource-name");
    }
}
