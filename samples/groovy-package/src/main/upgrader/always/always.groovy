def resource = getResource('/content/we-retail/jcr:content');
resource.adaptTo(ModifiableValueMap).put('always', ++(resource.valueMap['always'] ?: 0))
