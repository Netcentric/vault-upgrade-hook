/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import static biz.netcentric.vlt.upgrade.util.LogUtil.info;
import static biz.netcentric.vlt.upgrade.util.LogUtil.warn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;

import biz.netcentric.vlt.upgrade.handler.SlingPipesHandler;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import biz.netcentric.vlt.upgrade.handler.GroovyConsoleHandler;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandlerBase;
import biz.netcentric.vlt.upgrade.version.ArtifactVersion;
import biz.netcentric.vlt.upgrade.version.DefaultArtifactVersion;
import org.apache.jackrabbit.vault.packaging.InstallContext;

/**
 * User: Conrad Wöltge
 */
public class UpgradeInfo implements Comparable<UpgradeInfo> {

    public enum RunType {
        ONCE,       // run only once, i.e. if source version < version <= target version (default)
        SNAPSHOT,   // like ONCE, but if target version is a SNAPSHOT version, then also install if source version == version
        ALWAYS;      // run always, completely disregarding versions

        public static RunType fromString(String text) {
            for (RunType runType : RunType.values()) {
                if (runType.toString().toLowerCase().equals(text.toLowerCase())) {
                    return runType;
                }
            }
            return ONCE;
        }
    }

    public enum HandlerType {
        GROOVY,         // Groovy Console script
        SLINGPIPES,     // Sling pipes definition
        CUSTOM,         // Custom java handler
        UNKNOWN;        // unrecognised type

        public static HandlerType fromString(String text) {
            for (HandlerType handlerType : HandlerType.values()) {
                if (handlerType.toString().toLowerCase().equals(text.toLowerCase())) {
                    return handlerType;
                }
            }
            return UNKNOWN;
        }
    }

    private static final String PN_VERSION = "version";

    private static final String PN_PRIORITY = "priority";
    private static final String PN_HANDLER = "handler";
    private static final String PN_HANDLERCLASS = "handlerClass";
    private static final String PN_DEFAULTSEARCHPATHS = "defaultSearchPaths";
    private static final String PN_RUN = "run";
    private static final String PN_JCR_TITLE = "jcr:title";

    private ArtifactVersion version;

    private long priority;
    private List<String> defaultSearchPaths;
    private RunType runType;
    private HandlerType handlerType;

    private UpgradeHandlerBase handler;
    private InstallContext ctx;
    private ValueMap config;
    private Resource configResource;

    /**
     * Create upgrade info.
     *
     * @param configResource The config resource.
     * @param ctx            The install context.
     * @throws RepositoryException
     */
    public UpgradeInfo(Resource configResource, InstallContext ctx) throws RepositoryException {

        this.ctx = ctx;
        this.config = configResource.adaptTo(ValueMap.class);
        this.configResource = configResource;

        this.version = new DefaultArtifactVersion(config.get(PN_VERSION, "0.0.0"));
        this.priority = config.get(PN_PRIORITY, Long.MAX_VALUE);
        this.defaultSearchPaths = new ArrayList<>(Arrays.asList(
                config.get(PN_DEFAULTSEARCHPATHS, ArrayUtils.EMPTY_STRING_ARRAY)));
        this.runType = RunType.fromString(config.get(PN_RUN, RunType.ONCE.toString()));
        this.handlerType = HandlerType.fromString(config.get(PN_HANDLER, HandlerType.GROOVY.toString()));
    }

    /*
    * Getter
    */
    public ArtifactVersion getVersion() {
        return version;
    }

    public List<String> getDefaultSearchPaths() {
        return defaultSearchPaths;
    }

    public RunType getRunType() {
        return runType;
    }

    public UpgradeHandlerBase getHandler() throws RepositoryException, PackageException {

        switch (handlerType) {

            case GROOVY:
                if (GroovyConsoleHandler.isAvailable()) {
                    handler = new GroovyConsoleHandler();
                } else {
                    throw new PackageException("Could not find handler of type 'groovy', do you have it installed?");
                }
                break;

            case SLINGPIPES:
                if (SlingPipesHandler.isAvailable()) {
                    handler = new SlingPipesHandler();
                } else {
                    throw new PackageException("Could not find handler of type 'slingpipes', do you have it installed?");
                }
                break;

            case CUSTOM:
                String handlerClass = config.get(PN_HANDLERCLASS, String.class);
                if (StringUtils.isNotBlank(handlerClass)) {
                    try {
                        handler = (UpgradeHandlerBase) this.getClass().getClassLoader().loadClass(handlerClass).newInstance();
                        info("I", "Using custom handler: " + handlerClass, ctx);
                    } catch (Exception e) {
                        warn("W", "Could not load custom handler: " + handlerClass, ctx);
                    }
                } else {
                    throw new PackageException("Custom upgrade handler, but no handlerClass specified.");
                }
                break;

            case UNKNOWN:
            default:
                String handlerName = config.get(PN_HANDLER, StringUtils.EMPTY).toLowerCase();
                throw new PackageException("Did not recognise upgrade handler of name '" + handlerName + "'. Please use groovy, slingpipes or custom");
        }

        if (handler != null) {
            handler.setUpgradeInfo(this);
        }

        return handler;
    }

    public ValueMap getConfig() {
        return config;
    }

    public Resource getConfigResource() {
        return configResource;
    }

    public String getTitle() {
        return config.get(PN_JCR_TITLE, configResource.getName());
    }

    @Override
    public int compareTo(UpgradeInfo other) {
        // first sorting criterion: version
        int versionCompare = version.compareTo(other.version);
        if (versionCompare == 0) {
            // second sorting criterion: priority
            return (priority < other.priority) ? -1 : ((priority == other.priority) ? 0 : 1);
        } else {
            return versionCompare;
        }
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
