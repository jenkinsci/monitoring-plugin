// package net.bull.javamelody pour pouvoir compiler et exécuter avec les classes internes de JavaMelody
package net.bull.javamelody;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.servlet.ServletContext;
import net.bull.javamelody.Parameters;
import net.bull.javamelody.Counter;

// TODO temporaire (à supprimer après v1.17), pour nettoyage des fichiers rrd de monitoring http
// mais pas httpHitsRate.rrd, httpMeanTimes.rrd et httpSystemErrors.rrd
public class HttpMonitoringCleaner {

	public static void cleanIfFirstStart(ServletContext context) throws IOException {
		Parameters.initialize(context);
		final File storageDirectory = Parameters.getStorageDirectory(Parameters.getCurrentApplication());
		if (!storageDirectory.exists()) {
			storageDirectory.mkdirs();
		}
		final File httpMonitoringCleaned = new File(storageDirectory, "httpMonitoring.cleaned");
		if (!httpMonitoringCleaned.exists()) {
			// vide le counter http
			final Counter httpCounter = new Counter("http", null);
			httpCounter.setApplication(Parameters.getCurrentApplication());
			httpCounter.clear();
			httpCounter.writeToFile();
			
			// supprime les fichiers httpxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.rrd
			// (ceux vraiment nécessairent seront recréés)
			final FilenameFilter filenameFilter = new FilenameFilter() {
				/** {@inheritDoc} */
				public boolean accept(File dir, String name) {
					return name.length() > 30 && name.startsWith("http") && name.endsWith(".rrd");
				}
			};
			for (final File file : storageDirectory.listFiles(filenameFilter)) {
				file.delete();
			}
			httpMonitoringCleaned.createNewFile();
		}
	}
}