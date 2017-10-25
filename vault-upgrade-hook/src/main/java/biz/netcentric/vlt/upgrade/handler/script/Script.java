package biz.netcentric.vlt.upgrade.handler.script;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScript;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class Script extends UpgradeAction {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(Script.class);

    private final SlingScript                 delegate;

    public Script(final SlingScript delegate, final Phase defaultPhase) throws RepositoryException {
        super(delegate.getScriptResource().getName(), UpgradeAction.getPhaseFromPrefix(defaultPhase, delegate.getScriptResource().getName()),
                getMd5(delegate));
        this.delegate = delegate;
    }

    protected static String getMd5(final SlingScript delegate) throws RepositoryException {
        final Node node = delegate.getScriptResource().adaptTo(Node.class);
        if (node == null) {
            throw new IllegalArgumentException("Cannot get Node: " + delegate);
        }
        return getDataMd5(node);
    }

    @Override
    public void execute(final InstallContext ctx) throws RepositoryException {
        final SlingBindings bindings = new SlingBindings();
        final StringWriter scriptOutput = new StringWriter();
        bindings.setOut(new PrintWriter(scriptOutput));
        bindings.setResource(delegate.getScriptResource());
        delegate.eval(bindings);
        LOG.info(ctx, "[{}] output: [{}]", delegate.getScriptResource().getName(), scriptOutput);
    }

    public SlingScript getDelegate() {
        return delegate;
    }

}
