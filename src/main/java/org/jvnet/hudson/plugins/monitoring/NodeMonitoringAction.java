/*
 * Copyright 2008-2019 by Emeric Vernat
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

import hudson.model.Action;
import hudson.model.Computer;
import jenkins.model.Jenkins;

/**
 * Implements a "Monitoring" button for slaves.
 * This button will be available for everybody with Administer or SystemRead permissions.
 * @author Oleg Nenashev (o.v.nenashev@gmail.com), Emeric Vernat
 * @since 1.49
 */
public class NodeMonitoringAction implements Action {
	private final Computer computer;
	private final String displayName;
	private final String iconPath;

	/**
	 * Constructor.
	 * @param computer Computer
	 * @param displayName String
	 * @param iconPath String
	 */
	public NodeMonitoringAction(Computer computer, String displayName, String iconPath) {
		super();
		this.computer = computer;
		this.displayName = displayName;
		this.iconPath = iconPath;
	}

	/**
	 * @return Computer
	 */
	public Computer getComputer() {
		return computer;
	}

	/** {@inheritDoc} */
	@Override
	public final String getDisplayName() {
		return hasMonitoringPermissions() && computer.isOnline() ? displayName : null;
	}

	/** {@inheritDoc} */
	@Override
	public final String getIconFileName() {
		return hasMonitoringPermissions() && computer.isOnline() ? iconPath : null;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrlName() {
		return "monitoring";
	}

	/**
	 * Used in index.jelly
	 * @return String
	 */
	public String getMonitoringUrl() {
		final String urlSuffix = computer instanceof Jenkins.MasterComputer ? ""
				: "/nodes/" + computer.getName();
		return "../../../monitoring" + urlSuffix;
	}

	/**
	 * Checks that user has access permissions to the monitoring page.
	 * By default, requires global Administer or SystemRead permissions.
	 * @return boolean
	 */
	protected boolean hasMonitoringPermissions() {
		final Jenkins jenkins = Jenkins.getInstance();
		return jenkins.hasPermission(Jenkins.ADMINISTER)
				|| jenkins.hasPermission(SystemReadPermission.SYSTEM_READ);
	}
}
