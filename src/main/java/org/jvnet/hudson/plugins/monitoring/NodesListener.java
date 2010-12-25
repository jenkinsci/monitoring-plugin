/*
 * Copyright 2008-2011 by Emeric Vernat
 */
package org.jvnet.hudson.plugins.monitoring;

import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.slaves.ComputerListener;

import java.io.IOException;

import net.bull.javamelody.NodesCollector;

/**
 * Listener of nodes to update data when nodes become online or offline without
 * waiting 1 minute.
 * 
 * @author Emeric Vernat
 */
public class NodesListener extends ComputerListener {
	private final NodesCollector nodesCollector;

	public NodesListener(NodesCollector nodesCollector) {
		super();
		this.nodesCollector = nodesCollector;
	}

	/** {@inheritDoc} */
	@Override
	public void onOnline(Computer c, TaskListener listener) throws IOException,
			InterruptedException {
		nodesCollector.scheduleCollectNow();
		super.onOnline(c, listener);
	}

	/** {@inheritDoc} */
	@Override
	public void onOffline(Computer c) {
		try {
			nodesCollector.scheduleCollectNow();
		} catch (final IllegalStateException e) {
			// if timer already canceled, do nothing
		}
		super.onOffline(c);
	}
}
