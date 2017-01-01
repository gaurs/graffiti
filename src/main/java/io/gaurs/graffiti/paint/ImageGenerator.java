package io.gaurs.graffiti.paint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import io.gaurs.graffiti.config.RuntimeConfigurator;

public class ImageGenerator implements FileGenerator {

	@Override
	public void paint() {
		String pathToDotFiles = RuntimeConfigurator.getConfig().getDotFilesLocation();
		Collection<File> dotFiles = FileUtils.listFiles(new File(pathToDotFiles), new String[] { "dot" }, true);

		Iterator<File> dotFileIterator = dotFiles.iterator();

		dotFileIterator.forEachRemaining(file -> generateImageFile(file));

	}

	private void generateImageFile(File dotFile) {
		String pngDirectory = RuntimeConfigurator.getConfig().getPngFilesLocation();

		try {
			String outputFileName = dotFile.getName();
			outputFileName = outputFileName.substring(0, outputFileName.length() - 4);
			outputFileName = pngDirectory + File.separator + outputFileName;

			String[] dotCommand = new String[] { RuntimeConfigurator.getConfig().getDotExecutableLocation(), "-Tpng",
					dotFile.getAbsolutePath(), "-o" + outputFileName + ".png", "-Tcmapx" };

			Process process = Runtime.getRuntime().exec(dotCommand);

			InputStream consoleOutput = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(consoleOutput));

			String line = null;
			StringBuilder map = new StringBuilder();
			while (null != (line = reader.readLine())) {
				map.append(line);
				map.append(NEW_LINE);
			}

			int response = process.waitFor();

			if (response != 0) {
				Logger.error("Exception occurred while generating image file for : " + dotFile.getAbsolutePath());
				File imageFile = new File(outputFileName + ".png");
				if (imageFile.exists()) {
					imageFile.delete();
				}
			} else {
				FileUtils.writeStringToFile(new File(outputFileName + ".map"), map.toString(), "utf-8");
			}
		} catch (IOException | InterruptedException exception) {
			Logger.error("Exception occurred while generating image file for : " + dotFile.getAbsolutePath(),
					exception);
		}
	}

}
