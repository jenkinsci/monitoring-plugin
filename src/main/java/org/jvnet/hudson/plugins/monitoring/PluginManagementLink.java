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

import hudson.Extension;
import hudson.model.ManagementLink;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * ManagementLink of the plugin to add a link in the "/manage" page.
 * 
 * @author Emeric Vernat
 */
@Extension
public class PluginManagementLink extends ManagementLink {
	/**
	 * Mostly works like {@link hudson.model.Action#getIconFileName()}, except
	 * that the expected icon size is 48x48, not 24x24. So if you give just a
	 * file name, "/images/48x48" will be assumed.
	 * 
	 * @return As a special case, return null to exclude this object from the
	 *         management link. This is useful for defining
	 *         {@link ManagementLink} that only shows up under certain
	 *         circumstances.
	 */
	@Override
	public String getIconFileName() {
		return "monitor.gif";
	}

	/**
	 * Returns a short description of what this link does. This text is the one
	 * that's displayed in grey. This can include HTML, although the use of
	 * block tags is highly discouraged.
	 * 
	 * Optional.
	 */
	@Override
	public String getDescription() {
		return "Monitoring of memory, cpu, http requests and more in Hudson/Jenkins master."
				+ "<br/>You can also view the monitoring of <a href='./monitoring/nodes'>Hudson/Jenkins nodes</a>.";
	}

	/**
	 * Gets the string to be displayed.
	 * 
	 * The convention is to capitalize the first letter of each word, such as
	 * "Test Result".
	 */
	@Override
	public String getDisplayName() {
		return "Monitoring of Hudson/Jenkins master";
	}

	/** {@inheritDoc} */
	@Override
	public String getUrlName() {
		final StaplerRequest req = Stapler.getCurrentRequest();
		if (req != null) {
			return req.getContextPath() + "/monitoring";
		}
		return "/monitoring";
	}
}
