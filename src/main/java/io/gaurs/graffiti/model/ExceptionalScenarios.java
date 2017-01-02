package io.gaurs.graffiti.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.nodes.Element;
import org.pmw.tinylog.Logger;

public class ExceptionalScenarios extends ArrayList<Scenario> {

	private static final long serialVersionUID = 4875810391542050899L;

	private static final ExceptionalScenarios INSTANCE = new ExceptionalScenarios();

	public static ExceptionalScenarios getInstance() {
		return INSTANCE;
	}

	private ExceptionalScenarios() {

	}

	public Scenario createException(String className, String packageName, String comment) {
		Scenario scenario = new Scenario(className, packageName, comment);
		this.add(scenario);
		return scenario;
	}

	public Iterator<Scenario> getScenarios() {
		return this.iterator();
	}

	public void populate(Element tableBody) {
		this.forEach(scenario -> populateRow(scenario, tableBody));
	}

	private void populateRow(Scenario scenario, Element tableBody) {
		Element row = tableBody.appendElement("tr");

		Element classNameColumn = row.appendElement("td");
		Element packageNameColumn = row.appendElement("td");
		Element commentsColumn = row.appendElement("td");

		classNameColumn.attr("border", "1");
		packageNameColumn.attr("border", "1");
		commentsColumn.attr("border", "1");

		classNameColumn.text(scenario.getClassName());
		packageNameColumn.text(scenario.getPackageName());
		commentsColumn.text(scenario.getComment());
	}
}

class Scenario {
	private String className;
	private String packageName;
	private String comment;

	public Scenario(String className, String packageName, String comment) {
		super();
		this.className = className;
		this.packageName = packageName;
		this.comment = comment;
	}

	public String getClassName() {
		return className;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getComment() {
		return comment;
	}
}
