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

    public <T> ServiceWrapper<T> getService(Class<T> clazz) {
        Bundle bundle = FrameworkUtil.getBundle(clazz);
	if (bundle == null) {
	    return null;
	}
        BundleContext context = bundle.getBundleContext();
	if (context == null) {
	    return null;
	}
	ServiceReference<T> serviceReference = context.getServiceReference(clazz);
	if (serviceReference == null) {
	    return null;
	}
	ServiceWrapper<T> serviceWrapper = new ServiceWrapper<>(context, serviceReference);
	if (serviceWrapper.getService() == null) {
	    serviceWrapper.close();
	    return null;
	}
	return serviceWrapper;
    }

    public void close(ServiceWrapper<?> serviceWrapper) {
	if (serviceWrapper != null) {
	    serviceWrapper.close();
	}
    }

    public static class ServiceWrapper<T> implements AutoCloseable {

	private final BundleContext context;
	private final ServiceReference<T> serviceReference;
	private final T service;

	public ServiceWrapper(BundleContext context, ServiceReference<T> serviceReference) {
	    this.context = context;
	    this.serviceReference = serviceReference;
	    this.service = context.getService(serviceReference);
	}

	public T getService() {
	    return service;
	}

	@Override
	public void close() {
	    context.ungetService(serviceReference);
	}

    }

}
