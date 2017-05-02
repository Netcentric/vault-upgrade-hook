/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Helper class to resolve OSGi services
 */
public class OsgiUtil {

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
	Bundle bundle = FrameworkUtil.getBundle(clazz);
	BundleContext context = bundle.getBundleContext();
	ServiceReference<?> serviceReference = context.getServiceReference(clazz.getName());
	return (T) context.getService(serviceReference);
    }

}
