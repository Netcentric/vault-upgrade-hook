/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler;

import javax.jcr.Node;

import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;

public class UpgradeActionInfo {

    private Node node;
    private Phase defaultPhase;

    public UpgradeActionInfo(Node node, Phase defaultPhase) {
        this.node = node;
        this.defaultPhase = defaultPhase;
    }

    public Node getNode() {
        return node;
    }

    public Phase getDefaultPhase() {
        return defaultPhase;
    }
}
