package io.gaurs.graffiti;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.LogConfigurator;
import io.gaurs.graffiti.config.RuntimeConfigurator;
import io.gaurs.graffiti.model.ComplexType;
import io.gaurs.graffiti.model.ComplexTypeCache;
import io.gaurs.graffiti.paint.ClassFileGenerator;
import io.gaurs.graffiti.paint.DotFileGenerator;
import io.gaurs.graffiti.paint.FileGenerator;
import io.gaurs.graffiti.paint.HtmlFileGenerator;
import io.gaurs.graffiti.paint.ImageGenerator;
import io.gaurs.graffiti.paint.IndexFileGenerator;

/**
 * @author gaurs
 */
@SuppressWarnings("all")
public class Runner {

	/**
	 * Used for analysis of classes; attributes; methods etc
	 */
	private final StructureAnalyzer structureAnalyser = new StructureAnalyzer();

	/**
	 * Used for parsing the jar file to prepare a list of all the
	 * classes/interfaces etc
	 */
	private final Parser parser = new Parser();

	/**
	 * Used for generating dot files. The output of this class is fed to
	 * graphviz api to generate image files
	 */
	private final FileGenerator dotFileGenerator = new DotFileGenerator();

	/**
	 * Used to call graphviz api with the dot files to generate corresponding
	 * image files.
	 */
	private final FileGenerator imageGenerator = new ImageGenerator();

	/**
	 * Used to generate .html page corresponding to every .class file
	 */
	private final FileGenerator complexTypeGenerator = new ClassFileGenerator();

	/**
	 * Used to generate index page containing an overview of the jar files. It
	 * also contains list of all the classes and acts as a starting point for
	 * the generated output files.
	 */
	private final FileGenerator indexFileGenerator = new IndexFileGenerator();

	
	public static void main(String[] args) {
		// Configure the logging
		LogConfigurator.configure();

		//check if number of arguments passed is valid
		if (null == args || args.length < 1) {
			Logger.error("Jar File name can not be null");
			return;
		} else {
			//if valid, configure the arguments 
			RuntimeConfigurator.getConfig().load(args);
		}

		Runner runner = new Runner();
		runner.run();
	}
	
	public static void start(String[] args) {
		// Configure the logging
		LogConfigurator.configure();

		// check if number of arguments passed is valid
		if (null == args || args.length < 1) {
			Logger.error("Jar File name can not be null");
			return;
		} else {
			// if valid, configure the arguments
			RuntimeConfigurator.getConfig().load(args);
		}

		Runner runner = new Runner();

		try {
			runner.run();
		} catch (Throwable exception) {
			Logger.error("Exception occurred while executing graffiti-core", exception);
			throw exception;
		}
	}
	
	

	private void run() {
		Logger.info("Initializing the jar parsing for : " + RuntimeConfigurator.getConfig().getJarFile());
		
		//fetch the jar file name
		String jarFileName = RuntimeConfigurator.getConfig().getJarFile();

		//start the parsing
		parser.parse(jarFileName);
		
		//start the analysis
		structureAnalyser.analyse(jarFileName);

		if (Level.DEBUG == Logger.getLevel()) {
			logClassStructure();
		}

		generateDotFiles();
		copyStyleSheets();
		copy404Page();
		generateImageFiles();
		generateHtmlPageForEachClass();
		generateIndexPage();
	}

	private void generateIndexPage() {
		Logger.info("Copying Index page to " + RuntimeConfigurator.getConfig().getOutputLocation());
		indexFileGenerator.paint();
	}

	private void copy404Page() {
		Logger.info("Copying 404 page to " + RuntimeConfigurator.getConfig().getOutputLocation());
		HtmlFileGenerator.copy404Page();
	}

	private void generateHtmlPageForEachClass() {
		Logger.info("Generating html pages for the individual classes");
		complexTypeGenerator.paint();
	}

	private void generateImageFiles() {
		Logger.info("Generating Image Files at : " + RuntimeConfigurator.getConfig().getPngFilesLocation());
		imageGenerator.paint();
	}

	private void copyStyleSheets() {
		Logger.info("Copying style sheets to " + RuntimeConfigurator.getConfig().getCssFilesLocation());
		HtmlFileGenerator.copyStyleSheets();
	}

	private void generateDotFiles() {
		Logger.info("Generating " + ComplexTypeCache.getComplexTypeCache().size() + " dot files");
		dotFileGenerator.paint();
	}

	/**
	 * Print the entire cache of ComplexTypes
	 */
	private void logClassStructure() {

		Iterator<Map.Entry<String, ComplexType>> cacheIterator = ComplexTypeCache.getComplexTypeCache().entrySet()
				.iterator();

		while (cacheIterator.hasNext()) {
			ComplexType complexType = cacheIterator.next().getValue();
			Logger.debug(complexType.getName() + " : " + complexType.getFullyQualifiedName());
			Iterator<Entry<String, ComplexType>> itr = complexType.getAttributes().entrySet().iterator();
			while (itr.hasNext()) {
				Entry<String, ComplexType> attribute = itr.next();
				Logger.debug(attribute.getKey() + " : " + attribute.getValue().getName() + " : "
						+ attribute.getValue().getFullyQualifiedName());
			}
		}
	}
}
