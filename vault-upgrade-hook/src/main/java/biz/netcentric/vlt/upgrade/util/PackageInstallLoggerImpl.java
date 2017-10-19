/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener.Mode;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Helper class to log during content package installation.
 */
public class PackageInstallLoggerImpl implements PackageInstallLogger {
    private final Logger base;

    public PackageInstallLoggerImpl(Logger log) {
        base = log;
    }

    public static PackageInstallLoggerImpl create(Class<?> clazz) {
        return new PackageInstallLoggerImpl(LoggerFactory.getLogger(clazz));
    }

    @Override
    public void debug(InstallContext ctx, String format, Object... arguments) {
        if (getBase().isDebugEnabled()) {
            FormattingTuple message = build(format, arguments);
            getBase().debug(message.getMessage(), message.getThrowable());
        }
    }

    @Override
    public void info(InstallContext ctx, String format, Object... arguments) {
        FormattingTuple message = build(format, arguments);
        getBase().info(message.getMessage(), message.getThrowable());
    }

    @Override
    public void status(InstallContext ctx, String format, String path, Object... arguments) {
        info(ctx, format + " - " + path, arguments);
        progressLog(ctx, build(format, arguments), path);
    }

    @Override
    public void warn(InstallContext ctx, String format, Object... arguments) {
        FormattingTuple message = build(format, arguments);
        getBase().warn(message.getMessage(), message.getThrowable());
        progressLog(ctx, message, null);
    }

    @Override
    public void error(InstallContext ctx, String format, Object... arguments) {
        FormattingTuple message = build(format, arguments);
        getBase().error(message.getMessage(), message.getThrowable());
        progressLog(ctx, message, null);
    }

    protected FormattingTuple build(String format, Object[] arguments) {
        return MessageFormatter.arrayFormat(format, arguments);
    }

    protected void progressLog(InstallContext ctx, FormattingTuple message, String path) {
        ProgressTrackerListener l = ctx.getOptions().getListener();
        if (l != null) {
            if (message.getThrowable() != null) {
                l.onError(Mode.TEXT, message.getMessage(), toException(message.getThrowable()));
            } else {
                l.onMessage(path == null ? Mode.TEXT : Mode.PATHS, "Upgrade " + message.getMessage(),
                        path == null ? "" : path);
            }
        }
    }

    protected Exception toException(Throwable throwable) {
        if (throwable instanceof Exception) {
            return (Exception) throwable;
        } else {
            return new Exception(throwable);
        }
    }

    public Logger getBase() {
        return base;
    }

}
