package biz.netcentric.vlt.upgrade.handler.userpreferences;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Hashtable;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class UserPreferencesUpgradeActionTest {

    public static final class Constructor {

        @Rule
        public final SlingContext sling = new SlingContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

        @Test
        public void shouldHaveMd5() throws RepositoryException {
            Resource resource = sling.create().resource("/content/resource/path/resourcename");

            UserPreferencesUpgradeAction action = new UserPreferencesUpgradeAction("someUserId",resource);

            assertThat(action.getContentHash()).as("User preferences action should have non-empty md5 hash").isNotEmpty();
        }

        @Test
        public void shouldHaveDifferentMd5() throws RepositoryException {
            Resource resource1 = sling.create().resource("/content/resource/path/resourcename1");
            Resource resource2 = sling.create().resource("/content/resource/path/resourcename2");

            UserPreferencesUpgradeAction action1 = new UserPreferencesUpgradeAction("someUserId", resource1);
            UserPreferencesUpgradeAction action2 = new UserPreferencesUpgradeAction("someUserId", resource2);

            assertThat(action1.getContentHash())
                    .as("Different upgrade actions should have different md5 hashes")
                    .isNotEqualTo(action2.getContentHash());
        }

        @Test
        public void shouldUseUseridInMd5() throws RepositoryException {
            Resource resource = sling.create().resource("/content/resource/path/resourcename");

            UserPreferencesUpgradeAction action1 = new UserPreferencesUpgradeAction("someUserId",resource);
            UserPreferencesUpgradeAction action2 = new UserPreferencesUpgradeAction("anotherUserId",resource);

            assertThat(action1.getContentHash())
                    .as("The same upgrade configurations but for different users should have different md5 hash")
                    .isNotEqualTo(action2.getContentHash());
        }
    }
}