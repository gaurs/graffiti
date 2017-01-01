package io.gaurs.graffiti.paint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.RuntimeConfigurator;
import io.gaurs.graffiti.model.ComplexType;
import io.gaurs.graffiti.model.ComplexTypeCache;

public class IndexFileGenerator extends HtmlFileGenerator {

	@Override
	public void paint() {
		Document indexPage = null;
		try {

			InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("index.html");
			indexPage = Jsoup.parse(stream, "utf-8", "");

			String jarName = RuntimeConfigurator.getConfig().getJarFile();
			jarName = jarName.substring(jarName.lastIndexOf(File.separator) + 1);

			// set the title
			setTitleName(indexPage, jarName);

			// set the label
			setLabel(indexPage, jarName);

			// populate classes count
			setVariousCounts(indexPage);

			// populate maven details
			populateMavenDetails(indexPage);

			// populate classes table
			populateClassesTable(indexPage);

			stream.close();

		} catch (IOException exception) {
			Logger.error("Exception occurred while loading index file ", exception);
		}

		writeToFile("index", RuntimeConfigurator.getConfig().getOutputLocation(), indexPage.html());
	}

	private void populateClassesTable(Document indexPage) {
		Element table = indexPage.getElementById("classNames");
		Element tableBody = table.appendElement("tbody");

		ComplexTypeCache.getComplexTypeCache().entrySet().iterator()
				.forEachRemaining(entry -> populateRow(entry.getValue(), tableBody));
	}

	private void populateRow(ComplexType complexType, Element tableBody) {
		Element row = tableBody.appendElement("tr");

		Element classNameColumn = row.appendElement("td");
		Element packageNameColumn = row.appendElement("td");

		classNameColumn.attr("border", "1");
		Element link = classNameColumn.appendElement("a");
		
		String value = complexType.getFullyQualifiedName();
		value = value.replaceAll("&lt;", "<");
		value = value.replaceAll("&gt;", ">");
		value = value.replaceAll("&quot;", "\"");
		
		// if complex type is generic type; the index page lists the
		// corresponding entry to 404
		if (complexType.isGeneric()) {
			link.attr("href", "404.html");
		} else {
			link.attr("href", value + ".html");
		}
		
		link.text(complexType.getName());

		String fullyQualifiedName = complexType.getFullyQualifiedName();
		Logger.debug("Populaiting row for : "+ fullyQualifiedName);
		
		
		//Handle classes in default package
		if(fullyQualifiedName.lastIndexOf(".") < 0){
			packageNameColumn.text("");
		}else{
			packageNameColumn.text(fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf(".")));
		}				

		packageNameColumn.attr("border", "1");

	}

	private void populateMavenDetails(Document indexPage) {

		RuntimeConfigurator config = RuntimeConfigurator.getConfig();

		Element group = indexPage.getElementById("group");
		group.text(null == config.getGroup() ? "maven prop not found" : config.getGroup());

		Element artifactId = indexPage.getElementById("artifactId");
		artifactId.text(null == config.getArtifactId() ? "maven prop not found" : config.getArtifactId());

		Element version = indexPage.getElementById("version");
		version.text(null == config.getVersion() ? "maven prop not found" : config.getVersion());

		Element javaVersion = indexPage.getElementById("javaVerion");
		javaVersion.text(null == config.getJavaVersion() ? "maven prop not found" : config.getJavaVersion());

		Element dependencyCount = indexPage.getElementById("dependencyCount");
		dependencyCount.text(String.valueOf(RuntimeConfigurator.getConfig().getDependenciesCount()));
	}

	private void setVariousCounts(Document indexPage) {
		Element classesCount = indexPage.getElementById("classesCount");
		classesCount.text(String.valueOf(RuntimeConfigurator.getConfig().getClassesCount()));

		Element interfaceCount = indexPage.getElementById("interfaceCount");
		interfaceCount.text(String.valueOf(RuntimeConfigurator.getConfig().getInterfaceCount()));

		Element abstractClassesCount = indexPage.getElementById("abstractClassCount");
		abstractClassesCount.text(String.valueOf(RuntimeConfigurator.getConfig().getAbstractClassCount()));
	}

	private void setLabel(Document indexPage, String jarName) {
		Element title = indexPage.getElementById("label");
		title.text(jarName);
	}

	private void setTitleName(Document indexPage, String jarName) {
		Element title = indexPage.getElementsByTag("title").first();
		title.text(jarName);
	}

}
