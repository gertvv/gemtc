/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
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

import java.io.InputStream;
import java.util.Properties;

public class AppInfo {
	private static final String APPNAMEFALLBACK = "GeMTC";
	public static final String APPVERSIONFALLBACK = "UNKNOWN";

	public static String getAppVersion() {
		return getProperty("version", APPVERSIONFALLBACK);
	}

	public static String getAppName() {
		return getProperty("name", APPNAMEFALLBACK);
	}

	private static String getProperty(String property, String fallback) {
		try {
			InputStream is = AppInfo.class.getResourceAsStream("/META-INF/maven/org.drugis/mtc-gui/pom.properties");
			Properties props = new Properties();
			props.load(is);
			return props.getProperty(property, fallback);
		} catch (Exception e) {
			
		}
		return fallback;
	}
}
