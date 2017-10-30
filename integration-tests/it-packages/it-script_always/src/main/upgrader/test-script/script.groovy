import org.apache.sling.api.resource.ModifiableValueMap

def rr = resource.getResourceResolver()
def resource = rr.resolve("/content/vault-upgrade-test-script-resource-name")
def map = resource.adaptTo(ModifiableValueMap.class)
map.put("testResourceValue", "TestValue")
rr.commit()