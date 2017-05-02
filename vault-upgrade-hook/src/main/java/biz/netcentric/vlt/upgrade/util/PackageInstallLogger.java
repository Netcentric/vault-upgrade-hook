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
public class PackageInstallLogger {
    private final Logger log;

    public PackageInstallLogger(Logger log) {
	this.log = log;
    }

    public static PackageInstallLogger create(Class<?> clazz) {
	return new PackageInstallLogger(LoggerFactory.getLogger(clazz));
    }

    public void debug(InstallContext ctx, String format, Object... arguments) {
	if (log.isDebugEnabled()) {
	    FormattingTuple message = build(ctx, format, arguments);
	    log.debug(message.getMessage(), message.getThrowable());
	    log(ctx, message);
	}
    }

    public void info(InstallContext ctx, String format, Object... arguments) {
	FormattingTuple message = build(ctx, format, arguments);
	log.info(message.getMessage(), message.getThrowable());
	log(ctx, message);
    }

    public void warn(InstallContext ctx, String format, Object... arguments) {
	FormattingTuple message = build(ctx, format, arguments);
	log.warn(message.getMessage(), message.getThrowable());
	log(ctx, message);
    }

    public void error(InstallContext ctx, String format, Object... arguments) {
	FormattingTuple message = build(ctx, format, arguments);
	log.error(message.getMessage(), message.getThrowable());
	log(ctx, message);
    }

    protected FormattingTuple build(InstallContext ctx, String format, Object[] arguments) {
	return MessageFormatter.arrayFormat(format, arguments);
    }

    protected void log(InstallContext ctx, FormattingTuple message) {
	ProgressTrackerListener l = ctx.getOptions().getListener();
	if (l != null) {
	    if (message.getThrowable() != null) {
		l.onError(Mode.TEXT, message.getMessage(), toException(message.getThrowable()));
	    } else {
		l.onMessage(Mode.TEXT, "Upgrade", message.getMessage());
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

}
