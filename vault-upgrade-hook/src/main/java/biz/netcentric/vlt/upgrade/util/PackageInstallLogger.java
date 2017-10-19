package biz.netcentric.vlt.upgrade.util;

import org.apache.jackrabbit.vault.packaging.InstallContext;

public interface PackageInstallLogger {
    void debug(InstallContext ctx, String format, Object... arguments);

    void info(InstallContext ctx, String format, Object... arguments);

    void status(InstallContext ctx, String format, String path, Object... arguments);

    void warn(InstallContext ctx, String format, Object... arguments);

    void error(InstallContext ctx, String format, Object... arguments);
}
