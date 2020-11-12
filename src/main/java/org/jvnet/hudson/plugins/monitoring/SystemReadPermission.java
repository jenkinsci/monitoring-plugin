package org.jvnet.hudson.plugins.monitoring;

import hudson.security.Permission;
import hudson.util.ReflectionUtils;
import jenkins.model.Jenkins;

final class SystemReadPermission {
	// TODO replace with Jenkins.SYSTEM_READ after upgrade of minimum requirement to Jenkins 2.222
	static final Permission SYSTEM_READ;
	static {
		Permission systemRead;
		try { // System Read is available starting from Jenkins 2.222 (https://www.jenkins.io/changelog-old/#v2.222). See JEP-224 for more info
			systemRead = (Permission) ReflectionUtils.getPublicProperty(Jenkins.getInstance(),
					"SYSTEM_READ");
		} catch (final Throwable t) { // NOPMD
			systemRead = null;
		}
		SYSTEM_READ = systemRead;
	}

	private SystemReadPermission() {
		super();
	}
}
