package biz.netcentric.vlt.upgrade.handler.script;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingScript;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.UpgradeInfo;
import biz.netcentric.vlt.upgrade.handler.SlingUtils;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class ScriptHandler implements UpgradeHandler {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(ScriptHandler.class);

    SlingUtils                                sling = new SlingUtils();

    @Override
    public boolean isAvailable(final InstallContext ctx) {
        return true;
    }

    @Override
    public Iterable<UpgradeAction> create(final InstallContext ctx, final UpgradeInfo info) throws RepositoryException {
        final List<UpgradeAction> scripts = new ArrayList<>();

        final Resource root = sling.getResourceResolver(ctx.getSession()).getResource(info.getNode().getPath());
        if (root == null) {
            throw new IllegalArgumentException("Could not get resource from node: " + info.getNode().getPath());
        }
        for (final Resource child : root.getChildren()) {
            if (!child.isResourceType(JcrConstants.NT_FILE)) {
                LOG.debug(ctx, "[{}] ignored.", child);
                continue;
            }
            final SlingScript script = child.adaptTo(SlingScript.class);
            if (script == null) {
                LOG.warn(ctx, "[{}] could not be adapted to SlingScript.", child);
            } else {
                scripts.add(new Script(script, info.getDefaultPhase()));
            }
        }

        return scripts;
    }

}
