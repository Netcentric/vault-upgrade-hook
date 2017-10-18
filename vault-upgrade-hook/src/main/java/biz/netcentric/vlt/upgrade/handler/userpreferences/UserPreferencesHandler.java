package biz.netcentric.vlt.upgrade.handler.userpreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.sling.api.resource.ResourceResolver;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.UpgradeInfo;
import biz.netcentric.vlt.upgrade.handler.SlingUtils;
import biz.netcentric.vlt.upgrade.handler.UpgradeHandler;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class UserPreferencesHandler implements UpgradeHandler {

    private static final String PN_USER_IDS = "handler.userIds";
    private static final String NAME_SUFFIX_USER_PREFERENCES = "user.preferences";
    private static final PackageInstallLogger LOG = PackageInstallLogger.create(UserPreferencesHandler.class);

    @Override
    public boolean isAvailable(InstallContext ctx) {
        // this handler does not depend on any optional dependencies
        return true;
    }

    @Override
    public Iterable<UpgradeAction> create(InstallContext ctx, UpgradeInfo info) throws RepositoryException {
        List<UpgradeAction> actions = new ArrayList<>();

        // extract user id from upgrade info node
        if (!info.getNode().hasProperty(PN_USER_IDS)) {
            LOG.debug(ctx, "No property '{}' found on upgrade info {}, not creating any UserPreferencesUpgradeActions!", PN_USER_IDS, info);
            return actions;
        }
        Set<String> userIds = new HashSet<>();
        Property property = info.getNode().getProperty(PN_USER_IDS);
        if (property.isMultiple()) {
            for (Value value : property.getValues()) {
                userIds.add(value.getString());
            }
        } else {
            userIds.add(property.getString());
        }
        NodeIterator children = info.getNode().getNodes();
        ResourceResolver resourceResolver = new SlingUtils().getResourceResolver(ctx.getSession());
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (NAME_SUFFIX_USER_PREFERENCES.equals(child.getName())) {
                LOG.debug(ctx, "Found user preferences node '{}' below upgrade info {}", child.getPath(), info);
                for (String userId : userIds) {
                    actions.add(new UserPreferencesUpgradeAction(userId, resourceResolver.getResource(child.getPath())));
                    LOG.debug(ctx, "Added UserPreferencesUpgradeAction for userId '{}'.", userId);
                }
            }
        }
        return actions;
    }
}
