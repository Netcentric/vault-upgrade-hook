/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.util;

import static org.assertj.core.api.Assertions.assertThat;

import javax.jcr.RepositoryException;

import org.junit.Test;

public class JsonNodeSerializerTest extends SerializerAbstractTest {

    @Test
    public void shouldReturnNonEmptyString() throws RepositoryException {
        String result = new JsonNodeSerializer().serialize(node);
        assertThat(result)
                .as("Result of node's serialization should not be empty")
                .isNotEmpty();
    }

    @Test
    public void shouldSerializeNodeName() throws RepositoryException {
        String result = new JsonNodeSerializer().serialize(node);
        assertThat(result)
                .as("Node's name should be serialized")
                .contains(RESOURCE_NAME);
    }

    @Test
    public void shouldSerializeNodeProperties() throws RepositoryException {
        String result = new JsonNodeSerializer().serialize(node);
        assertThat(result)
                .as("Node's properties should be serialized")
                .contains(PARENT_PROP)
                .contains(PARENT_VALUE);
    }

    @Test
    public void shouldSerializeChildNodeName() throws RepositoryException {
        String result = new JsonNodeSerializer().serialize(node);
        assertThat(result)
                .as("Child node's name should be serialized")
                .contains(CHILD_RESOURCE_NAME);
    }

    @Test
    public void shouldSerializeChildNodeProperties() throws RepositoryException {
        String result = new JsonNodeSerializer().serialize(node);
        assertThat(result)
                .as("Child node's properties should be serialized")
                .contains(CHILD_PROP)
                .contains(CHILD_VALUE);
    }

    @Test
    public void shouldOmitProperties() throws RepositoryException {
        String result = new JsonNodeSerializer().serialize(node);
        assertThat(result)
                .as("Auto created properties should not be used for serialization")
                .doesNotContain("jcr:created");
    }
}