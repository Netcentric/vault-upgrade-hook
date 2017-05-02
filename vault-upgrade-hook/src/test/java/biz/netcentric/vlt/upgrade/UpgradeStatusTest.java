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

import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeStatusTest {

    @Rule
    public final SlingContext sling = new SlingContext(ResourceResolverType.JCR_MOCK);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InstallContext ctx;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UpgradeInfo info;

    @Mock
    private UpgradeAction action;

    private Session session;

    private UpgradeStatus status;

    @Before
    public void setup() throws Exception {
	session = sling.resourceResolver().adaptTo(Session.class);
	Mockito.when(ctx.getSession()).thenReturn(session);

	sling.build().resource("/test", JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_FILE, //
		UpgradeStatus.PN_VERSION, "1");
	status = new UpgradeStatus(ctx, "/test");
    }

    @Test
    public void testConstructor() throws Exception {
	Assert.assertFalse(status.isInitial());
	Assert.assertEquals("1", status.getLastExecution().toString());

	status = new UpgradeStatus(ctx, "/create-test");
	Assert.assertTrue(session.nodeExists("/create-test"));
	Assert.assertTrue(status.isInitial());
    }

    @Test
    public void testNotExecuted() throws Exception {
	Mockito.when(info.getNode().getName()).thenReturn("testInfo");
	Assert.assertTrue(status.notExecuted(ctx, info, action));

	sling.build().resource("/test/testInfo");
	Assert.assertTrue(status.notExecuted(ctx, info, action));

	sling.build().resource("/test/testInfo", UpgradeStatus.PN_ACTIONS, new String[] { "test1", "test2" });
	Assert.assertTrue(status.notExecuted(ctx, info, action));

	Mockito.when(action.getName()).thenReturn("test1");
	Assert.assertFalse(status.notExecuted(ctx, info, action));
    }

    @Test
    public void testUpdate() throws Exception {
	Mockito.when(ctx.getPackage().getId().getVersionString()).thenReturn("2.1.0");
	status.update(ctx);
	Assert.assertEquals("2.1.0",
		JcrUtils.getStringProperty(session, "/test/" + UpgradeStatus.PN_VERSION, "failed"));
	Assert.assertNotNull(JcrUtils.getDateProperty(session, "/test/" + UpgradeStatus.PN_UPGRADE_TIME, null));
    }

    @Test
    public void testUpdateAction() throws Exception {
	Mockito.when(info.getExecutedActions()).thenReturn(Arrays.asList("test1", "test2"));
	Mockito.when(info.getNode().getName()).thenReturn("testInfo");
	status.updateActions(ctx, info);
	Assert.assertArrayEquals(new String[] { "test1", "test2" },
		toStringArray(session.getProperty("/test/testInfo/" + UpgradeStatus.PN_ACTIONS).getValues()));
    }

    private String[] toStringArray(Value[] values) throws Exception {
	String[] strings = new String[values.length];
	for (int i = 0; i < values.length; i++) {
	    strings[i] = values[i].getString();
	}
	return strings;
    }

}
