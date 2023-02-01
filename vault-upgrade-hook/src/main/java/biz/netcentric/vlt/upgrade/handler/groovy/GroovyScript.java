/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler.groovy;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import be.orbinson.aem.groovy.console.GroovyConsoleService;
import be.orbinson.aem.groovy.console.api.context.ScriptContext;
import be.orbinson.aem.groovy.console.response.RunScriptResponse;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;


import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.handler.OsgiUtil.ServiceWrapper;
import biz.netcentric.vlt.upgrade.handler.SlingUtils;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class GroovyScript extends UpgradeAction {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(GroovyScript.class);

    SlingUtils sling = new SlingUtils();
    private final Node script;

    public GroovyScript(final Node script, final Phase defaultPhase) throws RepositoryException {
        super(script.getName(), UpgradeAction.getPhaseFromPrefix(defaultPhase, script.getName()), getDataMd5(script));
        this.script = script;
    }

    @Override
    public void execute(final InstallContext ctx) throws RepositoryException {
        LOG.debug(ctx, "Executing [{}]", getName());
        final RunScriptResponse scriptResponse = run();
        LOG.debug(ctx, "Executed script [{}]: [{}]\n{}\n---\n", getName(), scriptResponse.getRunningTime(),
                scriptResponse.getExceptionStackTrace());
        if (scriptResponse.getExceptionStackTrace() != null && scriptResponse.getExceptionStackTrace().trim().length() > 0) {
            throw new RuntimeException(
                    "Error executing script " + getName() + "\n" + scriptResponse.getExceptionStackTrace());
        } else {
            LOG.info(ctx, "Executed [{}]: [{}]\n{}\n---\n", getName(), scriptResponse.getRunningTime(),
                    scriptResponse.getOutput().trim());
        }
    }

    protected RunScriptResponse run() throws RepositoryException {
        try (ServiceWrapper<GroovyConsoleService> serviceWrapper = sling.getService(GroovyConsoleService.class)) {
        	ScriptContext context = new UpgradeHookScriptContext() //
        			.setResourceResolver(sling.getResourceResolver(script.getSession())) //
        			.setUserId(script.getSession().getUserID()) //
        			.setScript(getScriptContent(script)) //
        			.setData(getData()) //
        			;
        	return serviceWrapper.getService().runScript(context);
        }
    }

    private String getData() throws RepositoryException {
        Node parent = script.getParent();
        return parent.hasProperty("data") ? parent.getProperty("data").getString() : "";
    }

}
