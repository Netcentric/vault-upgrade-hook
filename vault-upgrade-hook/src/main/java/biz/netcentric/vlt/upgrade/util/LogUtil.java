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

import biz.netcentric.vlt.upgrade.UpgradeProcessor;

public class LogUtil {
	private static final Logger log = LoggerFactory.getLogger(UpgradeProcessor.class);

	public static void info(String action, String msg, InstallContext ctx) {
        ProgressTrackerListener l = ctx.getOptions().getListener();
		if (l != null) {
			l.onMessage(Mode.TEXT, action, msg);
		}
		log.info(action + " " + msg);
	}

	public static void info(String action, String msg, InstallContext ctx, InstallContext.Phase phase) {
        if(ctx.getPhase().equals(phase)) {
            ProgressTrackerListener l = ctx.getOptions().getListener();
            if (l != null) {
                l.onMessage(Mode.TEXT, action, msg);
            }
            log.info(action + " " + msg);
        }
	}

	public static void warn(String action, String msg, InstallContext ctx) {
		ProgressTrackerListener l = ctx.getOptions().getListener();
		if (l != null) {
            l.onMessage(Mode.TEXT, action, msg);
        }
        log.warn(action + " " + msg);
	}

	public static void error(String msg, Exception e, InstallContext ctx) {
		ProgressTrackerListener l = ctx.getOptions().getListener();
		if (l != null) {
			l.onError(Mode.TEXT, msg, e);
		}
		log.error(msg, e);
	}
}
