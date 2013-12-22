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

import jenkins.model.Jenkins;

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
		result.add(new NodeMonitoringAction(computer, "View threads",
				"../../../../monitoring?resource=threads.png", "../../monitoring?part=threads"));
		result.add(new NodeMonitoringAction(computer, "Execute the garbage collector",
				"../../../../monitoring?resource=broom.png", "../../monitoring?action=gc"));
		result.add(new NodeMonitoringAction(computer, "Generate a heap dump",
				"../../../../monitoring?resource=heapdump.png", "../../monitoring?action=heap_dump"));
		result.add(new NodeMonitoringAction(computer, "View memory histogram",
				"../../../../monitoring?resource=memory.png", "../../monitoring?part=heaphisto"));
		result.add(new NodeMonitoringAction(computer, "MBeans",
				"../../../../monitoring?resource=mbeans.png", "../../monitoring?part=mbeans"));
		result.add(new NodeMonitoringAction(computer, "View OS processes",
				"../../../../monitoring?resource=processes.png", "../../monitoring?part=processes"));
		if (!(computer instanceof Jenkins.MasterComputer)) {
			for (final NodeMonitoringAction action : result) {
				action.setUrlName(action.getUrlName().replace("/monitoring",
						"/monitoring/nodes/" + computer.getName()));
			}
		}
		return result;
	}
}
