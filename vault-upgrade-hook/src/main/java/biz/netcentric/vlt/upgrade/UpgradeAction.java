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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
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

    public UpgradeAction(final String name, final Phase phase, final String contentHash) {
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
    protected static Phase getPhaseFromPrefix(final Phase defaultPhase, final String name) {
        // need to loop from the end to avoid conflicts of "PREPARE" and
        // "PREPARE_FAILED"
        for (int i = Phase.values().length - 1; i >= 0; i--) {
            if (name.toLowerCase().startsWith(Phase.values()[i].name().toLowerCase())) {
                return Phase.values()[i];
            }
        }
        return defaultPhase;
    }

    public boolean isRelevant(final InstallContext ctx, final UpgradeInfo info) throws RepositoryException {
        return !info.getStatus().isExecuted(ctx, info, this);
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

    protected static String getDataMd5(final Node script) throws RepositoryException {
        final String encoding = JcrUtils.getStringProperty(script, JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_ENCODING, "utf-8");
        return getMd5(getScriptContent(script), encoding);
    }

    protected static String getScriptContent(final Node script) throws RepositoryException {
        final String dataPath = JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_DATA;
        if (script.hasProperty(dataPath)) {
            return JcrUtils.getStringProperty(script, dataPath, "");
        } else {
            throw new RepositoryException("Cannot load script content from " + script);
        }
    }

    protected static String getMd5(final String content, final String encoding) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] md5 = md.digest(content.getBytes(encoding));
            final StringWriter md5Base64 = new StringWriter();
            Base64.encode(md5, 0, md5.length, md5Base64);
            return md5Base64.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("Error while encoding script content.", e);
        }
    }

    @Override
    public int compareTo(final UpgradeAction o) {
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
    public boolean equals(final Object obj) {
        if (obj instanceof UpgradeAction) {
            return getName().equals(((UpgradeAction) obj).getName());
        } else {
            return false;
        }
    }

}
