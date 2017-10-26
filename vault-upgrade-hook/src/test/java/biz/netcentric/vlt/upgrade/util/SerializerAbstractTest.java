/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;

public abstract class SerializerAbstractTest {

    static final String RESOURCE_NAME = "resource";
    static final String CHILD_RESOURCE_NAME = "child_resource";
    static final String PATH = "/content/some/" + RESOURCE_NAME;
    static final String PARENT_PROP = "ParentProp";
    static final String PARENT_VALUE = "ParentValue";
    static final String CHILD_PROP = "ChildProp";
    static final String CHILD_VALUE = "ChildValue";

    Resource resource;
    Node node;

    @Rule
    public final SlingContext sling = new SlingContext(ResourceResolverType.JCR_MOCK);

    @Before
    public void setUp() {
        Map<String, Object> parentProps = new HashMap<>();
        parentProps.put(PARENT_PROP, PARENT_VALUE);
        parentProps.put("jcr:created", new Date());
        sling.build()
                .resource(PATH, parentProps)
                .resource(PATH + "/" + CHILD_RESOURCE_NAME, Collections.singletonMap(CHILD_PROP, CHILD_VALUE)).commit();

        resource = sling.resourceResolver().getResource(PATH);
        node = resource.adaptTo(Node.class);
    }
}
