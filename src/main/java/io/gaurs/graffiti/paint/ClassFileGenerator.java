package io.gaurs.graffiti.paint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.RuntimeConfigurator;
import io.gaurs.graffiti.model.ComplexType;
import io.gaurs.graffiti.model.ComplexTypeCache;

public class ClassFileGenerator extends HtmlFileGenerator {

	@Override
	public void paint() {
		// Get the list of all the classes
		Iterator<Entry<String, ComplexType>> iterator = ComplexTypeCache.getComplexTypeCache().entrySet().iterator();
		iterator.forEachRemaining(entry -> paint(entry));
	}

	private void paint(Map.Entry<String, ComplexType> entry) {
		if (null != entry && null != entry.getValue()) {
			ComplexType complexType = entry.getValue();
			String className = complexType.getFullyQualifiedName();

			Logger.debug("Painting " + complexType.getFullyQualifiedName() + ".class");
			Document classTemplate = null;
			try {
				InputStream stream = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("template.html");

				classTemplate = Jsoup.parse(stream, "utf-8", "");

				// set the title
				setTitleName(classTemplate, className);

				// set the class name
				setClassName(classTemplate, className);

				// set attributes count
				setAttributeCount(classTemplate,
						null != complexType.getAttributes() ? complexType.getAttributes().size() : 0);

				// set methods count
				setMethodCount(classTemplate,
						null != complexType.getMethodDetails() ? complexType.getMethodDetails().size() : 0);

				// set the image
				setDependencyMatrix(classTemplate, className);

				// populate the list of attributes
				populateAttributeList(classTemplate, complexType.getAttributes());

				// populate the list of methods
				populateMethodsList(classTemplate, complexType.getMethodDetails());

				stream.close();

			} catch (IOException exception) {
				Logger.error("Exeption occurred while loading html template file : ", exception);
			}

			writeToFile(className, RuntimeConfigurator.getConfig().getOutputLocation(), classTemplate.html());
		}
	}

	private void setTitleName(Document classTemplate, String className) {
		Element title = classTemplate.getElementsByTag("title").first();
		title.text("Graffiti | " + className);
	}

	private void populateMethodsList(Document classTemplate, List<Method> methodDetails) {
		if (null != methodDetails && !methodDetails.isEmpty()) {
			Element attributeTable = classTemplate.getElementById("methods");
			Element tableBody = attributeTable.appendElement("tbody");
			methodDetails.iterator().forEachRemaining(entry -> addMethodRow(entry, tableBody));
		}
	}

	private void populateAttributeList(Document classTemplate, Map<String, ComplexType> attributes) {
		if (null != attributes && null != attributes.entrySet() && !attributes.entrySet().isEmpty()) {
			Element attributeTable = classTemplate.getElementById("attributes");
			Element tableBody = attributeTable.appendElement("tbody");
			attributes.entrySet().iterator().forEachRemaining(entry -> addAttributeRow(entry, tableBody));
		}
	}

	private void addMethodRow(Method method, Element tableBody) {
		Element row = tableBody.appendElement("tr");

		Element column = row.appendElement("td");
		column.attr("border", "1");
		if (Modifier.toString(method.getModifiers()).contains("public")) {
			row.attr("class", "success");
		} else if (Modifier.toString(method.getModifiers()).contains("private")) {
			row.attr("class", "danger");
		} else if (Modifier.toString(method.getModifiers()).contains("abstract")) {
			row.attr("class", "warning");
		}

		try {
			column.text(method.toGenericString());
		} catch (Throwable exception) {
			//The return type of the parameter can be a class/interface from another jar
			Logger.error("Exception occurred while loading method " + method.getName() + " of class " + method.getDeclaringClass().getName());
		}
	}

	private void addAttributeRow(Entry<String, ComplexType> entry, Element tableBody) {

		ComplexType complexType = entry.getValue();

		Element row = tableBody.appendElement("tr");

		Element firstColumn = row.appendElement("td");
		firstColumn.attr("border", "1");
		firstColumn.text(entry.getKey());

		Element secondColumn = row.appendElement("td");
		secondColumn.attr("border", "1");
		Element link = secondColumn.appendElement("a");

		String value = null;

		if (complexType.isPrimitive() || complexType.isArray()) {
			value = complexType.getName();
		} else {
			value = complexType.getFullyQualifiedName();
		}

		value = value.replaceAll("&lt;", "<");
		value = value.replaceAll("&gt;", ">");
		value = value.replaceAll("&quot;", "\"");

		// See also StructureAnalyser.populateFieldsData()
		if (complexType.isGeneric() || complexType.isPrimitive() || complexType.isArray()) {
			link.attr("href", "404.html");
		} else {
			link.attr("href", value + ".html");
		}

		link.text(value);
	}

	private void setDependencyMatrix(Document classTemplate, String className) {
		try {

			// Set the image
			Element currentClass = classTemplate.getElementById("dependencyMatrix");
			currentClass.attr("src", "images" + File.separator + className + ".png");
			currentClass.attr("USEMAP", "#" + className);

			// Set the usemap
			String src = RuntimeConfigurator.getConfig().getPngFilesLocation();
			File map = new File(src + File.separator + className + ".map");
			Document mapDetails;
			mapDetails = Jsoup.parse(map, "utf-8");
			Element mapElement = mapDetails.getElementById(className);

			currentClass.after(mapElement);

		} catch (IOException exception) {
			Logger.error("Exception occurred while populating dependency matrix : " + exception);
		}

	}

	private void setMethodCount(Document classTemplate, int count) {
		Element currentClass = classTemplate.getElementById("methodCount");
		currentClass.text(String.valueOf(count));
	}

	private void setAttributeCount(Document classTemplate, int count) {
		Element currentClass = classTemplate.getElementById("attributeCount");
		currentClass.text(String.valueOf(count));
	}

	private void setClassName(Document classTemplate, String className) {
		Elements currentClass = classTemplate.getElementsByAttributeValue("id", "currentClass");

		for (Element element : currentClass) {
			element.text(className);
		}
	}
}
