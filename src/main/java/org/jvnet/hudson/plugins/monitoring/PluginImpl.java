package org.jvnet.hudson.plugins.monitoring;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.util.PluginServletFilter;

import java.io.File;

/**
 * Entry point of the plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author Emeric Vernat
 */
public class PluginImpl extends Plugin {

  @Override
  public void start() throws Exception {
    super.start();
	
	// on active les actions syst?mes (gc, heap dump, histogramme m?moire, processus...), sauf si l'administrateur a dit diff?remment
	if (System.getProperty("javamelody.system-actions-enabled") == null) {
		System.setProperty("javamelody.system-actions-enabled", "true");
	}
	// on d?sactive les graphiques jdbc et statistiques sql puisqu'il n'y en aura pas
	if (System.getProperty("javamelody.no-database") == null) {
		System.setProperty("javamelody.no-database", "true");
	}
	// le r?pertoire de stockage est dans le r?pertoire de hudson au lieu d'?tre dans le r?pertoire temporaire
	// ("/" initial n?cessaire sous windows pour javamelody v1.8.1)
	if (System.getProperty("javamelody.storage-directory") == null) {
		System.setProperty("javamelody.storage-directory", "/" + new File(Hudson.getInstance().getRootDir(),"monitoring").getAbsolutePath());
	}
	// google-analytics pour conna?tre le nombre d'installations actives et pour conna?tre les fonctions les plus utilis?es
	if (System.getProperty("javamelody.analytics-id") == null) {
		System.setProperty("javamelody.analytics-id", "UA-1335263-7");
	}
	
	PluginServletFilter.addFilter(new HudsonMonitoringFilter());
	
	// TODO on pourrait ajouter un counter de nom job avec les temps de build
  }
}
