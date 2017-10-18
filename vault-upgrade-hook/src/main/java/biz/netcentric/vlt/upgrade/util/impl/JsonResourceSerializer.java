/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.google.gson.Gson;

import biz.netcentric.vlt.upgrade.util.ResourceSerializer;

public class JsonResourceSerializer implements ResourceSerializer {

    private static final List<String> PROPERTY_PATTERN_TO_OMIT = Arrays.asList("jcr:.*");

    private static final String NODE_NAME = "name";

    @Override
    public String serialize(final Resource resource) {

        Map<String, Object> tree = getResourceMap(resource);
        traversToMap(resource, tree);

        return new Gson().toJson(tree);
    }

    private void traversToMap(final Resource parentResource, final Map<String, Object> parentMap) {
        for(Resource child : parentResource.getChildren()) {
            Map<String, Object> childMap = getResourceMap(child);
            parentMap.put(child.getName(), childMap);
            traversToMap(child, childMap);
        }
    }

    private Map<String, Object> getResourceMap(final Resource resource) {
        HashMap<String, Object> map = new HashMap<>();
        ValueMap resourceValueMap = resource.adaptTo(ValueMap.class);
        for (final Map.Entry<String,Object> entry : resourceValueMap.entrySet()) {
            for (final String propPattern : PROPERTY_PATTERN_TO_OMIT) {
                if (!entry.getKey().matches(propPattern)) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }

        map.put(NODE_NAME, resource.getName());

        return map;
    }
}
