def resource = getResource('/content/we-retail/jcr:content');
resource.adaptTo(ModifiableValueMap).put('onchange', ++(resource.valueMap['onchange'] ?: 0))
