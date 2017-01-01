package io.gaurs.graffiti;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.RuntimeConfigurator;
import io.gaurs.graffiti.model.ComplexType;
import io.gaurs.graffiti.model.ComplexTypeCache;
import io.gaurs.graffiti.paint.ClassFileGenerator;

/**
 * Populates the jar details in the ComplexType data Structure. For the
 * following types a 404 page is populated :
 * <ol>
 * <li>Arrays</li>
 * <li>Generic Types</li>
 * <li>Primitive Types</li>
 * </ol>
 * 
 * @author gaurs
 */
public class StructureAnalyzer {

	/**
	 * @param jarFileName
	 * @return
	 */
	public void analyse(String jarFileName) {

		try {
			Logger.info("Analysing jar file " + jarFileName);

			// load the jar file
			URL[] urls = { new URL("jar:file:" + jarFileName + "!/") };

			// get the class loader corresponding to the jar file
			URLClassLoader classLoader = URLClassLoader.newInstance(urls);

			// Get all the entries that we need to load from the jar file; The
			// list was prepared as part of the parsing process in which for
			// every .class file a new complex type was created. The
			// ComplexTypeCache holds all those instances with the key being the
			// fully qualified name an the value being the ComplexTypeInstance
			Iterator<Map.Entry<String, ComplexType>> cacheIterator = ComplexTypeCache.getComplexTypeCache().entrySet()
					.iterator();

			// For every entry in the cache
			while (cacheIterator.hasNext()) {

				// Fetch the entry
				ComplexType complexType = cacheIterator.next().getValue();
				Logger.debug("Loading class : " + complexType.getFullyQualifiedName());

				try {

					// load the class using the classloader created for this jar
					Class<?> loadedClass = classLoader.loadClass(complexType.getFullyQualifiedName());

					// populate various counts like :
					// classCount/interfaceCount/AbstractCount etc
					populateCounts(loadedClass);

					// get the list of declared fields
					Field[] fields = loadedClass.getDeclaredFields();

					// get the list of declared methods
					Method[] methods = loadedClass.getDeclaredMethods();

					if (null != fields && fields.length >= 0) {
						Logger.debug(
								"Class " + complexType.getFullyQualifiedName() + " has " + fields.length + " fields");
						// populate the fields data
						populateFieldsData(fields, complexType);
					}

					if (null != methods && methods.length >= 0) {
						Logger.debug(
								"Class " + complexType.getFullyQualifiedName() + " has " + methods.length + " methods");
						// populate the methods data
						populateMethodsData(methods, complexType);
					}
				} catch (Throwable exception) {
					Logger.error("Exception occurred while parsing : " + complexType.getFullyQualifiedName());
					cacheIterator.remove();
					continue;
				}
			}

			Logger.info("Analysis complete for " + jarFileName);
		} catch (MalformedURLException exception) {
			Logger.error("Exception occurred while loading the classes from the jar file", exception);
		}

	}

	/**
	 * Depenending upon the type of current entry, increment the corresponding
	 * count
	 * 
	 * @param loadedClass
	 */
	private void populateCounts(Class<?> loadedClass) {
		if (Modifier.isInterface(loadedClass.getModifiers())) {
			RuntimeConfigurator.getConfig().setInterfaceCount(RuntimeConfigurator.getConfig().getInterfaceCount() + 1);
		} else if (Modifier.isAbstract(loadedClass.getModifiers())) {
			RuntimeConfigurator.getConfig()
					.setAbstractClassCount(RuntimeConfigurator.getConfig().getAbstractClassCount() + 1);
		} else {
			RuntimeConfigurator.getConfig().setClassesCount(RuntimeConfigurator.getConfig().getClassesCount() + 1);
		}
	}

