package biz.netcentric.vlt.upgrade.handler.userpreferences;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class UserPreferencesUpgradeAction extends UpgradeAction {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(UserPreferencesUpgradeAction.class);
    
    private final Node xmlFileNode;
    private final String userId;
    
    public UserPreferencesUpgradeAction(String userId, Node xmlFileNode) throws RepositoryException {
        super(xmlFileNode.getName() + "/" + userId, Phase.PREPARE, "");
        this.xmlFileNode = xmlFileNode;
        this.userId = userId;
    }

    @Override
    public void execute(InstallContext ctx) throws RepositoryException {
        if (!(ctx.getSession() instanceof JackrabbitSession)) {
            throw new IllegalStateException("User preferences upgrade hook can only be used on a Jackrabbit-based repo!");
        }
        JackrabbitSession js = (JackrabbitSession) ctx.getSession();
        Authorizable authorizable = js.getUserManager().getAuthorizable(userId);
        if (authorizable == null) {
            throw new IllegalStateException("No user with id '"+userId+"' found. Therefore cannot modify its preferences!");
        }
        if (!(authorizable instanceof User)) {
            throw new IllegalStateException("Found authorizable with id '"+userId+"' but it is not a user. Therefore cannot modify its preferences!");
        }
        User user = (User)authorizable;
        
        // remove existing user preferences node
        String userPreferencesPath = user.getPath()+"/preferences";
        if (ctx.getSession().nodeExists(userPreferencesPath)) {
            ctx.getSession().removeItem(userPreferencesPath);
            ctx.getSession().save();
        }
        // copy the given node to the user preference
        ctx.getSession().getWorkspace().copy(xmlFileNode.getPath(), userPreferencesPath);
        LOG.info(ctx, "updated user preferences of user '{}' in '{}'", userId, userPreferencesPath);
    }

}
