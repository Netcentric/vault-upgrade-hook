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

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.VaultPackage;
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

import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeProcessorTest {

    @Rule
    public final SlingContext sling = new SlingContext(ResourceResolverType.JCR_MOCK);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InstallContext ctx;

    @Mock
    private VaultPackage vaultPackage;

    @Mock
    private UpgradeInfo gorup;

    @Mock
    private static UpgradeAction action;

    @Mock
    private static UpgradeStatus status;

    private UpgradeProcessor processor;

    @Before
    public void setup() {
	Mockito.reset(action);
	processor = new UpgradeProcessor();
	Mockito.when(ctx.getOptions()).thenReturn(Mockito.mock(ImportOptions.class));
	Mockito.when(ctx.getSession()).thenReturn(sling.resourceResolver().adaptTo(Session.class));
    }

    @Test
    public void testExecutePrepare() throws Exception {
	Mockito.when(ctx.getPhase()).thenReturn(Phase.PREPARE);
	Mockito.when(ctx.getPackage().getId().getName()).thenReturn("testVaultPackage");
	Mockito.when(ctx.getPackage().getId().getGroup()).thenReturn("testVaultGroup");
	Mockito.when(ctx.getPackage().getId().getInstallationPath()).thenReturn("/test/installation/path");
	Mockito.when(ctx.getPackage().getId().getVersionString()).thenReturn("1.0.1-SNAPSHOT");

	sling.load().json("/biz/netcentric/vlt/upgrade/testStatus.json",
		"/var/upgrade/testVaultGroup/testVaultPackage");
	sling.load().json("/biz/netcentric/vlt/upgrade/testUpgrade.json",
		"/test/installation/path.zip/jcr:content/vlt:definition/upgrader");

	Mockito.when(action.getName()).thenReturn("testAction");
	Mockito.when(action.getPhase()).thenReturn(Phase.PREPARE);
	Mockito.when(action.isRelevant(Mockito.eq(ctx), Mockito.any(UpgradeInfo.class))).thenReturn(true);

	processor.execute(ctx);

	Assert.assertEquals("1.0.0", processor.status.getLastExecution().toString());
	Assert.assertEquals(1, processor.infos.size());
	Assert.assertEquals("1.0.1-SNAPSHOT", processor.infos.get(0).getTargetVersion().toString());
	Assert.assertTrue(processor.infos.get(0).isRelevant());
	Mockito.verify(action).execute(ctx);
    }

    @Test
    public void testExecuteInstalled() throws Exception {
	processor.infos = Arrays.asList(gorup);

	Mockito.when(ctx.getPhase()).thenReturn(Phase.INSTALLED);

	processor.execute(ctx);

	Mockito.verify(gorup).execute(ctx);
    }

    @Test
    public void testExecuteEnd() throws Exception {
	processor.status = status;
	processor.infos = Arrays.asList(gorup);

	Mockito.when(ctx.getPhase()).thenReturn(Phase.END);

	JcrUtils.getOrCreateByPath("/test", JcrConstants.NT_UNSTRUCTURED, ctx.getSession());
	Assert.assertTrue("Make sure the session has changed to verify save after execution.",
		ctx.getSession().hasPendingChanges());

	processor.execute(ctx);

	Mockito.verify(gorup).execute(ctx);
	Assert.assertFalse(ctx.getSession().hasPendingChanges());
	Mockito.verify(status).update(ctx);
	Mockito.verify(status).updateActions(ctx, gorup);
    }

    @Test
    public void testExecuteFailed() throws Exception {
	processor.status = status;
	processor.infos = Arrays.asList(gorup);

	Mockito.when(ctx.getPhase()).thenReturn(Phase.PREPARE_FAILED);
	processor.execute(ctx);
	Mockito.verify(gorup).execute(ctx);

	Mockito.reset(gorup);

	Mockito.when(ctx.getPhase()).thenReturn(Phase.INSTALL_FAILED);
	processor.execute(ctx);
	Mockito.verify(gorup).execute(ctx);
    }

    @Test(expected = PackageException.class)
    public void testExecuteException() throws Exception {
	processor.infos = Arrays.asList(gorup);

	Mockito.when(ctx.getPhase()).thenReturn(Phase.INSTALLED);
	Mockito.doThrow(new IllegalArgumentException("testException")).when(gorup).execute(ctx);

	processor.execute(ctx);
    }

    public static class TestHandler implements UpgradeHandler {

	@Override
	public boolean isAvailable() {
	    return true;
	}

	@Override
	public Iterable<UpgradeAction> create(UpgradeInfo info) {
	    return Arrays.asList(action);
	}

    }

}
