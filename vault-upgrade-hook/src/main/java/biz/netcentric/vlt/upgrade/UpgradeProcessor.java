/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import static biz.netcentric.vlt.upgrade.util.LogUtil.error;
import static biz.netcentric.vlt.upgrade.util.LogUtil.info;
import static biz.netcentric.vlt.upgrade.util.Util.getResourceResolver;

import java.util.*;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallHook;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import biz.netcentric.vlt.upgrade.handler.UpgradeHandlerBase;
import biz.netcentric.vlt.upgrade.version.ArtifactVersion;
import biz.netcentric.vlt.upgrade.version.DefaultArtifactVersion;
import com.day.cq.commons.jcr.JcrUtil;

public class UpgradeProcessor implements InstallHook {

    private static final String PN_UPGRADE_VERSION = "version";
    private static final String PN_UPGRADE_TIME = "time";
    private static final String STATUS_PATH = "/var/upgrade";
    private static final String UNDEFINED_VERSION = "0.0.0";
    public static final String UPGRADER_PATH_IN_PACKAGE = ".zip/jcr:content/vlt:definition/upgrader";

    boolean failed = false;

    // ----< InstallHook interface >--------------------------------------------

    @Override
    public void execute(InstallContext ctx) throws PackageException {
        info("Executing content upgrade in phase " + ctx.getPhase(), "", ctx);

        try {
            ArtifactVersion sourceVersion = getSourceVersion(ctx);
            ArtifactVersion targetVersion = getTargetVersion(ctx);

            // load upgrade infos
            List<UpgradeInfo> infos = loadUpgradeInfos(ctx);

            // sort upgrade infos according to their version and priority
            Collections.sort(infos);

            // for each upgrade info: check whether it should be included, and if so execute its handler
            for (UpgradeInfo upgradeInfo : infos) {
                if (includeInfo(sourceVersion, targetVersion, upgradeInfo)) {
                    info("H", "Executing upgrade: " + upgradeInfo.getTitle() + " - version " + upgradeInfo.getVersion(),
                            ctx, InstallContext.Phase.PREPARE);
                    UpgradeHandlerBase handler = upgradeInfo.getHandler();
                    handler.execute(ctx);
                }
            }

            // if we are in the END phase, store the status info into the repository
            if (!failed && ctx.getPhase() == InstallContext.Phase.END) {
                storeUpgradeStatus(ctx, targetVersion);
            }
        } catch (Exception e) {
            failed = true;
            error("Error during content upgrade", e, ctx);
            throw new PackageException(e);
        }
    }

    // ----< internal >---------------------------------------------------------

    /**
     * Load and return all upgrade infos in the package.
     * @param ctx   The install context.
     * @return      A list of upgrade infos.
     * @throws RepositoryException
     */
    private List<UpgradeInfo> loadUpgradeInfos(InstallContext ctx) throws RepositoryException, PackageException {

        List<UpgradeInfo> infos = new ArrayList<>();

        String upgradeInfoPath = ctx.getPackage().getId().getInstallationPath() + UPGRADER_PATH_IN_PACKAGE;

        ResourceResolver resourceResolver = getResourceResolver(ctx);
        Resource upgradeInfoResource = resourceResolver.getResource(upgradeInfoPath);
        if (upgradeInfoResource != null) {
            for (Resource res : upgradeInfoResource.getChildren()) {
                final UpgradeInfo upgradeInfo = new UpgradeInfo(res, ctx);
                if (upgradeInfo.getHandler() != null) {
                    infos.add(upgradeInfo);
                }
            }
        }
        return infos;
    }

    /**
     * Get the target version, i.e. the version of the package being installed.
     * @param ctx   The install context.
     * @return The target version.
     */
    private ArtifactVersion getTargetVersion(InstallContext ctx) {
        ArtifactVersion version = new DefaultArtifactVersion(ctx.getPackage().getId().getVersionString());
        info("Package version: " + version, "", ctx, InstallContext.Phase.PREPARE);
        return version;
    }

