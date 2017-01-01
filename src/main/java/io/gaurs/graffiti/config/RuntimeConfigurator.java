package io.gaurs.graffiti.config;

import java.io.File;

/**
 * The runtime properties holder
 * 
 * @author gaurs
 *
 */
/**
 * @author gaurs
 *
 */
public class RuntimeConfigurator {

	/**
	 * Singleton INSTANCE
	 */
	private static final RuntimeConfigurator INSTANCE = new RuntimeConfigurator();

	private RuntimeConfigurator() {

	}

	public static RuntimeConfigurator getConfig() {
		return INSTANCE;
	}

	private String outputLocation;
	private String dotFilesLocation;
	private String cssFilesLocation;
	private String jsFilesLocation;
	private String pngFilesLocation;
	private String dotExecutableLocation;
	private String jarFile;

	// jar statistics placeholder
	private int classesCount;
	private int interfaceCount;
	private int abstractClassCount;

	// maven details placeholder
	private String version;
	private String artifactId;
	private String group;

	private String javaVersion;
	private int dependenciesCount;

	public String getOutputLocation() {
		return outputLocation;
	}

	public void setOutputLocation(String outputLocation) {
		this.outputLocation = outputLocation;
	}

	public String getJarFile() {
		return jarFile;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public int getDependenciesCount() {
		return dependenciesCount;
	}

	public void setDependenciesCount(int dependenciesCount) {
		this.dependenciesCount = dependenciesCount;
	}

	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	public String getDotFilesLocation() {
		return dotFilesLocation;
	}

	public void setDotFilesLocation(String dotFilesLocation) {
		this.dotFilesLocation = dotFilesLocation;
	}

	public String getCssFilesLocation() {
		return cssFilesLocation;
	}

	public void setCssFilesLocation(String cssFilesLocation) {
		this.cssFilesLocation = cssFilesLocation;
	}

	public String getDotExecutableLocation() {
		return dotExecutableLocation;
	}

	public void setDotExecutableLocation(String dotExecutableLocation) {
		this.dotExecutableLocation = dotExecutableLocation;
	}

	public String getPngFilesLocation() {
		return pngFilesLocation;
	}

	public void setPngFilesLocation(String pngFilesLocation) {
		this.pngFilesLocation = pngFilesLocation;
	}

	public void setJsFilesLocation(String jsFilesLocation) {
		this.jsFilesLocation = jsFilesLocation;
	}

	public String getJsFilesLocation() {
		return jsFilesLocation;
	}

	public int getClassesCount() {
		return classesCount;
	}

	public void setClassesCount(int classesCount) {
		this.classesCount = classesCount;
	}

	public int getInterfaceCount() {
		return interfaceCount;
	}

	public void setInterfaceCount(int interfaceCount) {
		this.interfaceCount = interfaceCount;
	}

	public int getAbstractClassCount() {
		return abstractClassCount;
	}

	public void setAbstractClassCount(int abstractClassCount) {
		this.abstractClassCount = abstractClassCount;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * This method loads the runtime configuration properties based on the input
	 * parameters received.
	 * 
	 * @param properties
	 */
	public void load(String[] properties) {

		// Jar file name
		File jarFile = new File(properties[0]);
		if (!jarFile.exists() || !jarFile.isFile()) {
			throw new IllegalArgumentException("Invalid jar file specified to analyse : " + properties[0]);
		} else {
			this.jarFile = properties[0];
		}

		// Check the output location
		String outputLocation = properties[1];

		//if output directory is not present, attempt to create the same
		File outputDir = new File(outputLocation);
		if ((outputDir.exists() && outputDir.isDirectory()) || (outputDir.mkdir())) {
			this.outputLocation = outputLocation;
		} else {
			throw new IllegalArgumentException("Invalid Output location");
		}

		// Create css directory
		cssFilesLocation = outputLocation + File.separator + "css";
		File css = new File(cssFilesLocation);

		if (!css.exists() && !css.mkdir()) {
			throw new IllegalArgumentException("Can not create css dir @ " + cssFilesLocation);
		}

		// Create js directory
		jsFilesLocation = outputLocation + File.separator + "js";
		File js = new File(jsFilesLocation);

		if (!js.exists() && !js.mkdir()) {
			throw new IllegalArgumentException("Can not create js dir @ " + jsFilesLocation);
		}

		// Create dot directory
		dotFilesLocation = outputLocation + File.separator + "dot";
		File dot = new File(dotFilesLocation);

		if (!dot.exists() && !dot.mkdir()) {
			throw new IllegalArgumentException("Can not create dot dir @ " + dotFilesLocation);
		}

		// Create png directory
		pngFilesLocation = outputLocation + File.separator + "images";
		File png = new File(pngFilesLocation);

		if (!png.exists() && !png.mkdir()) {
			throw new IllegalArgumentException("Can not create images dir @ " + pngFilesLocation);
		}

		// dot executable
		dotExecutableLocation = properties[2];
		File dotExe = new File(dotExecutableLocation);

		if (!dotExe.exists() || !dotExe.isFile() || !dotExe.canExecute()) {
			throw new IllegalArgumentException("Invalid dot file specified  : " + dotExecutableLocation);
		}
	}

}
