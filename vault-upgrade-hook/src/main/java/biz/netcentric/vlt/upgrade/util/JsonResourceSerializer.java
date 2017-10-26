/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;

public class JsonResourceSerializer {

    public String serialize(final Resource resource) {

        try {
            return new JsonNodeSerializer().serialize(resource.adaptTo(Node.class));
        } catch (RepositoryException e) {
            throw new RuntimeException("Can not serialize resource: " + resource.getPath(), e);
        }
    }
}
