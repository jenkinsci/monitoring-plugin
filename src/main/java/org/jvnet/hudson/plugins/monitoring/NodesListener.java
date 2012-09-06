/*
 * Copyright 2008-2011 by Emeric Vernat
 *
 *     This file is part of the Monitoring plugin.
 *
 * The Monitoring plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Monitoring plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Monitoring plugin.  If not, see <http://www.gnu.org/licenses/>.
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

	/**
	 * Constructor.
	 * @param nodesCollector NodesCollector
	 */
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
