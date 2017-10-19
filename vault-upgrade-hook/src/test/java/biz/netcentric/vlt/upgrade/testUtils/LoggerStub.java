package biz.netcentric.vlt.upgrade.testUtils;

import org.apache.jackrabbit.vault.packaging.InstallContext;

import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;

public class LoggerStub implements PackageInstallLogger {
    @Override
    public void debug(InstallContext ctx, String format, Object... arguments) {

    }

    @Override
    public void info(InstallContext ctx, String format, Object... arguments) {

    }

    @Override
    public void status(InstallContext ctx, String format, String path, Object... arguments) {

    }

    @Override
    public void warn(InstallContext ctx, String format, Object... arguments) {

    }

    @Override
    public void error(InstallContext ctx, String format, Object... arguments) {

    }
}
