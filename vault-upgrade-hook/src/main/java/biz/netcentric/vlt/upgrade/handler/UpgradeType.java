/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import org.apache.jackrabbit.vault.packaging.InstallContext;

import biz.netcentric.vlt.upgrade.handler.groovy.GroovyConsoleHandler;
import biz.netcentric.vlt.upgrade.handler.script.ScriptHandler;
import biz.netcentric.vlt.upgrade.handler.slingpipes.SlingPipesHandler;

/**
 * This class creates instances of {@link UpgradeHandler} via reflection.
 */
public enum UpgradeType {

    SCRIPT(ScriptHandler.class),

    GROOVYCONSOLE(GroovyConsoleHandler.class),

    SLINGPIPES(SlingPipesHandler.class);

    private final Class<? extends UpgradeHandler> clazz;

    private UpgradeType(final Class<? extends UpgradeHandler> clazz) {
        this.clazz = clazz;
    }

    public static UpgradeHandler create(final InstallContext ctx, final String key) {
        UpgradeHandler handler = null;
        for (final UpgradeType type : values()) {
            if (type.name().equalsIgnoreCase(key)) {
                handler = create(type.clazz);
            }
        }
        if (handler == null) {
            try {
                handler = create(Class.forName(key));
            } catch (final ClassNotFoundException e) {
                throw new IllegalArgumentException("Cannot find custom handler: " + key, e);
            }
        }
        if (!handler.isAvailable(ctx)) {
            throw new IllegalArgumentException("Handler not available: " + handler);
        }
        return handler;
    }

    private static UpgradeHandler create(final Class<?> clazz) {
        try {
            return (UpgradeHandler) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassCastException e) {
            throw new IllegalArgumentException("Cannot instantiate class: " + clazz, e);
        }
    }

}