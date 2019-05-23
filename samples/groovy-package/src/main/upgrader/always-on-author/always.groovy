def resource = getResource('/content/we-retail/jcr:content');
resource.adaptTo(ModifiableValueMap).put('alwaysOnAuthor', ++(resource.valueMap['alwaysOnAuthor'] ?: 0))
