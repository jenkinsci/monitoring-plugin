/*
 * Copyright 2008-2017 by Emeric Vernat
 *
 *     This file is part of the Monitoring plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
