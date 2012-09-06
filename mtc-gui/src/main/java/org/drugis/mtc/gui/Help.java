package org.drugis.mtc.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Help {
	private static final String TEXT_NOT_FOUND_ERROR = "<p><b>Error: </b>Help text not found.</p>";
	private static final String HELP_FILE = "help.properties";
	private static Properties s_properties = null;

	public static String getHelpText(String key) {
		if (s_properties == null) {
			initialize();
		}
		return s_properties.getProperty(key, TEXT_NOT_FOUND_ERROR);
	}

	private static void initialize() {
		Properties properties = new Properties();
		try {
			InputStream is = Help.class.getResourceAsStream(HELP_FILE);
			properties.load(is);
			is.close();
		} catch (IOException e) {
			s_properties = properties;
			throw new RuntimeException("Could not initialize GeMTC help text.", e);
		}
		s_properties = properties;
	}
}
