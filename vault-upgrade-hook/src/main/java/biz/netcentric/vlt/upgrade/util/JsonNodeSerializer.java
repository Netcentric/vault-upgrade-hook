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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

public class JsonNodeSerializer {

    private static final List<String> PROPERTY_PATTERN_TO_OMIT = Arrays.asList("jcr:.*");

    private static final String NODE_NAME = "vault_upgrade_configuration_node_name";

    public String serialize(final Node node) throws RepositoryException {

        Map<String, Object> tree = getNodeMap(node);
        traversToMap(node, tree);

        return tree.toString();
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
        Map<String, Object> map = new TreeMap<>();
        PropertyIterator properties = node.getProperties();
        while(properties.hasNext()) {
            Property property = properties.nextProperty();
            for (final String propPattern : PROPERTY_PATTERN_TO_OMIT) {
                if (!property.getName().matches(propPattern)) {

                    // For now we expect to see here only string value
                    map.put(property.getName(), property.getString());
                }
            }
        }

        map.put(NODE_NAME, node.getName());

        return map;
    }
}
