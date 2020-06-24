/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.userpreferences;

import java.io.IOException;

import org.apache.sling.testing.clients.ClientException;
import org.junit.Test;

public class UserPreferencesAlwaysIT extends UserPreferencesAbstract {

    @Test
    public void shouldUpdateUserPreferencePropertyOnFirstInstall() throws ClientException, InterruptedException, IOException {
        assertTestUserPropertyValue(getUserPath(testUser), testPropertyName, testPropertyValue);
        assertSuccessStatus();
    }

    @Test
    public void shouldAlwaysExecuteAction() throws ClientException, InterruptedException, IOException {
        String userPath = getUserPath(testUser);
        assertTestUserPropertyValue(userPath, testPropertyName, testPropertyValue);

        String anotherValue = "anotherValue";
        adminClient.setPropertyString(userPath + "/preferences", testPropertyName, anotherValue, 200);
        assertTestUserPropertyValue(userPath, testPropertyName, anotherValue);

        installPackage(packageRelPath);
        assertTestUserPropertyValue(userPath, testPropertyName, testPropertyValue);
        assertSuccessStatus();
    }

    protected String getPackageName() {
        return System.getProperty("vaultUpgradeHook.testpackage.userconfig.always", "it-userconfig_always");
    }
}
