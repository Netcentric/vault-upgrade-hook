/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.testUtils;

import org.apache.jackrabbit.vault.packaging.InstallContext;

import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class LoggerStub extends PackageInstallLogger {

    public LoggerStub() {
        super(null);
    }

    @Override
    public void debug(InstallContext ctx, String format, Object... arguments) {

    }

    @Override
    public void info(InstallContext ctx, String format, Object... arguments) {

    }

    @Override
    public void status(InstallContext ctx, String format, String path, Object... arguments) {

    }

    @Override
    public void warn(InstallContext ctx, String format, Object... arguments) {

    }

    @Override
    public void error(InstallContext ctx, String format, Object... arguments) {

    }
}
