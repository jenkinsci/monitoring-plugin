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

import java.io.IOException;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;
import jenkins.model.Jenkins;
import net.bull.javamelody.NodesCollector;

/**
 * Listener of nodes to update data when nodes become online or offline without
 * waiting 1 minute.
 * 
 * @author Emeric Vernat
 */
@Extension
public class NodesListener extends ComputerListener {
	private NodesCollector nodesCollector;

	/**
	 * Constructor.
	 */
	public NodesListener() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void onOnline(Computer c, TaskListener listener)
			throws IOException, InterruptedException {
		scheduleCollectNow();
		super.onOnline(c, listener);
	}

	/** {@inheritDoc} */
	@Override
	public void onOffline(Computer c, OfflineCause cause) {
		scheduleCollectNow();
		super.onOffline(c, cause);
	}

	private void scheduleCollectNow() {
		try {
			final NodesCollector collector = getNodesCollector();
			if (collector != null) {
				collector.scheduleCollectNow();
			}
		} catch (final IllegalStateException e) {
			// if timer already canceled, do nothing
			// [JENKINS-17757] IllegalStateException: Timer already cancelled from NodesCollector.scheduleCollectNow
		}
	}

	private NodesCollector getNodesCollector() {
		if (nodesCollector == null) {
			final Jenkins jenkins = Jenkins.getInstance();
			if (jenkins != null) {
				final PluginImpl pluginImpl = jenkins.getPlugin(PluginImpl.class);
				if (pluginImpl != null) {
					final HudsonMonitoringFilter monitoringFilter = pluginImpl.getFilter();
					if (monitoringFilter != null) {
						nodesCollector = monitoringFilter.getNodesCollector();
					}
				}
			}
		}
		return nodesCollector;
	}
}
