[![GitHub](https://img.shields.io/github/license/Netcentric/vault-upgrade-hook)](LICENSE.txt)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/Netcentric/vault-upgrade-hook/continuous-deployment/develop)](https://github.com/Netcentric/vault-upgrade-hook/actions)
[![Maven Central](https://img.shields.io/maven-central/v/biz.netcentric.vlt.upgrade/vault-upgrade-hook.svg?label=Maven%20Central)](https://search.maven.org/artifact/biz.netcentric.vlt.upgrade/vault-upgrade-hook)

# Vault Upgrade Hook

The *Vault-Upgrade-Hook* is an easy way to add additional logic to the installation of content packages. Nearly all projects are facing sooner or later the problem that existing user generated content has to be upgraded. Often the existing mechanism with plain content packages, filters and install modes is not enough and more complex and flexible approaches are needed. The *Vault-Upgrade-Hook* provides an alternative to implementing new Jars for every of those jobs by embedding additional actions (i.e. groovy scripts) directly into the content package. 

## Feature Overview

- installation mode "always" to execute on every package installation 
- installation mode "on_change" to execute only new and changed actions (see section **Versioning** in specific readme files for more info)
- minimum dependencies
- flexible API for custom action functionality
- convention over configuration, but still many options

## Requirements
 
`AEM6.0 SP3` and above, if you are using Sling without AEM see the detailed dependencies in `vault-upgrade-hook/pom.xml`.

Some provided samples also have additional requirements. You can find them in local readme files.

## Usage

Two general ways of how to use it: 

1. ad-hoc admin style:
    1. take a copy of one of the prepared projects under `samples/...`, 
    1. update actions under `src/main/upgrader` (e.g. test-groovy - replace all scripts with yours),
    1. build and install: `mvn -Pinstall`, target server and credentials can be set via `-Dcrx....`

2. dev-style:
    1. integrate the JAR via maven copy to your content package:
        ```
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-dependency-plugin</artifactId>
          <executions>
            <execution>
              <id>copy</id>
              <phase>prepare-package</phase>
              <goals>
                <goal>copy</goal>
              </goals>
              <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>biz.netcentric.vlt.upgrade</groupId>
                        <artifactId>vault-upgrade-hook</artifactId>
                        <version>${vault.upgrade.hook.version}</version>
                        <overWrite>true</overWrite>
                        <outputDirectory>${project.build.directory}/vault-work/META-INF/vault/hooks</outputDirectory>
                    </artifactItem>
                </artifactItems>
              </configuration>
            </execution>
          </executions>
        </plugin>
        ```
    2. create a directory (like `samples/groovy-package/src/main/upgrader/always`) and place it in your content package under `META-INF/vault/definition/upgrader`.

Note that the general structure of the package is always the same. There is a folder `META-INF/vault/definition/upgrader/<upgrade-info>` in the content package which contains the configuration properties for the contained actions. Depending on the used handler those actions are child nodes containing Groovy scripts or SlingPipes.   

## More information

### Jackrabbit FileVault package hook

AEM/Jackrabbit content packages allow to place JARs in a package below `META-INF/vault/hooks`. Those must contain a class implementing `InstallHook` and will be executed during installation. The installation is split into phases: `PREPARE`, `INSTALLED` and `END`. `END` is guaranteed to be called at the end of an installation process. If `PREPARE` phase or installation process fails, then `PREPARE_FAILED` or `INSTALL_FAILED` respectively are called. The *Vault-Upgrade-Hook* uses this mechanism and builds an upgrade process on top of it.

### Upgrade Process

There are 3 main entities used in the upgrade process:

1. `UpgradeAction` - a piece of logic (like a Groovy script) that will be executed during upgrade;
1. `UpgradeInfo` - contains `UpgradeAction`s and provides general configuration how the actions should be executed;
1. `UpgradeHandler` - creating `UpgradeAction`s.

The upgrade process is embedded in the installation phases of the package. On `PREPARE` the status of the last execution will be loaded and on `END` the status will be saved. On all phases `UpgradeAction`s will be executed if the package contains actions for the current phase. At stated above, `UpgradeAction`s are bundled in `UpgradeInfo`s which provide general configuration how the actions should be executed. For example does the `UpgradeInfo`s define to use Groovy scripts for the upgrade or SlingPipes. Also options like the `mode` which decides about whether to execute actions will be configured there.

#### Upgrade Info 

The upgrade info nodes carry the actual configuration as well as the actions to execute.
They need to be placed in `META-INF/vault/definition/upgrader/<upgrade-info>` within the content package zip.

The `<upgrade-info>` nodes (serialized as [DocView .content.xml files](https://jackrabbit.apache.org/filevault/docview.html)) support the following properties and must be of type `sling:Folder`.

Property Name | Property Type | Description | Default Value | Mandatory
--- | --- | --- | --- | ---
`mode` | `String` | Either `on_change` or `always`.  With `on_change` only new and changed actions are executed, otherwise the given actions are always executed (in case the run mode condition is fullfilled and the phase is executed, independent of previous executions) | `on_change` | no
`runmodes` | `String[]` | One or multiple [run mode values](https://sling.apache.org/documentation/bundles/sling-settings-org-apache-sling-settings.html) from which at least one value must be fullfilled for the action to be executed (Discjunction/OR). If none is set the action will always be executed! Each run mode value has the following grammar: `<runmode>{.<runmode>}` where multiple `<runmode>`s (concatenated by `.`) all need to be set (Conjunction/AND) | - (no restriction) | no
`defaultPhase` | `String` | Specifies the default phase when the action is executed. One of `prepare`, `installed` or `end`. See also <https://jackrabbit.apache.org/filevault/apidocs/org/apache/jackrabbit/vault/packaging/InstallContext.Phase.html>. Only relevant if the action name does not specify a phase. | `prepare` | no
`handler` | `String` | Either `script`, `groovyconsole`, `slingpipes`, `userpreferences` or another fully-qualified classname of a class implementing `UpgradeHandler` | - | yes

In addition the update info contains the actions in subnodes. For details refer to the samples. 

During installation of the content package `biz.netcentric.vlt.upgrade.UpgradeProcessor.execute(InstallContext)` will be called for each of the [phases](https://jackrabbit.apache.org/filevault/apidocs/org/apache/jackrabbit/vault/packaging/InstallContext.Phase.html). The processor will read the status of previous executions from `/var/upgrade` and loads the `biz.netcentric.vlt.upgrade.UpgradeInfo` child nodes from the uploaded content package under `<package-path>/jcr:content/vlt:definition/upgrader`. On phase `END` the the list of all executed actions is stored in `/var/upgrade`.

An `UpgradeInfo` loads a `biz.netcentric.vlt.upgrade.handler.UpgradeHandler` implementation to create `biz.netcentric.vlt.upgrade.handler.UpgradeAction`s which are executed during the upgrade. Whether an `UpgradeInfo` and an `UpgradeAction` is executed depends on the conditions set by `runmodes` and `mode`

`UpgradeAction`s are bound to a specific execution phase. The default phase is `PREPARE`. This means an arbitrary action is executed before the content is installed. Specific handlers may provide additional ways of executing at specific phases (for provided samples see corresponding readme files). Usually the `defaultPhase` can be overridden by prefixing the script/configuration name with the name of another phase e.g. "prepare_failed-myscript.groovy".

### Upgrade Actions
Multiple different upgrade actions are included with this hook. Those are also referred to as handlers. For details refer to the following sections.

#### Groovy

For usage and details please see the [sample package](samples/groovy-package).

#### Sling Scripting

For usage and details please see the [sample package](samples/script-package).

#### Sling Pipes

For details about Sling Pipes please have a look at [Sling documentation](https://sling.apache.org/documentation/bundles/sling-pipes.html) and the [sample package](samples/sling-pipes-package).

#### User Preferences

For usage and details please see the [sample package](samples/userpreferences-package).
