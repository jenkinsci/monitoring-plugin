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

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;

/**
 * {@link ManagementLink} of the plugin to add a link in the "/manage" page, for the agents next to the one for the instance.
 * @author Emeric Vernat
 */
@Extension(ordinal = Integer.MAX_VALUE - 491)
public class NodesManagementLink extends ManagementLink {
	/**
	 * Mostly works like {@link hudson.model.Action#getIconFileName()}, except
	 * that the expected icon size is 48x48, not 24x24. So if you give just a
	 * file name, "/images/48x48" will be assumed.
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
	 * Optional.
	 */
	@Override
	public String getDescription() {
		return "Monitoring of builds, build queue and Jenkins agents.";
	}

	/**
	 * Gets the string to be displayed.
	 * The convention is to capitalize the first letter of each word, such as
	 * "Test Result".
	 */
	@Override
	public String getDisplayName() {
		return "Monitoring of Jenkins agents";
	}

	/** {@inheritDoc} */
	@Override
	public Permission getRequiredPermission() {
		//This link is displayed to any user with permission to access the management menu
		return Jenkins.READ;
	}

	/** {@inheritDoc} */
	@Override
	public String getUrlName() {
		final StaplerRequest req = Stapler.getCurrentRequest();
		if (req != null) {
			return req.getContextPath() + "/monitoring/nodes";
		}
		return "/monitoring/nodes";
	}

	/**
	 * Name of the category for this management link. Exists so that plugins with core dependency pre-dating the version
	 * when this was introduced can define a category.
	 *
	 * TODO when the core version is &gt;2.226 change this to override {@code getCategory()} instead
	 *
	 * @return name of the desired category, one of the enum values of Category, e.g. {@code STATUS}.
	 * @since 2.226
	 */
	public String getCategoryName() {
		return "STATUS";
	}
}
