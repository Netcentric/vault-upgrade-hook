/*
 * (C) Copyright 2017 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import org.apache.http.HttpEntity;
import org.apache.sling.testing.clients.AbstractSlingClient;
import org.apache.sling.testing.clients.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostExecutor extends ConfiguredAbstractPoller {
    private static final Logger LOG = LoggerFactory.getLogger(PostExecutor.class);

    private final AbstractSlingClient client;
    private final String path;
    private final HttpEntity httpEntity;
    private final int expectedStatus;

    public PostExecutor(AbstractSlingClient client, String path, HttpEntity httpEntity, int expectedStatus) {
        this.client = client;
        this.path = path;
        this.httpEntity = httpEntity;
        this.expectedStatus = expectedStatus;
    }

    public boolean condition() {
        try {
            client.doPost(path, httpEntity, null, expectedStatus);
        } catch (ClientException e) {
            LOG.warn("Post on {} failed: {}", path, e);
            return false;
        }
        return true;
    }
}
