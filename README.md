# Vault Upgrade Hook

 AEM/Jackrabbit content packages allow to place Jars to a package in `META-INF/vault/hooks` that will be executed during installation. These hooks have to implement the interface `InstallHook`. 

InstallHooks are executed for each install phase: PREPARE, INSTALLED and END. If an error occurs FAILED is called for PREPARE or INSTALLED. END is called if installation was successful.

Instead of implementing and creating new Java.jars with every job, *Vault-Upgrade-Hooks* aims to allow to run Groovy Scripts developed and tested using the CQ Groovy Console [https://github.com/Citytechinc/cq-groovy-console].

## Features

- Run Groovy Console Scripts places w/in the package files
- Control if the upgrade is executed by Package and install version.
- Control if the upgrade is executed once or with every install of an package


## Usage

Two general ways of how to use it: 

1. ad-hoc admin style:
- take a copy of the sample package, 
- update package info (make sure name, version and group are updated), 
- update test-groovy (replace all scripts with yours, update the run-info (`run`,`version`,`jcr:title`))
- build and install

2. dev-style:
- add a copy job to your maven build (resource plugin) and place the hook.jar
- create a upgrade info folder (like the test-groovy one) and place your scripts in
-> part of your deployment process

###
Run `mvn clean install -PautoInstallPackage`

## More information
### Stores

Run information is stored in Phase END under:

    /var/upgrade/packagegroup/packagename


### Interface implementation AEM6.0 and AEM6.1+
 
 AEM6 expects `com.day.jcr.vault.packaging.InstallHook` 

Packaging has been OpenSourced by Adobe and contributed to Jackrabbit. *AEM6.1+* expects `org.apache.jackrabbit.vault.packaging.InstallHook`. This is the OpenSource version 