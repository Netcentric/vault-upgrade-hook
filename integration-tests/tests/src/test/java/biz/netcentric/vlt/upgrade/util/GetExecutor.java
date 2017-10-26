/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import org.apache.sling.testing.clients.AbstractSlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetExecutor extends ConfiguredAbstractPoller {
    private static final Logger LOG = LoggerFactory.getLogger(GetExecutor.class);

    private final AbstractSlingClient client;
    private final String path;
    private final Predicate predicate;
    private final int expectedStatus;

    public GetExecutor(AbstractSlingClient client, String path, Predicate predicate, int expectedStatus) {
        this.client = client;
        this.path = path;
        this.predicate = predicate;
        this.expectedStatus = expectedStatus;
    }

    public boolean condition() {
        try {
            SlingHttpResponse slingHttpResponse = client.doGet(path, expectedStatus);
            if (predicate != null) {
                return predicate.test(slingHttpResponse);
            }
        } catch (Exception e) {
            LOG.warn("Get on {} failed: {}", path, e);
            return false;
        }
        return true;
    }
}
