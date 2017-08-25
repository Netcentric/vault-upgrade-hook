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
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
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

import biz.netcentric.vlt.upgrade.UpgradeInfo.RunMode;
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
    private UpgradeInfo info;

    @Mock
    private UpgradeAction action;

    @Mock
    private UpgradeStatus status;

    private UpgradeProcessor processor;

    @Before
    public void setup() throws Exception {
        processor = new UpgradeProcessor();
        Mockito.when(ctx.getOptions()).thenReturn(Mockito.mock(ImportOptions.class));
        Mockito.when(ctx.getSession()).thenReturn(sling.resourceResolver().adaptTo(Session.class));
        Mockito.when(status.getNode()).thenReturn(Mockito.mock(Node.class));
        Mockito.when(info.getNode()).thenReturn(Mockito.mock(Node.class));
        Mockito.when(info.getHandler()).thenReturn(Mockito.mock(UpgradeHandler.class));
        Mockito.when(info.getExecutedActions()).thenReturn(Collections.<UpgradeAction>emptySet());
    }

    @Test
    public void testExecutePrepare() throws Exception {
        Mockito.when(ctx.getPhase()).thenReturn(Phase.PREPARE);
        Mockito.when(ctx.getPackage().getId().getName()).thenReturn("testVaultPackage");
        Mockito.when(ctx.getPackage().getId().getGroup()).thenReturn("testVaultGroup");
        Mockito.when(ctx.getPackage().getId().getInstallationPath()).thenReturn("/test/installation/path");
        Mockito.when(ctx.getPackage().getId().getVersionString()).thenReturn("1.0.1-SNAPSHOT");

        sling.build().resource("/test/installation/path.zip/jcr:content/vlt:definition/upgrader/test", //
                "handler", "biz.netcentric.vlt.upgrade.UpgradeProcessorTest$TestHandler" //
        );

        processor.execute(ctx);

        Assert.assertEquals(1, processor.infos.size());
        Assert.assertTrue(processor.infos.get(0).getHandler() instanceof TestHandler);
        TestHandler handler = (TestHandler) processor.infos.get(0).getHandler();
        Mockito.verify(handler.action).execute(ctx);
    }

    @Test
    public void testExecuteInstalled() throws Exception {
        Mockito.when(ctx.getPhase()).thenReturn(Phase.INSTALLED);

        processor.infos = Arrays.asList(info);
        Mockito.when(info.getActions()).thenReturn(Collections.singletonMap(Phase.INSTALLED, Arrays.asList(action)));

        Mockito.when(info.getRunMode()).thenReturn(RunMode.ALWAYS);
        processor.execute(ctx);
        Mockito.verify(action).execute(ctx);

        Mockito.reset(action);
        Mockito.when(info.getRunMode()).thenReturn(RunMode.ON_CHANGE);
        processor.execute(ctx);
        Mockito.verify(action, Mockito.never()).execute(ctx);

        Mockito.reset(action);
        Mockito.when(action.isRelevant(ctx, info)).thenReturn(true);
        processor.execute(ctx);
        Mockito.verify(action).execute(ctx);
    }

    @Test
    public void testExecuteEnd() throws Exception {
        processor.status = status;
        processor.infos = Arrays.asList(info);

        Mockito.when(action.isRelevant(ctx, info)).thenReturn(true);

        Mockito.when(ctx.getPhase()).thenReturn(Phase.END);
        Mockito.when(info.getActions()).thenReturn(Collections.singletonMap(Phase.END, Arrays.asList(action)));

        JcrUtils.getOrCreateByPath("/test", JcrConstants.NT_UNSTRUCTURED, ctx.getSession());
        Assert.assertTrue("Make sure the session has changed to verify save after execution.",
                ctx.getSession().hasPendingChanges());

        processor.execute(ctx);

        Assert.assertFalse(ctx.getSession().hasPendingChanges());
        Mockito.verify(info).executed(action);
        Mockito.verify(action).execute(ctx);
        Mockito.verify(status).update(ctx, false);
        Mockito.verify(status).update(ctx, info);
    }

    @Test(expected = PackageException.class)
    public void testException() throws Exception {
        processor.infos = Arrays.asList(info);

        Mockito.when(ctx.getPhase()).thenReturn(Phase.INSTALLED);
        Mockito.doThrow(new IllegalArgumentException("testException")).when(info).getActions();

        processor.execute(ctx);
    }

    @Test(expected = PackageException.class)
    public void testActionException() throws Exception {
        processor.infos = Arrays.asList(info);

        Mockito.when(ctx.getPhase()).thenReturn(Phase.INSTALLED);
        Mockito.when(info.getActions()).thenReturn(Collections.singletonMap(Phase.INSTALLED, Arrays.asList(action)));
        Mockito.when(action.isRelevant(ctx, info)).thenReturn(true);

        Mockito.doThrow(new RuntimeException("testException")).when(action).execute(ctx);

        try {
            processor.execute(ctx);
        } catch (Exception e) {
            Assert.assertTrue(info.getExecutedActions().isEmpty());
            throw e;
        }
    }

    public static class TestHandler implements UpgradeHandler {

        private final UpgradeAction action;

        public TestHandler() throws RepositoryException {
            action = Mockito.mock(UpgradeAction.class);
            Mockito.when(action.getName()).thenReturn("testAction");
            Mockito.when(action.getPhase()).thenReturn(Phase.PREPARE);
            Mockito.when(action.isRelevant(Mockito.any(InstallContext.class), Mockito.any(UpgradeInfo.class)))
                    .thenReturn(true);
        }

        @Override
        public boolean isAvailable(InstallContext ctx) {
            return true;
        }

        @Override
        public Iterable<UpgradeAction> create(InstallContext ctx, UpgradeInfo info) {
            return Arrays.asList(action);
        }

    }

}
