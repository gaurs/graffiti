package io.gaurs.graffiti.model;

import java.util.HashMap;

public class ComplexTypeCache extends HashMap<String, ComplexType> {

	private static final long serialVersionUID = -1085025032517774903L;

	private static final ComplexTypeCache INSTANCE = new ComplexTypeCache();

	private ComplexTypeCache() {

	}

	public static ComplexTypeCache getComplexTypeCache() {
		return INSTANCE;
	}
}
