/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import com.google.gson.Gson;

public class JsonNodeSerializer {

    private static final List<String> PROPERTY_PATTERN_TO_OMIT = Arrays.asList("jcr:.*");

    private static final String NODE_NAME = "name";

    public String serialize(final Node node) throws RepositoryException {

        Map<String, Object> tree = getNodeMap(node);
        traversToMap(node, tree);

        return new Gson().toJson(tree);
    }

    private void traversToMap(final Node node, final Map<String, Object> parentMap) throws RepositoryException {
        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            final Node child = children.nextNode();
            Map<String, Object> childMap = getNodeMap(child);
            parentMap.put(child.getName(), childMap);
            traversToMap(child, childMap);
        }
    }

    private Map<String, Object> getNodeMap(final Node node) throws RepositoryException {
        HashMap<String, Object> map = new HashMap<>();
        PropertyIterator properties = node.getProperties();
        while(properties.hasNext()) {
            Property property = properties.nextProperty();
            for (final String propPattern : PROPERTY_PATTERN_TO_OMIT) {
                if (!property.getName().matches(propPattern)) {
                    map.put(property.getName(), property.getValue());
                }
            }
        }

        map.put(NODE_NAME, node.getName());

        return map;
    }
}
