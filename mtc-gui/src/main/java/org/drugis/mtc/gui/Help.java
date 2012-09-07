/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
