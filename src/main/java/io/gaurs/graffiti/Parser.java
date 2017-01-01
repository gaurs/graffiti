package io.gaurs.graffiti;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.RuntimeConfigurator;
import io.gaurs.graffiti.model.ComplexType;
import io.gaurs.graffiti.model.ComplexTypeCache;

/**
 * @author gaurs
 *
 */
public class Parser {

	/**
	 * Parse the jar file to get the detail of all the classes. This method then
	 * creates an instance of ComplexType for each of the found .class file and
	 * stores the same in cache.
	 * 
	 * @param jarFileName
	 * @return
	 */
	public void parse(String jarFileName) {
		JarFile jarFile = null;
		try {

			Logger.info("Parsing the jar file : " + jarFileName);

			// Load the jar file
			jarFile = new JarFile(jarFileName);

			// Get the jar file entries
			Enumeration<JarEntry> jarFileEntry = jarFile.entries();

			// For every entry
			while (jarFileEntry.hasMoreElements()) {
				JarEntry entry = jarFileEntry.nextElement();

				// check if it is pom file, if yes populate maven details
				if (entry.getName().contains("pom.xml")) {
					popoulateMavenDetails(jarFile.getInputStream(entry));
				}

				// If it is a directory or something other than a class file
				else if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
					continue;
				}

				// Get the fully qualified name of the class ex:
				// org.apache.commons.lang.StringUtils
				String fullyQualifiedName = entry.getName().substring(0, entry.getName().length() - 6);
				fullyQualifiedName = fullyQualifiedName.replace('/', '.');

				// Get the actual class name ex : StringUtils
				String className = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf(".") + 1);

				// Create an instance of ComplexType for each of the loaded
				// class
				ComplexType complexType = new ComplexType().setName(className)
						.setFullyQualifiedName(fullyQualifiedName);

				// Store the same in the cache; the key is fully qualified name
				// and value is the complex type;

				// name is not used as the key due to the obvious reason of
				// duplicate names in multiple packages
				ComplexTypeCache.getComplexTypeCache().put(fullyQualifiedName, complexType);
			}
			Logger.info("Parsing complete for jar : " + jarFileName);
		} catch (IOException exception) {
			Logger.error("Exception occurred while parsing the jar file", exception);
		} finally {
			try {
				if (null != jarFile) {
					jarFile.close();
				}
			} catch (IOException exception) {
				Logger.error("Exception occurred while closing the jar file", exception);
			}
		}
	}

	private void popoulateMavenDetails(InputStream stream) {
		try {
			Document pom = null;
			// if pom file can be parsed
			if (null != stream && null != (pom = Jsoup.parse(stream, "utf-8", ""))) {

				// get the groupid
				String groupId = pom.getElementsByTag("groupId").first().text();
				RuntimeConfigurator.getConfig().setGroup(groupId);

				// get the version
				String version = pom.getElementsByTag("version").first().text();
				RuntimeConfigurator.getConfig().setVersion(version);

				// artifact id
				String artifactId = pom.getElementsByTag("artifactId").first().text();
				RuntimeConfigurator.getConfig().setArtifactId(artifactId);

				// java.version
				if (null != pom.getElementsByTag("java.version") && !pom.getElementsByTag("java.version").isEmpty()) {
					String javaVersion = pom.getElementsByTag("java.version").first().text();
					RuntimeConfigurator.getConfig().setJavaVersion(javaVersion);
				}

				// list of dependencies
				int dependencies = pom.getElementsByTag("dependency").size();
				RuntimeConfigurator.getConfig().setDependenciesCount(dependencies);
			}

		} catch (IOException exception) {
			Logger.error("Exception occurred while parsing pom.xml");
		}
	}
}
