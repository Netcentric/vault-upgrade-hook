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
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.jackrabbit.vault.packaging.Version;
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

import biz.netcentric.vlt.upgrade.UpgradeInfo.RunMode;
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
	    new UpgradeInfo(status, node, "1.0.0");
	    Assert.fail("Exception expected");
	} catch (IllegalArgumentException e) {
	    Assert.assertNotNull(e);
	}

	sling.build().resource("/test", //
		UpgradeInfo.PN_HANDLER, TEST_HANDLER);
	Node node = session.getNode("/test");
	UpgradeInfo info = new UpgradeInfo(status, node, "1.0.0");
        Assert.assertSame(node, info.getNode());
        Assert.assertSame(status, info.getStatus());
        Assert.assertEquals(UpgradeInfo.DEFAULT_PRIORITY, info.getPriority());
        Assert.assertEquals(UpgradeInfo.DEFAULT_SAVE_THRESHOLD, info.getSaveThreshold());
        Assert.assertEquals("1.0.0", info.getTargetVersion().toString());
        Assert.assertEquals(UpgradeInfo.DEFAULT_RUN_MODE, info.getRunMode().toString());
        Assert.assertEquals(UpgradeInfo.DEFAULT_SKIP_ON_INITIAL, info.isSkipOnInitial());
        Assert.assertEquals(UpgradeInfo.DEFAULT_PHASE, info.getDefaultPhase().toString());
	Assert.assertTrue(info.getHandler() instanceof TestHandler);
        Assert.assertEquals(Phase.values().length, info.getActions().size());
	UpgradeAction action = info.getActions().get(TestHandler.PHASE).get(0);
	Assert.assertTrue(action instanceof UpgradeAction);
	Assert.assertTrue(new MockUtil().isMock(action));
        Assert.assertEquals(0, info.getCounter());
    }

    @Test
    public void testConstructor() throws Exception {
        sling.build().resource("/test", //
                UpgradeInfo.PN_PRIORITY, 47l, //
                UpgradeInfo.PN_SAVE_THRESHOLD, 48l, //
                UpgradeInfo.PN_TARGET_VERSION, "2.0.0", //
                UpgradeInfo.PN_RUN_MODE, "always", //
                UpgradeInfo.PN_SKIP_ON_INITIAL, !UpgradeInfo.DEFAULT_SKIP_ON_INITIAL, //
                UpgradeInfo.PN_DEFAULT_PHASE, "prepare", //
		UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        Node node = session.getNode("/test");
        UpgradeInfo info = new UpgradeInfo(status, node, "1.0.0");

        Assert.assertEquals(47l, info.getPriority());
        Assert.assertEquals(48l, info.getSaveThreshold());
        Assert.assertEquals("2.0.0", info.getTargetVersion().toString());
        Assert.assertEquals(RunMode.ALWAYS, info.getRunMode());
        Assert.assertEquals(!UpgradeInfo.DEFAULT_SKIP_ON_INITIAL, info.isSkipOnInitial());
        Assert.assertEquals(Phase.PREPARE, info.getDefaultPhase());
        Assert.assertTrue(info.getHandler() instanceof TestHandler);
    }

    @Test
    public void testExecute() throws Exception {
        Mockito.when(ctx.getPhase()).thenReturn(Phase.INSTALLED);

	sling.build().resource("/testIncremental", //
		UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        UpgradeInfo info = new UpgradeInfo(status, session.getNode("/testIncremental"), "0");
        Assert.assertEquals(RunMode.INCREMENTAL, info.getRunMode());

        UpgradeAction action1 = Mockito.mock(UpgradeAction.class);
        Mockito.when(action1.isRelevant(ctx, info)).thenReturn(false);
        info.getActions().get(Phase.INSTALLED).add(action1);

        UpgradeAction action2 = Mockito.mock(UpgradeAction.class);
        Mockito.when(action2.isRelevant(ctx, info)).thenReturn(true);
        info.getActions().get(Phase.INSTALLED).add(action2);

        UpgradeAction action3 = Mockito.mock(UpgradeAction.class);
        Mockito.when(action3.isRelevant(ctx, info)).thenReturn(false);
        info.getActions().get(Phase.INSTALLED).add(action3);

        info.execute(ctx);

        Mockito.verify(action1, Mockito.never()).execute(ctx);
        Mockito.verify(action2).execute(ctx);
        Mockito.verify(action3).execute(ctx);

        Mockito.reset(action1, action2, action3);
	sling.build().resource("/testAlways", //
		UpgradeInfo.PN_RUN_MODE, RunMode.ALWAYS.toString(), //
		UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        info = new UpgradeInfo(status, session.getNode("/testAlways"), "0");
        Assert.assertEquals(RunMode.ALWAYS, info.getRunMode());
        info.getActions().get(Phase.INSTALLED).add(action1);
        info.getActions().get(Phase.INSTALLED).add(action2);
        info.getActions().get(Phase.INSTALLED).add(action3);

        info.execute(ctx);

        Mockito.verify(action1).execute(ctx);
        Mockito.verify(action2).execute(ctx);
        Mockito.verify(action3).execute(ctx);
    }

    @Test
    public void testIsRelevant() throws Exception {
	sling.build().resource("/testAlways", //
		UpgradeInfo.PN_RUN_MODE, RunMode.ALWAYS.toString(), //
		UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        UpgradeInfo info = new UpgradeInfo(status, session.getNode("/testAlways"), "0");
        Assert.assertEquals(RunMode.ALWAYS, info.getRunMode());

	Assert.assertTrue(info.isRelevant(ctx));

	sling.build().resource("/test", //
		UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        info = new UpgradeInfo(status, session.getNode("/test"), "1");
	Mockito.when(status.getLastExecution(ctx, info)).thenReturn(Version.create("0"));

	Assert.assertTrue(info.isRelevant(ctx));

        Mockito.when(status.isInitial()).thenReturn(true);
	Assert.assertFalse(info.isRelevant(ctx));
        Mockito.when(status.isInitial()).thenReturn(false);

	Mockito.when(status.getLastExecution(ctx, info)).thenReturn(Version.create("1"));
	Assert.assertTrue(info.isRelevant(ctx));

	Mockito.when(status.getLastExecution(ctx, info)).thenReturn(Version.create("2"));
	Assert.assertFalse(info.isRelevant(ctx));

	sling.build().resource("/testNotSkipOnInitial", //
		UpgradeInfo.PN_SKIP_ON_INITIAL, false, //
		UpgradeInfo.PN_HANDLER, TEST_HANDLER);
        info = new UpgradeInfo(status, session.getNode("/testNotSkipOnInitial"), "1");
	Mockito.when(status.getLastExecution(ctx, info)).thenReturn(Version.create("0"));

	Assert.assertTrue(info.isRelevant(ctx));

        Mockito.when(status.isInitial()).thenReturn(true);
	Assert.assertTrue(info.isRelevant(ctx));

	Mockito.when(status.getLastExecution(ctx, info)).thenReturn(Version.create("2"));
	Assert.assertTrue(info.isRelevant(ctx));
    }

    public static class TestHandler implements UpgradeHandler {

	private static final Phase PHASE = Phase.INSTALL_FAILED;

	@Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public Iterable<UpgradeAction> create(UpgradeInfo info) throws RepositoryException {
	    UpgradeAction action = Mockito.mock(UpgradeAction.class);
	    Mockito.when(action.getPhase()).thenReturn(PHASE);
	    return Arrays.asList(action);
        }

    }

}
