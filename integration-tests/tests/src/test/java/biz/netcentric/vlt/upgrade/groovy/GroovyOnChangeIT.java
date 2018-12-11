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

public class GroovyOnChangeIT extends GroovyAbstractIT {

    public static final String METADATA_ACTIONS_PROPERTY = "actions";
    static String testPropertyValue;
    static String actionFolder;
    static String scriptName;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testPropertyValue = System.getProperty("vaultUpgradeHook.testpackage.groovy.onchange.testPropertyValue");
        scriptName = System.getProperty("vaultUpgradeHook.testpackage.groovy.onchange.scriptName");
        actionFolder = System.getProperty("vaultUpgradeHook.testpackage.groovy.onchange.actionFolder");
    }

    @Test
    public void shouldExecuteActionOnlyOnce() throws ClientException, InterruptedException {
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);

        String anotherValue = "anotherValue";
        adminClient.setPropertyString(testResourcePath, testPropertyName, anotherValue, 200);

        installPackage(packageRelPath);
        assertTestPropertyValue(testResourcePath, testPropertyName, anotherValue);
    }

    @Test
    public void shouldExecuteActionTwiceOnChange() throws ClientException, InterruptedException {
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);

        String anotherValue = "anotherValue";
        adminClient.setPropertyString(testResourcePath, testPropertyName, anotherValue, 200);
        assertTestPropertyValue(testResourcePath, testPropertyName, anotherValue);

        changeHashAndAssert();

        installPackage(packageRelPath);
        assertTestPropertyValue(testResourcePath, testPropertyName, testPropertyValue);
        assertSuccessStatus();
    }

    private void changeHashAndAssert() throws ClientException, InterruptedException {
        String testChangedHash = scriptName + "_FRnrwoy7jRRFPLk6MxwUUg==";
        String savedHashPath = getUpgraderMetadataPath() + "/" + actionFolder;
        adminClient.setPropertyString(savedHashPath, METADATA_ACTIONS_PROPERTY, testChangedHash, 200);
        assertTestArrayPropertyValue(savedHashPath, METADATA_ACTIONS_PROPERTY, testChangedHash, 0);
    }

    protected String getPackageName() {
        return System.getProperty("vaultUpgradeHook.testpackage.groovy.onchange");
    }
}
