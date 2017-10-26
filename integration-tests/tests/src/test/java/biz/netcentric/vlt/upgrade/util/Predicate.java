/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import org.apache.sling.testing.clients.SlingHttpResponse;

public interface Predicate {

    boolean test(SlingHttpResponse response) throws Exception;

    SlingHttpResponse getLastResponse();
}
