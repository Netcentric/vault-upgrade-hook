/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler.groovy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.packaging.impl.InstallContextImpl;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.icfolson.aem.groovy.console.GroovyConsoleService;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.handler.OsgiUtil;
import biz.netcentric.vlt.upgrade.handler.UpgradeActionInfo;
import biz.netcentric.vlt.upgrade.testUtils.LoggerStub;

@RunWith(Enclosed.class)
public class GroovyConsoleHandlerTest {

    public static final class IsAvailable extends GroovyConsoleHandlerAbstractTest {


        @Test
        public void shouldReturnTrueIfGroovyConsoleInstalled() throws RepositoryException {
            Mockito.doReturn(true).when(osgi).hasService(GroovyConsoleService.class);

            boolean result = groovyConsoleHandler.isAvailable(installContext);
            assertThat(result).as("Should return true if GroovyConsoleService is available").isTrue();
        }

        @Test
        public void shouldReturnFalseIfGroovyConsoleNotInstalled() throws RepositoryException {
            Mockito.doReturn(false).when(osgi).hasService(GroovyConsoleService.class);

            boolean result = groovyConsoleHandler.isAvailable(installContext);
            assertThat(result).as("Should return false if GroovyConsoleService is not available").isFalse();
        }
    }

    public static final class Create extends GroovyConsoleHandlerAbstractTest {

        @Test
        public void shouldReturnGroovyScriptActions() throws RepositoryException {
            List<UpgradeAction> upgradeActions = groovyConsoleHandler.create(installContext, new UpgradeActionInfo(node, null));
            assertThat(upgradeActions).hasSize(2);
        }

        @Test
        public void shouldReturnOnlyGroovyScripts() throws RepositoryException {
            List<UpgradeAction> upgradeActions = groovyConsoleHandler.create(installContext, new UpgradeActionInfo(node, null));

            for (UpgradeAction action : upgradeActions) {
                assertThat(action.getName()).endsWith(".groovy");
            }
        }
    }

    public static abstract class GroovyConsoleHandlerAbstractTest {

        @Rule
        public final SlingContext sling = new SlingContext(ResourceResolverType.JCR_MOCK);

        GroovyConsoleHandler groovyConsoleHandler;
        OsgiUtil osgi;
        InstallContextImpl installContext;
        Node node;

        @Before
        public void setUp() throws RepositoryException, UnsupportedEncodingException {
            groovyConsoleHandler = new GroovyConsoleHandler();
            GroovyConsoleHandler.LOG = new LoggerStub();

            osgi = Mockito.spy(new OsgiUtil());
            groovyConsoleHandler.osgi = osgi;

            sling.build()
                    .resource("/some/resource")
                    .siblingsMode()
                    .file("script1.groovy", new ByteArrayInputStream("testContent1".getBytes(StandardCharsets.UTF_8.name())), "application/octet-stream", new Date().getTime())
                    .file("script2.groovy", new ByteArrayInputStream("testContent2".getBytes(StandardCharsets.UTF_8.name())), "application/octet-stream", new Date().getTime())
                    .file("script3.java", new ByteArrayInputStream("Not a groovy script".getBytes(StandardCharsets.UTF_8.name())), "application/octet-stream", new Date().getTime())
                    .resource("notAFileNode.groovy")
                    .commit();

            node = sling.resourceResolver().getResource("/some/resource").adaptTo(Node.class);
            installContext = new InstallContextImpl(node, null, null, null);
        }
    }
}
