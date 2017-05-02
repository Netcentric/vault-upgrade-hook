/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import java.util.Collections;

import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

/**
 * Helper class extending {@link OsgiUtil} to get a Sling
 * {@link ResourceResolver}.
 */
public class SlingUtils extends OsgiUtil {

    public ResourceResolver getResourceResolver(Session session) {
	ResourceResolverFactory resourceResolverFactory = getService(ResourceResolverFactory.class);
	try {
	    return resourceResolverFactory
		    .getResourceResolver(Collections.<String, Object>singletonMap("user.jcr.session", session));
	} catch (LoginException e) {
	    throw new IllegalStateException("Cannot get ResourceResolver from session.", e);
	}
    }

}
