# Groovy Console Sample Package

## Features

- executing Groovy scripts within an AEM environment
- the provided context includes many helper methods, Groovy extension methods and a DSL to create content
- see [https://github.com/Citytechinc/cq-groovy-console] for a complete list of features

## Dependencies

This execution uses one of the following dependencies in any version greater or equals then 7 

- `com.citytechinc.aem.groovy.console:aem-groovy-console`
- `com.icfolson.aem.groovy.console:aem-groovy-console`

Make sure the included bundles are installed and activated correctly and the `GroovyConsoleService` is active.

## Usage

To execute a Groovy script place it under `META-INF/vault/definition/upgrader/<upgrade-info>/[<phase-prefix>]<script-name>.groovy`. The optional `<phase-prefix>` (`PREPARE`, `INSTALLED`, `END`) is not case sensitive and configures the phase of execution. If no phase prefix is there `INSTALLED` is the default. 

Note that the scripts will be sorted and executed by name.

## Difference to script-package

With both packages groovy scripts can be exectued. The advantage of the `script-package` is that there are minimal dependencies. Only Sling and Groovy-All have to be installed. Which means it can be used in any Sling environment. This package on the other hand has a dependencies to the GroovyConsole and with it to AEM. The GroovyConsole installs a UI and comes with advanced features for scripting like a DSL for creating resources and Groovy extensions to common JCR, Sling and AEM classes. If you don't need/use those features or if you cannot install the dependency go with the `script-package`.