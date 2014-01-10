/*
 * Copyright 2013 by Emeric Vernat
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

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientComputerActionFactory;
import hudson.model.Computer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generates a {@link NodeMonitoringAction} for the each slave computer.
 * @author Oleg Nenashev <o.v.nenashev@gmail.com>, Emeric Vernat
 */
@Extension
public class NodesMonitoringActionFactory extends TransientComputerActionFactory {
	/** {@inheritDoc} */
	@Override
	public Collection<? extends Action> createFor(Computer computer) {
		final List<NodeMonitoringAction> result = new ArrayList<NodeMonitoringAction>();
		// Add a single monitoring action, which will handle all monitoring features
		result.add(new NodeMonitoringAction(computer, "Monitoring", "monitor.gif"));
		return result;
	}
}
