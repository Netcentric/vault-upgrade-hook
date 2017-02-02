/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import static biz.netcentric.vlt.upgrade.util.LogUtil.info;
import static biz.netcentric.vlt.upgrade.util.LogUtil.warn;
import static biz.netcentric.vlt.upgrade.util.Util.getService;
import static org.apache.jackrabbit.vault.packaging.InstallContext.Phase;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import biz.netcentric.vlt.upgrade.util.FakeRequest;
import com.citytechinc.aem.groovy.console.GroovyConsoleService;
import com.citytechinc.aem.groovy.console.response.RunScriptResponse;
import com.day.text.Text;

/**
 * User: Conrad Wöltge
 */
public class GroovyConsoleHandler extends UpgradeHandlerBase {

    private Map<Phase, LinkedList<String>> scripts;

    @Override
    public void execute(InstallContext ctx) throws RepositoryException {
        this.ctx = ctx;

        scripts = getScriptsFromConfig();

        Collections.sort(scripts.get(ctx.getPhase())); // make sure we're executing in alphabetical order
        for (String scriptPath : scripts.get(ctx.getPhase())) {
            runScript(scriptPath);
        }
    }

    /**
     * Builds the Map of Phases with a List of ScriptPaths for each Phase.
     * @return Map of ScriptPaths per Phase
     */
    private Map<Phase, LinkedList<String>> getScriptsFromConfig() {
        scripts = new HashMap<>();
        for (Phase phase : Phase.values()) {
            scripts.put(phase, new LinkedList<String>());
        }

        Resource resource = upgradeInfo.getConfigResource();
        for (Resource child : resource.getChildren()) {
            // groovy scripts
            if (StringUtils.endsWith(child.getName(), ".groovy") && child.isResourceType("nt:file")) {
                // it's a script, assign the script to a phase
                scripts.get(getPhaseFromPrefix(child.getName())).add(child.getPath());
            }
        }
        return scripts;
    }

    /**
     * Executes the script from a given path via GroovyConsole.
     * @param scriptPath    the path a package definition to execute
     */
    public void runScript(String scriptPath) {

        GroovyConsoleService groovyConsoleService = getService(GroovyConsoleService.class);
        SlingHttpServletRequest request = getRequestForScript(scriptPath);
        if (request != null) {
            info("I", "Executing " + Text.getName(scriptPath), ctx);
            RunScriptResponse scriptResponse = groovyConsoleService.runScript(request);
            info("I", "Run in " + scriptResponse.getRunningTime() + "ms with output of",ctx );
            info("", scriptResponse.getOutput().trim() ,ctx);
        }
    }

    private SlingHttpServletRequest getRequestForScript(String scriptPath) {

        ResourceResolver resourceResolver = getResourceResolver();
        Resource resource = resourceResolver.getResource(scriptPath + "/" + JcrConstants.JCR_CONTENT);
        String script = null;

        if (resource != null) {
            script = resource.adaptTo(ValueMap.class).get(JcrConstants.JCR_DATA, String.class);
        }
        if (script != null) {
            Map<String, Object> parameters = Collections.<String, Object>singletonMap("script", script);
            return new FakeRequest(resourceResolver, "GET", "/bin/groovyconsole/post.json", parameters);
        } else {
            warn("E", "Can't load script at " + scriptPath, ctx);
            return null;
        }

    }

    public static boolean isAvailable() {
        try {
            GroovyConsoleHandler.class.getClassLoader().loadClass("com.citytechinc.aem.groovy.console.GroovyConsoleService");
            return true;
        } catch(ClassNotFoundException cnfe) {
            return false;
        }
    }

}
