package io.gaurs.graffiti.paint;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.RuntimeConfigurator;
import io.gaurs.graffiti.model.ComplexType;
import io.gaurs.graffiti.model.ComplexTypeCache;

/**
 * dot -Tcmapx -oorg.pmw.tinylog.Configuration.map -Tgif
 * -oorg.pmw.tinylog.Configuration.png org.pmw.tinylog.Configuration.dot
 * 
 * 
 * @author gaurs
 *
 */
public class DotFileGenerator implements FileGenerator {

	// The method initiates the process of generating dot files which will
	// become the input for graphviz api to generate directed graphs
	
	@Override
	public void paint() {
		
		//Get the output location for dot files
		String outputLocation = RuntimeConfigurator.getConfig().getDotFilesLocation();
		
		//for every entry in the complexType cache; generate a dot file
		ComplexTypeCache.getComplexTypeCache().entrySet().iterator().forEachRemaining(entry -> {
			generateDotFile(entry, outputLocation);
		});
	}

	/**
	 * Generate a dot file for the given entry @ outputLocation as denoted by
	 * the input parm outputLocation.
	 * <ol>
	 * <li>Get the tabular representation of the ComplexType</li>
	 * <li>Create an arrow from all of its attributes to their corresponding
	 * types</li>
	 * <li>get the tabular representation of the field's type</li>
	 * <li>The generated arrow will be from 2 to 3 itself</li>
	 * 
	 * @param entry
	 * @param outputLocation
	 */
	private void generateDotFile(Entry<String, ComplexType> entry, String outputLocation) {
		String fullyQualifiedClassName = entry.getKey();
		ComplexType complexType = entry.getValue();

		StringBuilder content = new StringBuilder();
		
		//populate the header denoting the color; shape; bgcolor etc attributes
		content.append(getHeader(fullyQualifiedClassName));

		// get a tabular representation of the class represented by current
		// complexType entry
		String tableView = getCurrentTableView(complexType);
		content.append(tableView);

		// For all the attributes which are part of the jar; append their
		// structure to the current graph for 1 level hierarchy (no arrows
		// generating from those)
		Map<String, ComplexType> attributes = complexType.getAttributes();

		if (null != attributes && !attributes.isEmpty()) {
			attributes.entrySet().iterator().forEachRemaining(relatedEntry -> {
				String relatdeTableData = getRelatedTableView(relatedEntry);
				if (null != relatdeTableData) {
					content.append(NEW_LINE);
					content.append(NEW_LINE);
					content.append(relatdeTableData);
				}
			});
		}

		content.append(NEW_LINE);
		
		//close the current dot file by appending the footer 
		content.append(closeGraph());
		
		//write the current dot file contents to the file
		writeToFile(fullyQualifiedClassName, outputLocation, content.toString(), ".dot");
	}

	/**
	 * Generates the RHS of the directed edge for every attribute
	 * 
	 * @param relatedEntry
	 * @return
	 */
	private String getRelatedTableView(Entry<String, ComplexType> relatedEntry) {
		ComplexType complexType = relatedEntry.getValue();
		StringBuilder content = null;
		if (null != complexType) {
			content = new StringBuilder();
			
			content.append("\"" + complexType.getFullyQualifiedName() + "\" [" + "label=<" + NEW_LINE
					+ "<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"#ffffff\">");
			content.append(NEW_LINE);

			content.append("	<TR><TD BGCOLOR=\"#8CB4F0\" ALIGN=\"CENTER\">" + complexType.getFullyQualifiedName()
					+ "</TD></TR>");
			content.append(NEW_LINE);

			Iterator<Map.Entry<String, ComplexType>> iterator = null;

			// Just create a row for every attribute of the RHS; we are not
			// generating any arrows starting from this table. The same will be
			// catered in its own .html file
			if (null != complexType.getAttributes()) {
				iterator = complexType.getAttributes().entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, ComplexType> entry = iterator.next();
					content.append("	<TR><TD PORT=\"" + entry.getKey() + "\" BGCOLOR=\"#E2EBF9\" ALIGN=\"LEFT\">"
							+ entry.getKey() + "</TD></TR>");
					content.append(NEW_LINE);
				}
			}
			content.append("</TABLE>>");
			content.append(NEW_LINE);
			
			// for primitives, arrays and generic types populate the link to 404
			// page; for others it will link to <fully_qualified_name>.html
			content.append("URL=\" " + getPageName(complexType) + ".html" + "\"");
			content.append(NEW_LINE);
			content.append("tooltip=\" " + complexType.getFullyQualifiedName() + "\"");
			content.append(NEW_LINE);
			content.append("];");
		}

		return null != content ? content.toString() : null;
	}

	/**
	 * The three indicators are set for anything not present in ComplexTypeCache.
	 * Now ComplexTypeCache will not have entry for :
	 * <ol>
	 * <li> Classes in other jar
	 * <li> Generic classes
	 * <li> Arrays
	 * <li>Primitives
	 * </ol>
	 * 
	 * @see StructureAnalyzer.populateFieldsData()
	 * 
	 * @param complexType
	 * @return
	 */
	private String getPageName(ComplexType complexType) {
		String name = null;
		if (complexType.isArray() || complexType.isGeneric() || complexType.isPrimitive()) {
			name = "404";
		} else {
			name = complexType.getFullyQualifiedName();
		}
		return name;
	}