	private void populateMethodsData(Method[] methods, ComplexType complexType) {
		List<Method> methodDetails = new ArrayList<>();
		// For every method
		for (Method method : methods) {

			// Add the same to the list of methods which will be used in
			// ClassFileGenerator.populateMethodsList()
			methodDetails.add(method);
		}

		complexType.setMethodDetails(methodDetails);
	}

	/**
	 * Populate the fields data. The following use cases are handled with a 404
	 * page:
	 * <ol>
	 * <li>primitive field</li>
	 * <li>non-primitive field</li>
	 * <li>genric type field</li>
	 * </ol>
	 * 
	 * @see ClassFileGenerator
	 * @param fields
	 * @param complexType
	 */
	private void populateFieldsData(Field[] fields, ComplexType complexType) {
		HashMap<String, ComplexType> attributes = new HashMap<>();

		// for every field
		for (Field field : fields) {
			if (!field.getType().isPrimitive()) {
				populateNonPrimitiveFieldsData(field, attributes);
			} else {
				populatePrimitiveFieldsData(field, attributes);
			}
			complexType.setAttributes(attributes);
		}
	}

	private void populatePrimitiveFieldsData(Field field, HashMap<String, ComplexType> attributes) {
		// If it is a primitive type field, the same will not be available in
		// the ComplexTypeCache as it is populated ONLY with ComplexType
		// instances for every .class file found in the jar

		// The same will be used only on the individual .html files
		// corresponding to every class and not on the index .html file as it
		// displays the classes only and not class specific details

		// The ClassFileGenerator -> addAttributeRow() is handeled accordingly
		// with a 404 page
		ComplexType complexType = new ComplexType();
		complexType.setPrimitive(true);
		complexType.setName(field.getType().getName());
		complexType.setFullyQualifiedName(field.getType().getName());

		attributes.put(field.getName(), complexType);
	}

	/**
	 * @param field
	 * @param attributes
	 */
	private void populateNonPrimitiveFieldsData(Field field, HashMap<String, ComplexType> attributes) {
		// Fetch the ComplexType corresponding to the field type :
		// 1. Get the fieldTyp -> name
		// 2. Query the complexTypeCache with the fieldTypeName; The same can be
		// unavailable in two scenarios :
		// 2.a Field is of a type which is present in some other jar
		// 2.b Field is a generic class type
		// 2.c Field is an array of some class which may or may not be present
		// in the jar file

		ComplexType complexType = ComplexTypeCache.getComplexTypeCache().get(field.getType().getName());

		// TODO Better handling of generic types; The same must not be
		// added to ComplexTypeCache as no .html file is to be generated
		// corresponding to (arrays or generic classes)

		// As of now both IndexFileGenerator -> populateRow and
		// ClassFileGenerator -> addAttributeRow() are handled accordingly
		if (null == complexType) {
			complexType = generateComplexType(field.getType().getName());

			// Handles the scenario when the field is an array
			if (field.getType().isArray()) {
				complexType.setArray(true);
				complexType.setName(field.getType().getSimpleName());
				complexType.setFullyQualifiedName(field.getType().getSimpleName());
			} else {

				// Handles the scenario when the field is a generic field or
				// anything outside the current jar(becuase in both the cases a
				// 404 needs to be published as the same must not have been
				// initialized during the parsing phase)
				complexType.setGeneric(true);

				String genericName = field.getGenericType().toString();
				// class with a space ex: class java.lang.String
				genericName = genericName.replaceAll("class ", "");
				genericName = genericName.replaceAll("<", "&lt;");
				genericName = genericName.replaceAll(">", "&gt;");
				complexType.setFullyQualifiedName(genericName);
			}
		}

		// FieldName *..1 FieldType
		attributes.put(field.getName(), complexType);

	}

	private ComplexType generateComplexType(String name) {
		ComplexType complexType = new ComplexType();

		complexType.setAttributes(new HashMap<>());
		complexType.setMethodDetails(new ArrayList<>());

		complexType.setName(name);
		return complexType;
	}
}
