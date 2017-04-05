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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.TransientComputerActionFactory;

/**
 * Generates a {@link NodeMonitoringAction} for the each slave computer.
 * @author Oleg Nenashev (o.v.nenashev@gmail.com), Emeric Vernat
 */
@Extension
public class NodesMonitoringActionFactory extends TransientComputerActionFactory {
	/** {@inheritDoc} */
	@Override
	public Collection<? extends Action> createFor(Computer computer) {
		final List<NodeMonitoringAction> result = new ArrayList<>();
		// Add a single monitoring action, which will handle all monitoring features
		result.add(new NodeMonitoringAction(computer, "Monitoring", "monitor.gif"));
		return result;
	}
}