	/**
	 * Generates a tabular representation of the current class denoted by
	 * current entry
	 * 
	 * @param complexType
	 * @return
	 */
	private String getCurrentTableView(ComplexType complexType) {
		StringBuilder content = new StringBuilder();

		content.append("\"" + complexType.getFullyQualifiedName() + "\" [" + "label=<" + NEW_LINE
				+ "<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"#ffffff\">");
		content.append(NEW_LINE);

		content.append("	<TR><TD BGCOLOR=\"#8CB4F0\" ALIGN=\"CENTER\">" + complexType.getFullyQualifiedName()
				+ "</TD></TR>");
		content.append(NEW_LINE);

		//create a row for every attribute in the class
		Iterator<Map.Entry<String, ComplexType>> iterator = null;
		if (null != complexType.getAttributes()) {
			iterator = complexType.getAttributes().entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<String, ComplexType> entry = iterator.next();
				
				// Concentrate on the port attribute; the same acts as the
				// starting point of the arrow
				content.append("	<TR><TD PORT=\"" + entry.getKey() + "\" BGCOLOR=\"#E2EBF9\" ALIGN=\"LEFT\">"
						+ entry.getKey() + "</TD></TR>");
				content.append(NEW_LINE);
			}
		}

		//close the table
		content.append("</TABLE>>");
		content.append(NEW_LINE);
		
		//Clicking on the current table/class will land on the same .html page
		content.append("URL=\" " + complexType.getFullyQualifiedName() + ".html" + "\"");
		content.append(NEW_LINE);
		content.append("tooltip=\" " + complexType.getFullyQualifiedName() + "\"");
		content.append(NEW_LINE);
		content.append("];");

		content.append(NEW_LINE);
		content.append(NEW_LINE);

		// print relations
		if (null != complexType.getAttributes()) {
			
			//Now draw arrows for all of the attributes
			String relations = generateRelations(complexType);
			content.append(relations);
		}

		content.append(NEW_LINE);
		return content.toString();
	}

	/**
	 * Generates directed edges starting from every attribute/port of current
	 * class to the corresponding target.
	 * 
	 * <pre>
	 * ex: "io.gaurs.graffiti.model.ComplexType":"fullyQualifiedName"->"class java.lang.String"
	 * </pre>
	 * 
	 * The target is generated as part of getRelatedTableView() method 
	 * 
	 * @param complexType
	 * @return
	 */
	private String generateRelations(ComplexType complexType) {
		Iterator<Map.Entry<String, ComplexType>> iterator = complexType.getAttributes().entrySet().iterator();
		StringBuilder content = new StringBuilder();

		while (iterator.hasNext()) {
			Map.Entry<String, ComplexType> entry = iterator.next();

			if (null != entry.getValue()) {

				// class name in quotes
				content.append("\"" + complexType.getFullyQualifiedName() + "\"");

				// colon
				content.append(":");

				// the port name in quotes
				content.append("\"" + entry.getKey() + "\"");

				// arrow symbol
				content.append("->");

				// directed edge destination
				content.append("\"" + entry.getValue().getFullyQualifiedName() + "\"");

				// new line for next entry
				content.append(NEW_LINE);
			}
		}

		return content.toString();

	}

	private Object closeGraph() {
		return "}";
	}

	private void writeToFile(String name, String outputLocation, String html, String extension) {
		File file = new File(outputLocation + File.separator + name + extension);

		try {
			file.createNewFile();
			FileUtils.writeStringToFile(file, html, "utf-8");

		} catch (IOException exception) {
			Logger.error("Exception occurred while writing to file : " + name, exception);
		}
	}

	private String getHeader(String name) {
		String header = "digraph \"" +name+"\" {" + NEW_LINE
				+ "	graph [" + NEW_LINE
				+ "		rankdir=\"LR\"" + NEW_LINE
				+ "		bgcolor=\"#ffffff\"" + NEW_LINE
				+ "		label=\"\\nGenerated by Graffiti (http://graffiti.gaurs.io)\"" + NEW_LINE
				+ "		labeljust=\"l\"" + NEW_LINE
				+ "		nodesep=\"0.18\"" + NEW_LINE
				+ "		ranksep=\"0.46\"" + NEW_LINE
				+ "		fontname=\"Helvetica\"" + NEW_LINE
				+ "		fontsize=\"11\"" + NEW_LINE
				+ "		];" + NEW_LINE
				+ " node [" + NEW_LINE
				+ "		fontname=\"Helvetica\"" + NEW_LINE
				+ "		fontsize=\"11\"" + NEW_LINE
				+ "		shape=\"plaintext\"" + NEW_LINE
				+ "];" + NEW_LINE
				+"edge [" + NEW_LINE
				+"arrowsize=\"0.8\"" + NEW_LINE
				+"];" + NEW_LINE + NEW_LINE; 
		
		return header;
	}
	
}
