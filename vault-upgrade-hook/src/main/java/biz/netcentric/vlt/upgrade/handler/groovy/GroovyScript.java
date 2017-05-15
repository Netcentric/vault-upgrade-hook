/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler.groovy;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.sling.api.SlingHttpServletRequest;

import com.citytechinc.aem.groovy.console.GroovyConsoleService;
import com.citytechinc.aem.groovy.console.response.RunScriptResponse;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.handler.OsgiUtil.ServiceWrapper;
import biz.netcentric.vlt.upgrade.handler.SlingUtils;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class GroovyScript extends UpgradeAction {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(GroovyScript.class);

    protected static String getContentHash(Node script) throws RepositoryException {
        String encoding = JcrUtils.getStringProperty(script, JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_ENCODING,
                "utf-8");
        return getMd5(getScriptContent(script), encoding);
    }

    protected static String getScriptContent(Node script) throws RepositoryException {
        String dataPath = JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_DATA;
        if (script.hasProperty(dataPath)) {
            return JcrUtils.getStringProperty(script, dataPath, "");
        } else {
            throw new RepositoryException("Cannot load script content from " + script);
        }
    }

    SlingUtils sling = new SlingUtils();
    private final Node script;

    public GroovyScript(Node script, Phase defaultPhase) throws RepositoryException {
        super(script.getName(), UpgradeAction.getPhaseFromPrefix(defaultPhase, script.getName()),
                getContentHash(script));
        this.script = script;
    }

    @Override
    public void execute(InstallContext ctx) throws RepositoryException {
        SlingHttpServletRequest request = getRequestForScript();
        if (request != null) {
            LOG.debug(ctx, "Executing [{}]", getName());
            RunScriptResponse scriptResponse = run(request);
            LOG.info(ctx, "Executed [{}]: [{}]\n{}\n---\n", getName(), scriptResponse.getRunningTime(),
                    scriptResponse.getOutput().trim());
        }
    }

    protected RunScriptResponse run(SlingHttpServletRequest request) {
        try (ServiceWrapper<GroovyConsoleService> serviceWrapper = sling.getService(GroovyConsoleService.class)) {
            return serviceWrapper.getService().runScript(request);
        }
    }

    protected SlingHttpServletRequest getRequestForScript() throws RepositoryException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("script", getScriptContent(script));
        parameters.put("scriptPath", script.getPath());
        return new FakeRequest(sling.getResourceResolver(script.getSession()), "GET", "/bin/groovyconsole/post.json",
                parameters);
    }

}
