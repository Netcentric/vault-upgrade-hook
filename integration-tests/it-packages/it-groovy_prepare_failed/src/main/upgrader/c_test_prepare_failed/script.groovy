package c_test_prepare_failed

def resource = getResource('/content/vault-upgrade-test-resource-name')
def map = resource.adaptTo(ModifiableValueMap.class)
map.put("testResourceValue", ((String) map.get("testResourceValue")) + "_preparefailed")
resourceResolver.commit()