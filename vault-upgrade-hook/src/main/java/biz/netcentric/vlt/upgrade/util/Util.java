/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import static biz.netcentric.vlt.upgrade.util.LogUtil.error;

import java.util.Collections;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import org.apache.jackrabbit.vault.packaging.InstallContext;

/**
 * Created by Conrad Wöltge on 7/16/15.
 */
public class Util {

    @SuppressWarnings("unchecked")
    public static <T>T getService(Class<T> clazz) {
        Bundle bundle = FrameworkUtil.getBundle(clazz);
        BundleContext context = bundle.getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(clazz.getName());
        return (T) context.getService(serviceReference);
    }

    public static ResourceResolver getResourceResolver(InstallContext ctx) {
        ResourceResolverFactory resourceResolverFactory = getService(ResourceResolverFactory.class);
        try {
            return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap("user.jcr.session", ctx.getSession()));
        } catch (LoginException e) {
            error("Login Exception", e, ctx);
            return null;
        }
    }

    public static void save(InstallContext ctx, Session sess)
   			throws RepositoryException {
   		if (sess.hasPendingChanges()) {
   			LogUtil.info("Saving changes", "", ctx);
   			sess.save();
   		}
   	}
}
