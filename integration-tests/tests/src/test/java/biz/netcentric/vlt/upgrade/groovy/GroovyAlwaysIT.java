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

public class GroovyAlwaysIT extends GroovyAbstractIT {

    static String testPropertyValue;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testPropertyValue = System.getProperty("vaultUpgradeHook.testpackage.groovy.always.testPropertyValue", "TestValue");
    }

    @Test
    public void shouldExecuteAction() throws ClientException, InterruptedException {
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);
        assertSuccessStatus();
    }

    @Test
    public void shouldExecuteActionOnSecondInstallation() throws ClientException, InterruptedException {
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);
        assertSuccessStatus();

        String anotherValue = "anotherValue";
        adminClient.setPropertyString(testResourcePath, testPropertyName, anotherValue, 200);
        assertTestPropertyValue(testResourcePath, testPropertyName, anotherValue);

        installPackage(packageRelPath);
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);
        assertSuccessStatus();
    }

    protected String getPackageName() {
        return System.getProperty("vaultUpgradeHook.testpackage.groovy.always", "it-groovy_always");
    }
}
