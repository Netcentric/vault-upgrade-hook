/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import static biz.netcentric.vlt.upgrade.util.LogUtil.error;
import static biz.netcentric.vlt.upgrade.util.LogUtil.info;
import static biz.netcentric.vlt.upgrade.util.Util.getService;
import static org.apache.jackrabbit.vault.packaging.InstallContext.Phase;

import java.util.*;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.pipes.Pipe;
import org.apache.sling.pipes.Plumber;

import com.day.text.Text;

/**
 * User: Chris Pilsworth
 */
public class SlingPipesHandler extends UpgradeHandlerBase {

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
            // sling pipes
            if (StringUtils.startsWith(child.getResourceType(), "slingPipes/")) {
                scripts.get(getPhaseFromPrefix(child.getName())).add(child.getPath());
            }
        }
        return scripts;
    }

    /**
     * Executes the sling pipe
     * @param pipePath    the path a package definition to execute
     */
    public void runScript(String pipePath) {

        info("I", "Running sling pipe at " + Text.getName(pipePath), ctx);
        Plumber plumber = getService(Plumber.class);
        ResourceResolver resourceResolver = getResourceResolver();
        Resource resource = resourceResolver.getResource(pipePath);
        if (resource != null) {
            Pipe pipe = plumber.getPipe(resource);
            if (pipe.isDryRun()) {
                info("I", "Executing dry run of " + Text.getName(pipePath), ctx);
            } else {
                info("I", "Executing " + Text.getName(pipePath), ctx);
            }

            final Iterator<Resource> output = pipe.getOutput();

            while (output.hasNext()) {
                Resource r = output.next();
                // output affected resource path for information
                info("I", r.getPath(), ctx);
            }

            if (pipe.modifiesContent() && !pipe.isDryRun()) {
                try {
                    resourceResolver.commit();
                } catch (PersistenceException e) {
                    warn("E", "Exception saving sling pipe output " + pipePath + ". " + e.getMessage(), ctx);
                }
            }
        }
    }

    public static boolean isAvailable() {
        try {
            SlingPipesHandler.class.getClassLoader().loadClass("org.apache.sling.pipes.Plumber");
            return true;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }

}
