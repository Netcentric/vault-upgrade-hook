


# Sample Package

## create package with hook

Copy `vault-upgrade-hook-0.0.1-SNAPSHOT.jar` to `META-INF/vault/hooks`
Place your scripts to `META-INF/vault/definition/upgrader/foo`
Package to a zip like this:
    
    $ cd groovy-sample-package
    $ zip --filesync -r ../groovy-sample-package.zip *

Upload to AEM and install. Notice the scripts placed to `META-INF/vault/definition/upgrader/foo` are executed.

The sample package has scripts for PREPARE, INSTALLED and END phase as samples in `META-INF/vault/definition/upgrader/test-groovy` 

## upload and install package - with output sample

For a quick turnaround you can install via cUrl like this

    $ curl -X POST -F"file=@../groovy-sample-package.zip" -F"install=true" -uadmin:admin localhost:4502/crx/packmgr/service.jsp

Notice output:

    ...
    Installing content...
    Executing content upgrade in phase PREPARE
    Content version: 0.0.1-SNAPSHOT
    Package version: 0.0.1-SNAPSHOT
    H Executing upgrade: Update via Groovy Script. - version 0.0.1-SNAPSHOT
    I Executing prepare_a.groovy
    I Executing prepare_something_step2.groovy
    I Executing prepare_something_step3.groovy
    ...
    saving approx 0 nodes...
    Package imported.
    Executing content upgrade in phase INSTALLED
    Package version: 0.0.1-SNAPSHOT
    I Executing installed-foo.groovy
    I Executing somename.groovy
    Executing content upgrade in phase END
    Package version: 0.0.1-SNAPSHOT
    I Executing end-all-good.groovy
    Package installed in 178ms.

## package versions and run

The sample package has a settings as `run=always` but can also run as `run=once` or `run=snapshot` where the version check would prevent the scripts from being executed multiple times
