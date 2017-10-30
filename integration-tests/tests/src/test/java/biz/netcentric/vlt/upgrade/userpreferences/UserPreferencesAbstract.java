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
import java.util.Collections;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import biz.netcentric.vlt.upgrade.util.AbstractIT;
import biz.netcentric.vlt.upgrade.util.GetExecutor;
import biz.netcentric.vlt.upgrade.util.PostExecutor;
import biz.netcentric.vlt.upgrade.util.Predicate;

public abstract class UserPreferencesAbstract extends AbstractIT {

    static String testUser;
    static String testPropertyName;
    static String testPropertyValue;

    @BeforeClass
    public static void setUpClass() {
        AbstractIT.setUpClass();
        testUser = System.getProperty("vaultUpgradeHook.testpackage.userconfig.testUser");
        testPropertyName = System.getProperty("vaultUpgradeHook.testpackage.userconfig.testProperty");
        testPropertyValue = System.getProperty("vaultUpgradeHook.testpackage.userconfig.testPropertyValue");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (adminClient != null && checkIfUserExist(testUser)) {

            LOG.warn("### WARNING ### test user 'test-vault-hook' already exists." +
                    "This may be the result of failed test or test without clean up");
            deleteUser(getUserPath(testUser));
        }

        createUser();
        installPackage(packageRelPath);
    }

    @After
    public void tearDown() throws ClientException, IOException, InterruptedException {
        if (adminClient != null && checkIfUserExist(testUser)) {
            deleteUser(getUserPath(testUser));
        } else {
            LOG.warn("### WARNING ### adminClient is null, so I can not clean up repo after the test.");
        }
    }

    void assertTestUserPropertyValue(String testUserPath, String testPrefPropertyName, String testPrefPropertyExpectedValue)
            throws ClientException, InterruptedException {
        String propertyPath = testUserPath + "/preferences/testUserPreference";
        assertTestPropertyValue(propertyPath, testPrefPropertyName, testPrefPropertyExpectedValue);
    }

    void createUser() throws ClientException {
        HttpEntity httpEntity = EntityBuilder.create().setParameters()
                .setParameters(
                        new BasicNameValuePair("createUser", ""),
                        new BasicNameValuePair("authorizableId", testUser),
                        new BasicNameValuePair("rep:password", testUser))
                .build();

        adminClient.doPost("/libs/granite/security/post/authorizables", httpEntity, Collections.<Header>emptyList(), 201);
    }

    String getUserPath(final String userId) throws IOException, ClientException, InterruptedException {
        return findUserInfo(userId, true).get("authorizables").get(0).get("home").getValueAsText();
    }

    private void deleteUser(final String userPath) throws ClientException, InterruptedException {
        HttpEntity httpEntity = EntityBuilder.create().setParameters()
                .setParameters(new BasicNameValuePair("deleteAuthorizable", testUser))
                .build();
        new PostExecutor(adminClient, userPath, httpEntity, 200).callAndWait();
    }

    private boolean checkIfUserExist(final String userId) throws ClientException, IOException, InterruptedException {
        return findUserInfo(userId, false).get("authorizables").get(0) != null;
    }

    private JsonNode findUserInfo(final String userId, final boolean expectExist) throws IOException, ClientException, InterruptedException {
        SlingHttpResponse response;
        if (expectExist) {
            Predicate userExistPredicate = new UserExistPredicate();
            new GetExecutor(adminClient,
                    String.format("/bin/security/authorizables.json?filter=%s&limit=1&_charset_=utf-8", userId),
                    userExistPredicate,
                    200)
                    .callAndWait();
            response = userExistPredicate.getLastResponse();
        } else {
            response = adminClient.doGet(String.format("/bin/security/authorizables.json?filter=%s&limit=1&_charset_=utf-8", testUser), 200);
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.getContent());
    }

    private class UserExistPredicate implements Predicate {

        private SlingHttpResponse response;

        public boolean test(SlingHttpResponse response) throws IOException {
            this.response = response;

            return new ObjectMapper().readTree(response.getContent()).get("authorizables").get(0) != null;
        }

        public SlingHttpResponse getLastResponse() {
            return response;
        }
    }
}
