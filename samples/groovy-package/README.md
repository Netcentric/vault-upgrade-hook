# Groovy Console Sample Package

## Features

- executing Groovy scripts within an AEM environment
- the provided context includes many helper methods, Groovy extension methods and a DSL to create content
- see [https://github.com/orbinson/aem-groovy-console] for a complete list of features

## Dependencies

This execution uses the following dependencies in version 18 

- `be.orbinson.aem.groovy.console:aem-groovy-console`

Make sure the included bundles are installed and activated correctly and the `GroovyConsoleService` is active.

The GroovyConsole was forked several times over the last year for support for the `icfolson` or `Citytechinc` variants please see version 2 of the `vault-upgrade-hook`. Furthermore the Groovy Console's compatibility changed with AEM 6.0/6.1, for support see the branch [groovy-console-v8](https://github.com/Netcentric/vault-upgrade-hook/tree/groovy-console-v8]groovy-console-v8).

## Usage

To execute a Groovy script place it under `META-INF/vault/definition/upgrader/<upgrade-info>/[<phase-prefix>]<script-name>.groovy`. The optional `<phase-prefix>` (`PREPARE`, `INSTALLED`, `END`) is not case sensitive and configures the phase of execution. If no phase prefix is there `INSTALLED` is the default. 

Note that the scripts will be sorted and executed by name.

## Difference to script-package

With both packages groovy scripts can be executed. The advantage of the `script-package` is that there are minimal dependencies. Only Sling and Groovy-All have to be installed. Which means it can be used in any Sling environment. This package on the other hand has a dependencies to the GroovyConsole and with it to AEM. The GroovyConsole installs a UI and comes with advanced features for scripting like a DSL for creating resources and Groovy extensions to common JCR, Sling and AEM classes. If you don't need/use those features or if you cannot install the dependency go with the `script-package`.

## Versioning

Groovy script's content is used to generate md5 hash which is used as its version.