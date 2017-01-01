package io.gaurs.graffiti.model;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ComplexType {
	private String name;
	private String fullyQualifiedName;

	private boolean isPrimitive;
	private boolean isGeneric;
	private boolean isArray;

	private Map<String, ComplexType> attributes = null;

	private List<Method> methodDetails = null;

	public List<Method> getMethodDetails() {
		return methodDetails;
	}

	public void setMethodDetails(List<Method> methodDetails) {
		this.methodDetails = methodDetails;
	}

	public String getName() {
		return name;
	}

	public ComplexType setName(String name) {
		this.name = name;
		return this;
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public ComplexType setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
		return this;
	}

	public Map<String, ComplexType> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, ComplexType> attributes) {
		this.attributes = attributes;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public void setPrimitive(boolean isPrimitive) {
		this.isPrimitive = isPrimitive;
	}

	public boolean isGeneric() {
		return isGeneric;
	}

	public void setGeneric(boolean isGeneric) {
		this.isGeneric = isGeneric;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}
}
