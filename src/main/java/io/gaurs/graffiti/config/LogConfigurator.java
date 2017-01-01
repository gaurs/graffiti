package io.gaurs.graffiti.config;

import java.util.Locale;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.writers.ConsoleWriter;

/**
 * @author gaurs
 *
 */
public class LogConfigurator {

	/**
	 * The method configures the logging level and pattern for the utility
	 */
	public static void configure() {
		Configurator.currentConfig().writer(new ConsoleWriter()).level(Level.INFO).locale(Locale.US)
				.formatPattern(
						"{date:yyyy-MM-dd HH:mm:ss} [{thread}] {class}.{method}() : {line} {level|indent=5}: {message|indent=4}")
				.maxStackTraceElements(-1).activate();
	}

}
