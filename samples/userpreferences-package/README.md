
# Sample Package

## create package with hook

1. Copy `vault-upgrade-hook-0.0.1-SNAPSHOT.jar` to `META-INF/vault/hooks`
1. Create a node below `META-INF/vault/definition/upgrader/`.
The node can be named arbitrarily and should have type `sling:Folder`.
It must have the following properties

| Property Name  | Description | Property Type | Example Value |
| ------------- | ------------- | ---------- | ------ |
| handler  | The internal name of the User Preferences handler. Should always have the value given in the column `Example Value` | String | `userpreferences` |
| handler.userIds | An array of user ids whose preferences should be updated  | String[] | `admin` |

3. Place an enhanced document view XML named `user.preferences.xml` below the node being created in 2. That represents the preferences node which should replace the former preferences node of the user given in `handler.userIds`.

Package to a zip like this:
    
    $ cd sample-userpreference-package
    $ zip --filesync -r ../sample-userpreference-package.zip *

Upload to AEM and install. Notice the user preferences of the admin user are being replaced during the installation.

## upload and install package - with output sample

For a quick turnaround you can install via cUrl like this

    $ curl -X POST -F"file=@../sample-userpreference-package.zip" -F"install=true" -uadmin:admin localhost:4502/crx/packmgr/service.jsp

Notice output:

    ...
    Installing content...
    Upgrade starting [PREPARE]
    Upgrade loaded status [biz.netcentric.vlt.upgrade.UpgradeStatus@6cff174d [node=Node[NodeDelegate{tree=/var/upgrade/vault-upgrade-hook-samples/sample-userpreferences-package: { jcr:primaryType = nt:unstructured}}], version=null]]
    Upgrade updated user preferences of user 'admin' in '/home/users/6/6lfaAeUdY-OYzqrhTKu-/preferences'
    ...
    
    Package installed in 178ms.

