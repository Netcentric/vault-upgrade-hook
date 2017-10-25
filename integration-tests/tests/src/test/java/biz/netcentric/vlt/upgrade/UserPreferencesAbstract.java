package biz.netcentric.vlt.upgrade;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UserPreferencesAbstract {

    static String testUser;
    static String packageVersion;
    static String packageGroup;
    static String alwaysPackageName;
    static String testPropertyName;
    static String testPropertyValue;

    Logger LOG = LoggerFactory.getLogger(this.getClass());

    private String packageGroupAndName = String.format("%s/%s", packageGroup, alwaysPackageName);
    String packageRelPath = String.format("%s-%s.zip", packageGroupAndName, packageVersion);

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    SlingClient adminClient;

    @BeforeClass
    public static void setUpClass() {
        testUser = System.getProperty("vaultUpgradeHook.testpackage.userconfig.testUser");
        packageVersion = System.getProperty("vaultUpgradeHook.testpackage.version");
        packageGroup = System.getProperty("vaultUpgradeHook.testpackage.group");
        alwaysPackageName = System.getProperty("vaultUpgradeHook.testpackage.userconfig.always");
        testPropertyName = System.getProperty("vaultUpgradeHook.testpackage.userconfig.testProperty");
        testPropertyValue = System.getProperty("vaultUpgradeHook.testpackage.userconfig.testPropertyValue");
    }

    @Before
    public void setUp() throws ClientException, IOException {
        adminClient = slingInstanceRule.getAdminClient();
        if (adminClient != null && checkIfUserExist()) {

            LOG.warn("### WARNING ### test user 'test-vault-hook' already exists." +
                    "This may be the result of failed test or test without clean up");
            deleteUser(getUserPath(testUser));
        }

        createUser();
        installPackage(packageRelPath);
    }

    @After
    public void tearDown() throws ClientException, IOException {
        if (adminClient != null && checkIfUserExist()) {
            deleteUser(getUserPath(testUser));
        } else {
            LOG.warn("### WARNING ### adminClient is null, so I can not clean up repo after the test.");
        }
    }

    void assertTestPropertyValue(String testUserPath, String testPrefPropertyName, String testPrefPropertyExpectedValue)
            throws ClientException, InterruptedException {
        JsonNode testProperty = adminClient.getJsonNode(testUserPath + "/preferences/testUserPreference", 1);

        assertThat(testProperty).as("Test preference property should exist").isNotNull();
        assertThat(testProperty.get(testPrefPropertyName).getTextValue()).isEqualTo(
                testPrefPropertyExpectedValue);
    }

    SlingHttpResponse installPackage(String packageRelPath) throws ClientException {
        return adminClient.doPost("/crx/packmgr/service/.json/etc/packages/" + packageRelPath + "?cmd=install", null, 200);
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

    String getUserPath(final String userId) throws IOException, ClientException {
        JsonNode userInfo = findUserInfo();
        return userInfo.get("authorizables").get(0).get("home").getValueAsText();
    }

    private void deleteUser(final String userPath) throws ClientException {
        HttpEntity httpEntity = EntityBuilder.create().setParameters()
                .setParameters(new BasicNameValuePair("deleteAuthorizable", ""))
                .build();
        adminClient.doPost(userPath, httpEntity, 200);
    }

    private boolean checkIfUserExist() throws ClientException, IOException {
        return findUserInfo().get("authorizables").get(0) != null;
    }

    private JsonNode findUserInfo() throws IOException, ClientException {
        SlingHttpResponse slingHttpResponse = adminClient.doGet(String.format("/bin/security/authorizables.json?filter=%s&limit=1&_charset_=utf-8", testUser), 200);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(slingHttpResponse.getContent());
    }
}
