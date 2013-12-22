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

import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Hudson;

/**
 * Implements a "Monitoring" button for slaves.
 * This button will be available for everybody with ADMINISTER permission.
 * @author Oleg Nenashev <o.v.nenashev@gmail.com>, Emeric Vernat
 * @since 1.49
 */
public class NodeMonitoringAction implements Action {
	private final Computer computer;
	private final String displayName;
	private final String iconPath;
	private String url;

	/**
	 * Constructeur.
	 * @param computer Computer
	 * @param displayName String
	 * @param iconPath String
	 * @param url String
	 */
	public NodeMonitoringAction(Computer computer, String displayName, String iconPath, String url) {
		super();
		this.computer = computer;
		this.displayName = displayName;
		this.iconPath = iconPath;
		this.url = url;
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
		return url;
	}

	/**
	 * Sets urlName.
	 * @param url String
	 */
	public void setUrlName(String url) {
		this.url = url;
	}

	/**
	 * Checks that user has access permissions to the monitoring page.
	 * By default, requires global Administer permission.
	 * @return boolean
	 */
	protected boolean hasMonitoringPermissions() {
		return Hudson.getInstance().hasPermission(Hudson.ADMINISTER);
	}
}
