package io.gaurs.graffiti.paint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.RuntimeConfigurator;

public abstract class HtmlFileGenerator implements FileGenerator {

	public static void copyStyleSheets() {
		InputStream bootstrapStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("css" + File.separator + "bootstrap.css");
		InputStream bootstrapMapStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("css" + File.separator + "bootstrap.css.map");
		InputStream bootstrapMinStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("css" + File.separator + "bootstrap.min.css");
		InputStream bootstrapMinMapStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("css" + File.separator + "bootstrap.min.css.map");

		InputStream bootstrapJs = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("js" + File.separator + "bootstrap.js");
		InputStream bootstrapMinJs = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("js" + File.separator + "bootstrap.min.js");

		String outputCss = RuntimeConfigurator.getConfig().getCssFilesLocation();
		String outputJs = RuntimeConfigurator.getConfig().getJsFilesLocation();
		try {

			if (null != bootstrapStream && null != bootstrapMapStream && null != bootstrapMinStream
					&& null != bootstrapMinMapStream && null != bootstrapJs && null != bootstrapMinJs) {

				FileUtils.copyInputStreamToFile(bootstrapStream,
						new File(outputCss + File.separator + "bootstrap.css"));
				FileUtils.copyInputStreamToFile(bootstrapMapStream,
						new File(outputCss + File.separator + "bootstrap.css.map"));
				FileUtils.copyInputStreamToFile(bootstrapMinStream,
						new File(outputCss + File.separator + "bootstrap.min.css"));
				FileUtils.copyInputStreamToFile(bootstrapMinMapStream,
						new File(outputCss + File.separator + "bootstrap.min.css.map"));

				FileUtils.copyInputStreamToFile(bootstrapJs, new File(outputJs + File.separator + "bootstrap.js"));
				FileUtils.copyInputStreamToFile(bootstrapMinJs,
						new File(outputJs + File.separator + "bootstrap.min.js"));
			} else {
				Logger.error("Could not load style sheets");
			}
		} catch (IOException exception) {
			Logger.error("Exception occurred while copying css/js files to the directory : " + outputCss
					+ File.separator + outputJs, exception);
		} finally {
			try {
				if (null != bootstrapStream && null != bootstrapMapStream && null != bootstrapMinStream
						&& null != bootstrapMinMapStream && null != bootstrapJs && null != bootstrapMinJs) {
					bootstrapStream.close();
					bootstrapMapStream.close();
					bootstrapMinStream.close();
					bootstrapMinMapStream.close();
				} else {
					Logger.error("Could not load style sheets");
				}
			} catch (IOException exception) {
				Logger.error("Exception occurred while copying css/js files to the directory : " + outputCss
						+ File.separator + outputJs, exception);
			}
		}
	}

	public void writeToFile(String name, String outputLocation, String htmlContent) {
		File file = new File(outputLocation + File.separator + name + ".html");
		try {
			file.createNewFile();
			FileUtils.writeStringToFile(file, htmlContent, "utf-8");

		} catch (IOException exception) {
			Logger.error("Exception occurred while writing to file : " + name, exception);
		}
	}

	public static void copy404Page() {
		InputStream bootstrapStream = null;
		try {
			bootstrapStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("404.html");
			FileUtils.copyInputStreamToFile(bootstrapStream,
					new File(RuntimeConfigurator.getConfig().getOutputLocation() + File.separator + "404.html"));
		} catch (IOException exception) {
			Logger.error("Exception occurred while copying 404 page to : "
					+ RuntimeConfigurator.getConfig().getOutputLocation());
		} finally {
			try {
				bootstrapStream.close();
			} catch (IOException exception) {
				Logger.error("Exception occurred while copying 404 page to : "
						+ RuntimeConfigurator.getConfig().getOutputLocation(), exception);
			}
		}
	}
}
