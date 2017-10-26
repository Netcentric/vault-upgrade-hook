/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import java.io.IOException;

import org.apache.sling.testing.clients.ClientException;
import org.junit.Test;

public class UserPreferencesAlwaysIT extends UserPreferencesAbstract {

    @Test
    public void shouldUpdateUserPreferencePropertyOnFirstInstall() throws ClientException, InterruptedException, IOException {
        assertTestPropertyValue(getUserPath(testUser), testPropertyName, testPropertyValue);
    }

    @Test
    public void shouldAlwaysExecuteAction() throws ClientException, InterruptedException, IOException {
        String userPath = getUserPath(testUser);
        assertTestPropertyValue(userPath, testPropertyName, testPropertyValue);

        String anotherValue = "anotherValue";
        adminClient.setPropertyString(userPath + "/preferences", testPropertyName, anotherValue, 200);
        assertTestPropertyValue(userPath, testPropertyName, anotherValue);

        installPackage(packageRelPath);
        assertTestPropertyValue(userPath, testPropertyName, testPropertyValue);
    }
}
