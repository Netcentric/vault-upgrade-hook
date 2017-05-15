/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import java.nio.charset.Charset;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeActionTest {

    @Mock
    private InstallContext ctx;

    @Mock
    private UpgradeInfo info;

    @Mock
    private UpgradeStatus status;

    @Test
    public void testIsRelevant() throws Exception {
        TestAction action = new TestAction("testName", null, "testHash");

        Mockito.when(info.getStatus()).thenReturn(status);

        Mockito.when(status.isExecuted(ctx, info, "testName_testHash")).thenReturn(false);
        Assert.assertTrue(action.isRelevant(ctx, info));

        Mockito.when(status.isExecuted(ctx, info, "testName_testHash")).thenReturn(true);
        Assert.assertFalse(action.isRelevant(ctx, info));
    }

    @Test
    public void testGetMd5() throws Exception {
        Assert.assertEquals("CY9rzUYh03PK3k6DJie09g==", TestAction.getMd5("test", Charset.defaultCharset().name()));
    }


    private static class TestAction extends UpgradeAction {

        public TestAction(String name, Phase phase, String hash) {
            super(name, phase, hash);
        }

        @Override
        public void execute(InstallContext ctx) throws RepositoryException {
        }

    }

}
