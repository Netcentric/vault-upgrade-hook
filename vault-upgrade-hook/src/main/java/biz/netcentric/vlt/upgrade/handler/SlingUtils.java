/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.settings.SlingSettingsService;

/**
 * Helper class extending {@link OsgiUtil} to get a Sling
 * {@link ResourceResolver}.
 */
public class SlingUtils extends OsgiUtil {

    public ResourceResolver getResourceResolver(Session session) {
        try (ServiceWrapper<ResourceResolverFactory> serviceWrapper = getService(ResourceResolverFactory.class)) {
            return serviceWrapper.getService()
                    .getResourceResolver(Collections.<String, Object>singletonMap("user.jcr.session", session));
        } catch (LoginException e) {
            throw new IllegalStateException("Cannot get ResourceResolver from session.", e);
        }
    }

    public boolean hasRunModes(Set<String> requiredRunModes) {
        Iterator<String> it = requiredRunModes.iterator();
        boolean hasMatchingSetOfRunModes = requiredRunModes.isEmpty();
        while (!hasMatchingSetOfRunModes && it.hasNext()) {
            String next = it.next();
            if (next == null || next.length() == 0) {
                // Set allows at most one null object, also empty string doesn't make sense
                throw new IllegalArgumentException("null or empty string cannot be a required runmode.");
            }
            Collection<String> collectionOfRequiredRunModes = Arrays.asList(next.split("\\."));
            hasMatchingSetOfRunModes = getRunModes().containsAll(collectionOfRequiredRunModes);
        }
        return hasMatchingSetOfRunModes;
    }

    protected Set<String> getRunModes() {
        try (ServiceWrapper<SlingSettingsService> serviceWrapper = getService(SlingSettingsService.class)) {
            return serviceWrapper.getService().getRunModes();
        } catch (IllegalStateException ex) {
            LOG.debug("Failed to get SlingSettingsService. Returning empty set of runmodes.", ex);
            return Collections.emptySet();
        }
    }
}
