/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

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
	TestAction action = new TestAction("test", null);

	Mockito.when(info.getStatus()).thenReturn(status);

	Mockito.when(status.notExecuted(ctx, info, action)).thenReturn(true);
	Assert.assertTrue(action.isRelevant(ctx, info));

	Mockito.when(status.notExecuted(ctx, info, action)).thenReturn(false);
	Assert.assertFalse(action.isRelevant(ctx, info));
    }

    private static class TestAction extends UpgradeAction {

	public TestAction(String name, Phase phase) {
	    super(name, phase);
	}

	@Override
	public void execute(InstallContext ctx) throws RepositoryException {
	}

    }

}
