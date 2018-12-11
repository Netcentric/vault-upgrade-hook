def resource = getResource('/content/vault-upgrade-test-resource-name')
def map = resource.adaptTo(ModifiableValueMap.class)
map.put("testResourceValue", "prepare")
resourceResolver.commit()