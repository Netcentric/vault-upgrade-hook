log.info("DEFAULT groovy: some: Hello World")
println "DEFAULT groovy: some: Hello World"

println "DEFAULT /content/geometrixx/fr has the following sub-pages"

resourceResolver.findResources("/jcr:root/content/geometrixx/fr//element(*,cq:Page)","xpath").each { resource ->

    println resource.path

}