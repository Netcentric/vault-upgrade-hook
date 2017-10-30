// Need to revert changes with conflicts from the page content to be able to modify content
resourceResolver.revert()

def resource = getResource('/content/vault-upgrade-test-resource-name')
def map = resource.adaptTo(ModifiableValueMap.class)
map.put("testResourceValue", "installfailed")
resourceResolver.commit()