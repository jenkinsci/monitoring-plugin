package org.jvnet.hudson.plugins.monitoring;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.util.PluginServletFilter;
import javax.servlet.ServletContext;

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
  private ServletContext context;

  @Override
  public void setServletContext(ServletContext context) {
    this.context = context;
  }
  
  @Override
  public void start() throws Exception {
    super.start();
	
	// on active les actions systemes (gc, heap dump, histogramme memoire, processus...), sauf si l'administrateur a dit differemment
	if (System.getProperty("javamelody.system-actions-enabled") == null) {
		System.setProperty("javamelody.system-actions-enabled", "true");
	}
	// on desactive les graphiques jdbc et statistiques sql puisqu'il n'y en aura pas
	if (System.getProperty("javamelody.no-database") == null) {
		System.setProperty("javamelody.no-database", "true");
	}
	// le repertoire de stockage est dans le repertoire de hudson au lieu d'etre dans le repertoire temporaire
	// ("/" initial necessaire sous windows pour javamelody v1.8.1)
	if (System.getProperty("javamelody.storage-directory") == null) {
		System.setProperty("javamelody.storage-directory", "/" + new File(Hudson.getInstance().getRootDir(),"monitoring").getAbsolutePath());
	}
	// google-analytics pour connaitre le nombre d'installations actives et pour connaitre les fonctions les plus utilisees
	if (System.getProperty("javamelody.analytics-id") == null) {
		System.setProperty("javamelody.analytics-id", "UA-1335263-7");
	}
	// http-transform-pattern pour aggréger les requêtes contenant des parties "dynamiques" comme des numeros des builds,
	// les fichiers dans job/<name>/site/, javadoc/, ws/, cobertura/, testReport/, violations/file/
	// ou les utilisateurs dans user/ ou les fichiers dans /static/abcdef123/
	if (System.getProperty("javamelody.http-transform-pattern") == null) {
		System.setProperty("javamelody.http-transform-pattern", "/\\d+/|/site/.+|avadoc/.+|/ws/.+|obertura/.+|estReport/.+|iolations/file/.+|/user/.+|/static/\\w+/");
	}
	
	PluginServletFilter.addFilter(new HudsonMonitoringFilter());
	
	// TODO on pourrait ajouter un counter avec les temps de build
  }
}
