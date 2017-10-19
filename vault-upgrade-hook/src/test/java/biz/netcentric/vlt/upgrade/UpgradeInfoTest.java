/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.mockito.runners.MockitoJUnitRunner;

import biz.netcentric.vlt.upgrade.UpgradeInfo.InstallationMode;
import biz.netcentric.vlt.upgrade.handler.UpgradeActionInfo;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeInfoTest {

    private static final Object TEST_HANDLER = UpgradeInfoTest.class.getName() + "$"
            + TestHandler.class.getSimpleName();

    @Rule
    public final SlingContext sling = new SlingContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private UpgradeStatus status;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InstallContext ctx;

    private Session session;

    @Before
    public void setup() {
        session = sling.resourceResolver().adaptTo(Session.class);
    }

    @Test
    public void testConstructorDefaults() throws Exception {
        try {
            sling.build().resource("/test");
            Node node = session.getNode("/test");
            new UpgradeInfo(ctx, status, node);
            Assert.fail("Exception expected");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        sling.build().resource("/test", //
                UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        Node node = session.getNode("/test");
        Mockito.when(ctx.getPackage().getId().getVersionString()).thenReturn("1.0.0");
        UpgradeInfo info = new UpgradeInfo(ctx, status, node);
        Assert.assertSame(node, info.getNode());
        Assert.assertSame(status, info.getStatus());
        Assert.assertEquals(UpgradeInfo.DEFAULT_INSTALLATION_MODE, info.getInstallationMode().toString());
        Assert.assertEquals(UpgradeInfo.DEFAULT_PHASE, info.getDefaultPhase().toString());
        Assert.assertTrue(info.getHandler() instanceof TestHandler);
        Assert.assertEquals(Phase.values().length, info.getActions().size());
        UpgradeAction action = info.getActions().get(TestHandler.PHASE).get(0);
        Assert.assertTrue(action instanceof UpgradeAction);
        Assert.assertTrue(new MockUtil().isMock(action));
    }

    @Test
    public void testConstructor() throws Exception {
        sling.build().resource("/test", //
                UpgradeInfo.PN_INSTALLATION_MODE, "always", //
                UpgradeInfo.PN_DEFAULT_PHASE, "prepare", //
                UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        Node node = session.getNode("/test");
        UpgradeInfo info = new UpgradeInfo(ctx, status, node);

        Assert.assertEquals(InstallationMode.ALWAYS, info.getInstallationMode());
        Assert.assertEquals(Phase.PREPARE, info.getDefaultPhase());
        Assert.assertTrue(info.getHandler() instanceof TestHandler);
    }

    public static class TestHandler implements UpgradeHandler {

        private static final Phase PHASE = Phase.INSTALL_FAILED;

        @Override
        public boolean isAvailable(InstallContext ctx) {
            return true;
        }

        @Override
        public Iterable<UpgradeAction> create(InstallContext ctx, UpgradeActionInfo info) {
            UpgradeAction action = Mockito.mock(UpgradeAction.class);
            Mockito.when(action.getPhase()).thenReturn(PHASE);
            return Arrays.asList(action);
        }

    }

}
