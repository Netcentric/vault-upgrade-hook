/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.http.HttpEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIT {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAILED_STATUS = "FAILED";

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    private static String packageVersion;
    private static String packageGroup;

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected SlingClient adminClient;

    protected String packageRelPath;

    @BeforeClass
    public static void setUpClass() {
        packageVersion = System.getProperty("vaultUpgradeHook.testpackage.version");
        packageGroup = System.getProperty("vaultUpgradeHook.testpackage.group");
    }

    @Before
    public void setUp() throws Exception {
        adminClient = slingInstanceRule.getAdminClient();
        packageRelPath = String.format("%s-%s.zip", getPackageGroupAndName(), packageVersion);
    }

    protected SlingHttpResponse installPackage(String packageRelPath) throws ClientException {
        return adminClient.doPost("/crx/packmgr/service/.json/etc/packages/" + packageRelPath + "?cmd=install", null, 200);
    }

    protected void assertTestPropertyValue(String propertyPath, String testPropertyName, String testPropertyExpectedValue)
            throws ClientException, InterruptedException {
        assertTestArrayPropertyValue(propertyPath, testPropertyName, testPropertyExpectedValue, -1);
    }

    protected void assertTestArrayPropertyValue(String propertyPath, String testPropertyName,
                                                String testPropertyExpectedValue, int index) throws InterruptedException, ClientException {
        new GetExecutor(adminClient, propertyPath + ".json", null, 200).callAndWait();
        JsonNode testProperty = adminClient.getJsonNode(propertyPath, 1);

        JsonNode propertyJsonNode = testProperty.get(testPropertyName);
        if (index > -1) {
            propertyJsonNode = propertyJsonNode.get(index);
        }
        assertThat(propertyJsonNode)
                .as("Property '%s' should exist under path '%s'", testPropertyName, propertyPath)
                .isNotNull();
        assertThat(propertyJsonNode.getTextValue()).isEqualTo(
                testPropertyExpectedValue);
    }

    protected void assertSuccessStatus() throws InterruptedException {
        boolean isSuccessful = new GetExecutor(adminClient, getUpgraderMetadataPath() + ".json",
                new ExecutionStatusCorrect(SUCCESS_STATUS), 200).callAndWait();
        assertThat(isSuccessful).as("Status should be 'SUCCESS' after upgrader execution").isTrue();
    }

    protected void assertFailedStatus() throws InterruptedException {
        boolean isSuccessful = new GetExecutor(adminClient, getUpgraderMetadataPath() + ".json",
                new ExecutionStatusCorrect(FAILED_STATUS), 200).callAndWait();
        assertThat(isSuccessful).as("Status should be 'FAILED' after upgrader execution").isTrue();
    }

    protected String getUpgraderMetadataPath() {
        return "/var/upgrade/" + getPackageGroupAndName();
    }

    protected String getPackageGroupAndName() {
        return String.format("%s/%s", packageGroup, getPackageName());
    }

    protected boolean checkIfResourceExist(String resourcePath, boolean expectExist) throws ClientException, InterruptedException {
        if (expectExist) {
            return new GetExecutor(adminClient, resourcePath + ".json", 200).callAndWait();
        } else {
            return adminClient.exists(resourcePath);
        }
    }

    protected void deleteResource(String resourcePath) throws InterruptedException {
        HttpEntity entity = FormEntityBuilder.create().addParameter(":operation", "delete").build();
        new PostExecutor(adminClient, resourcePath, entity, 200).callAndWait();
    }

    protected void createResource(String resourcePath) throws ClientException, InterruptedException {
        adminClient.createNode(resourcePath, "nt:unstructured");
        new GetExecutor(adminClient, resourcePath + ".json", 200).callAndWait();
    }

    protected class ExecutionStatusCorrect implements Predicate {

        private SlingHttpResponse response;
        private String expectedStatus;

        public ExecutionStatusCorrect(String expectedStatus) {
            this.expectedStatus = expectedStatus;
        }

        public boolean test(SlingHttpResponse response) throws Exception {
            this.response = response;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getContent());
            return expectedStatus.equals(jsonNode.get("status").getValueAsText());
        }

        public SlingHttpResponse getLastResponse() {
            return response;
        }
    }

    protected void cleanUpBefore() throws ClientException, InterruptedException {
        if (adminClient != null) {

            String testResourcePath = getTestResourcePath();
            if (checkIfResourceExist(testResourcePath, false)) {
                LOG.warn("### WARNING ### test resource '{}' already exists. This may be the result of failed test" +
                        " or test without clean up", testResourcePath);
                deleteResource(testResourcePath);
            }

            String upgraderMetadataPath = getUpgraderMetadataPath();
            if (checkIfResourceExist(upgraderMetadataPath, false)) {
                LOG.warn("### WARNING ### Upgrader metadata '{}' exists. This may be the result of failed test" +
                        " or test without clean up", upgraderMetadataPath);
                deleteResource(upgraderMetadataPath);
            }
        }
    }

    protected void cleanUpAfter() throws ClientException, InterruptedException {
        if (adminClient != null) {
            if(checkIfResourceExist(getTestResourcePath(), true)) {
                deleteResource(getTestResourcePath());
            } else {
                LOG.warn("### WARNING ### Test resource does not exist after the test.");
            }
            String upgraderMetadataPath = getUpgraderMetadataPath();
            if(checkIfResourceExist(upgraderMetadataPath, true)) {
                deleteResource(upgraderMetadataPath);
            } else {
                LOG.warn("### WARNING ### Upgrader metadata does not exist after the test.");
            }
        } else {
            LOG.warn("### WARNING ### adminClient is null, so I can not clean up repo after the test.");
        }
    }

    protected abstract String getPackageName();

    /**
     * Should return path of test resource.
     * Default implementation.
     * @return null
     */
    protected String getTestResourcePath() {
        return null;
    }
}
