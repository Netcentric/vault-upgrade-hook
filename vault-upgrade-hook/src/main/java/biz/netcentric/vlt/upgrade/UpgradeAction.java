/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.util.Base64;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;

/**
 * This class represents a single step in an upgrade process, for example a
 * script.
 */
public abstract class UpgradeAction implements Comparable<UpgradeAction> {

    private final String name;
    private final Phase phase;
    private final String contentHash;

    public UpgradeAction(String name, Phase phase, String contentHash) {
        this.name = name;
        this.phase = phase;
        this.contentHash = contentHash;
    }

    /**
     * returns the correct Phase for a script name by its prefix.
     * 
     * @param defaultPhase
     * @param name
     * @return related phase, {@code defaultPhase} if no {@code name} is not
     *         prefixed by a phase.
     */
    protected static Phase getPhaseFromPrefix(Phase defaultPhase, String name) {
        // need to loop from the end to avoid conflicts of "PREPARE" and
        // "PREPARE_FAILED"
        for (int i = Phase.values().length - 1; i >= 0; i--) {
            if (name.toLowerCase().startsWith(Phase.values()[i].name().toLowerCase())) {
                return Phase.values()[i];
            }
        }
        return defaultPhase;
    }

    public boolean isRelevant(InstallContext ctx, UpgradeInfo info) throws RepositoryException {
        return !info.getStatus().isExecuted(ctx, info, this.getName() + "_" + getContentHash());
    }

    /**
     * @return the identifying name of this action. This name is unique within a
     *         {@link UpgradeInfo}.
     */
    public String getName() {
        return name;
    }

    public Phase getPhase() {
        return phase;
    }

    public String getContentHash() {
        return contentHash;
    }

    public abstract void execute(InstallContext ctx) throws RepositoryException;

    protected static String getMd5(String content, String encoding) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5 = md.digest(content.getBytes(encoding));
            StringWriter md5Base64 = new StringWriter();
            Base64.encode(md5, 0, md5.length, md5Base64);
            return md5Base64.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("Error while encoding script content.", e);
        }
    }

    @Override
    public int compareTo(UpgradeAction o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UpgradeAction) {
            return getName().equals(((UpgradeAction) obj).getName());
        } else {
            return false;
        }
    }

}
