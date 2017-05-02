


# Sample Package

## create package with hook

Copy `vault-upgrade-hook-0.0.1-SNAPSHOT.jar` to `META-INF/vault/hooks`
Place your scripts to `META-INF/vault/definition/upgrader/foo`
Package to a zip like this:
    
    $ cd sling-pipes-sample-package
    $ zip --filesync -r ../sling-pipes-sample-package.zip *

Upload to AEM and install. Notice the scripts placed to `META-INF/vault/definition/upgrader/foo` are executed.

The sample package has scripts for PREPARE, INSTALLED and END phase as samples in `META-INF/vault/definition/upgrader/test-sling-pipes`

## upload and install package - with output sample

For a quick turnaround you can install via cUrl like this

    $ curl -X POST -F"file=@../sling-pipes-sample-package.zip" -F"install=true" -uadmin:admin localhost:4502/crx/packmgr/service.jsp

Notice output:

    ...
    Installing content
    Creating snapshot for package netcentric:sling-pipes-sample-package:0.0.1-SNAPSHOT 
    A META-INF
    A META-INF/vault
    A META-INF/vault/config.xml
    A META-INF/vault/filter.xml
    A META-INF/vault/nodetypes.cnd
    A META-INF/vault/properties.xml
    A /.content.xml
    A META-INF/vault/definition/.content.xml
    Executing content upgrade in phase PREPARE 
    Content version: 0.0.0 
    Package version: 0.0.1-SNAPSHOT 
    H Executing upgrade: Update via Sling Pipe Handler. - version 0.0.1-SNAPSHOT
    I Found sling pipe at /etc/packages/netcentric/sling-pipes-sample-package-0.0.1-SNAPSHOT.zip/jcr:content/vlt:definition/upgrader/test-sling-pipes/installed_pipe
    I Found sling pipe at /etc/packages/netcentric/sling-pipes-sample-package-0.0.1-SNAPSHOT.zip/jcr:content/vlt:definition/upgrader/test-sling-pipes/end_pipe
    I Found sling pipe at /etc/packages/netcentric/sling-pipes-sample-package-0.0.1-SNAPSHOT.zip/jcr:content/vlt:definition/upgrader/test-sling-pipes/prepare_pipe
    I Found sling pipe at /etc/packages/netcentric/sling-pipes-sample-package-0.0.1-SNAPSHOT.zip/jcr:content/vlt:definition/upgrader/test-sling-pipes/failed_pipe
    I Running sling pipe at prepare_pipe
    I Executing prepare_pipe
    I /content/geometrixx/fr/products
    I /content/geometrixx/fr/services
    ...
    
    Package installed in 178ms.

## package versions and run

The sample package has a settings as `run=always` but can also run as `run=once` or `run=snapshot` where the version check would prevent the scripts from being executed multiple times
