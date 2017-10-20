package biz.netcentric.vlt.upgrade.handler.slingpipes;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class SlingPipeTest {

    public static final class Constructor {

        @Rule
        public final SlingContext sling = new SlingContext(ResourceResolverType.JCR_MOCK);

        @Test
        public void shouldHaveMd5() {
            Resource resource = sling.create().resource("/content/resource/path/resourcename");

            SlingPipe slingPipe = new SlingPipe(resource, InstallContext.Phase.PREPARE);

            assertThat(slingPipe.getContentHash()).as("Pipe should have non-empty md5 hash").isNotEmpty();
        }

        @Test
        public void shouldHaveDifferentMd5() {
            Resource resource1 = sling.create().resource("/content/resource/path/resourcename1");
            Resource resource2 = sling.create().resource("/content/resource/path/resourcename2");

            SlingPipe slingPipe1 = new SlingPipe(resource1, InstallContext.Phase.PREPARE);
            SlingPipe slingPipe2 = new SlingPipe(resource2, InstallContext.Phase.PREPARE);

            assertThat(slingPipe1.getContentHash())
                    .as("Different pipes should have different md5 hashes")
                    .isNotEqualTo(slingPipe2.getContentHash());
        }
    }
}