    /**
     * Get the source version, i.e. the last installed version.
     * @param ctx   The install context.
     * @return The last installed version, or 0.0.0 if no status info available.
     * @throws RepositoryException
     */
    private ArtifactVersion getSourceVersion(InstallContext ctx) throws RepositoryException {
        Session session = ctx.getSession();

        String versionInfo = UNDEFINED_VERSION;
        String versionProp = getStatusPath(ctx.getPackage().getId()) + "/" + PN_UPGRADE_VERSION;
        if (session.propertyExists(versionProp)) {
            versionInfo = session.getProperty(versionProp).getString();
        }
        ArtifactVersion version = new DefaultArtifactVersion(versionInfo);
        info("Content version: " + version, "", ctx, InstallContext.Phase.PREPARE);
        return version;
    }

    /**
     * Return the absolute JCR path to the version status information.
     * @param packageId The package ID to build the path from.
     * @return The status path.
     */
    private String getStatusPath(PackageId packageId) {
        return STATUS_PATH + "/" + packageId.getGroup() + "/" + packageId.getName();
    }

    /**
     * Store the upgrade version and timestamp into the repository.
     * @param ctx           The install context.
     * @param targetVersion The target version of the package install.
     * @throws RepositoryException
     */
    public void storeUpgradeStatus(InstallContext ctx, ArtifactVersion targetVersion) throws RepositoryException {

        final String statusPath = getStatusPath(ctx.getPackage().getId());

        Session session = ctx.getSession();

        Node status;
        if (!session.itemExists(statusPath)) {
            JcrUtil.createPath(statusPath, "sling:Folder", ctx.getSession());
        }
        status = session.getNode(statusPath);

        final Calendar now = Calendar.getInstance();
        status.setProperty(PN_UPGRADE_TIME, now);
        status.setProperty(PN_UPGRADE_VERSION, targetVersion.toString());
        session.save();
    }

    /**
     * Check, if an upgrade info should be included, depending on its version and on the specified runType.
     * @param source        The source version.
     * @param target        The target version.
     * @param upgradeInfo   The upgrade info to check.
     * @return true, if the upgrade info should be included; false otherwise.
     */
    public boolean includeInfo(ArtifactVersion source, ArtifactVersion target, UpgradeInfo upgradeInfo) {

        UpgradeInfo.RunType runType = upgradeInfo.getRunType();
        if(runType == UpgradeInfo.RunType.ALWAYS) {
            return true;
        }
        else {
            if (UNDEFINED_VERSION.equals(source.toString())) {
                return false; // don't spool all upgrades on a new installation
            }

            // ONCE || SNAPSHOT
            ArtifactVersion upgradeInfoVersion = upgradeInfo.getVersion();

            // activate "snapshot mode" (install even if version == sourceVersion) iff the "snapshot" run type is set
            // and the package version is a snapshot version
            boolean snapshotUpgrade = (runType == UpgradeInfo.RunType.SNAPSHOT && isSnapshotVersion(target));

            // if snapshot: return true if source <= v <= target
            // else:        return true if source < v <= target
            return (compareToIgnoreQualifier(source, upgradeInfoVersion) < 0 || snapshotUpgrade && compareToIgnoreQualifier(source, upgradeInfoVersion) == 0)
                 && compareToIgnoreQualifier(target, upgradeInfoVersion) >= 0;
        }
    }

    /**
     * Check, if the specified version is a snapshot version.
     * @param version The version.
     * @return true if version is a snapshot version; false otherwise.
     */
    private boolean isSnapshotVersion(ArtifactVersion version) {
        return "snapshot".equalsIgnoreCase(version.getQualifier());
    }

    /**
     * Compare two versions disregarding the qualifier (e.g. "SNAPSHOT"), that is: 2.1.7-SNAPSHOT == 2.1.7
     * @param v1 The first version.
     * @param v2 The second version.
     * @return Same logic as Comparable.compareTo(): -1 iff v1 < v2; 1 iff v1 > v2; 0 iff v1 == v2
     */
    private int compareToIgnoreQualifier(ArtifactVersion v1, ArtifactVersion v2) {
        List<Integer> versionComponents1 = Arrays.asList(v1.getMajorVersion(), v1.getMinorVersion(), v1.getIncrementalVersion());
        List<Integer> versionComponents2 = Arrays.asList(v2.getMajorVersion(), v2.getMinorVersion(), v2.getIncrementalVersion());
        for(int i=0; i<3; i++) {
            int cmp = versionComponents1.get(i).compareTo(versionComponents2.get(i));
            if(cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
