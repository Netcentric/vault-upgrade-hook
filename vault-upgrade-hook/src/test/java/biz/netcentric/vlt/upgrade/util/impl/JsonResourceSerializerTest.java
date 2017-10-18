package biz.netcentric.vlt.upgrade.util.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class JsonResourceSerializerTest {

    private static final String RESOURCE_NAME = "resource";
    private static final String CHILD_RESOURCE_NAME = "child_resource";
    private static final String PATH = "/content/some/" + RESOURCE_NAME;
    private static final String PARENT_PROP = "ParentProp";
    private static final String PARENT_VALUE = "ParentValue";
    private static final String CHILD_PROP = "ChildProp";
    private static final String CHILD_VALUE = "ChildValue";

    @Rule
    public final SlingContext sling = new SlingContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    @Before
    public void setUp() {
        Map<String, Object> parentProps = new HashMap<>();
        parentProps.put(PARENT_PROP, PARENT_VALUE);
        parentProps.put("jcr:created", new Date());
        sling.build()
                .resource(PATH, parentProps)
                .resource(PATH + "/" + CHILD_RESOURCE_NAME, Collections.singletonMap(CHILD_PROP, CHILD_VALUE)).commit();
    }

    @Test
    public void shouldReturnNonEmptyString() {
        String result = new JsonResourceSerializer().serialize(sling.resourceResolver().getResource(PATH));
        assertThat(result)
                .as("Result of node's serialization should not be empty")
                .isNotEmpty();
    }

    @Test
    public void shouldSerializeNodeName() {
        String result = new JsonResourceSerializer().serialize(sling.resourceResolver().getResource(PATH));
        assertThat(result)
                .as("Node's name should be serialized")
                .contains(RESOURCE_NAME);
    }

    @Test
    public void shouldSerializeNodeProperties() {
        String result = new JsonResourceSerializer().serialize(sling.resourceResolver().getResource(PATH));
        assertThat(result)
                .as("Node's properties should be serialized")
                .contains(PARENT_PROP)
                .contains(PARENT_VALUE);
    }

    @Test
    public void shouldSerializeChildNodeName() {
        String result = new JsonResourceSerializer().serialize(sling.resourceResolver().getResource(PATH));
        assertThat(result)
                .as("Child node's name should be serialized")
                .contains(CHILD_RESOURCE_NAME);
    }

    @Test
    public void shouldSerializeChildNodeProperties() {
        String result = new JsonResourceSerializer().serialize(sling.resourceResolver().getResource(PATH));
        assertThat(result)
                .as("Child node's properties should be serialized")
                .contains(CHILD_PROP)
                .contains(CHILD_VALUE);
    }

    @Test
    public void shouldOmitProperties() {
        String result = new JsonResourceSerializer().serialize(sling.resourceResolver().getResource(PATH));
        assertThat(result)
                .as("Auto created properties should not be used for serialization")
                .doesNotContain("jcr:created");
    }
}