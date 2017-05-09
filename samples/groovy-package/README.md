# Groovy Console Sample Package

## Features

- executing Groovy scripts within an AEM environment
- the provided context includes many helper methods, Groovy extension methods and a DSL to create content
- see [https://github.com/Citytechinc/cq-groovy-console] for a complete list of features

## Dependencies

This execution uses one of the following dependencies in any version greater or equals then 7 

- `com.citytechinc.aem.groovy.console:aem-groovy-console`
- `com.icfolson.aem.groovy.console:aem-groovy-console`

Make sure the bundle is installed and activated correctly and the `GroovyConsoleService` is available.

## Usage

To execute a Groovy script place it under `META-INF/vault/definition/upgrader/<upgrade-info>/[<phase-prefix>]<script-name>.groovy`. The optional `<phase-prefix>` (`PREPARE`, `INSTALLED`, `END`) is not case sensitive and configures the phase of execution. If no phase prefix is there `INSTALLED` is the default. 

Note that the scripts will be sorted and executed by name.