/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

package org.drugis.mtc;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class DefaultModelFactory {
	/**
	 * Get the default ModelFactory, as configured in the property
	 * defaultModelFactory in the resource org/drugis/mtc/factory.properties.
	 * If no configuration is found, org.drugis.mtc.jags.JagsModelFactory is
	 * used.
	 */
	public static ModelFactory instance() {
		InputStream is =
			DefaultModelFactory.class.getResourceAsStream("factory.properties");
		String className = "org.drugis.mtc.yadas.YadasModelFactory";
		if (is != null) {
			try {
				Properties props = new Properties();
				props.load(is);
				is.close();

				className = props.getProperty("defaultModelFactory");
			} catch (IOException e) {
				// ignore
			}
		}

		return instance(className);
	}

	/**
	 * Get a ModelFactory by class name.
	 */
	public static ModelFactory instance(String className) {
		try {
			Class<? extends ModelFactory> cls =
				Class.forName(className).asSubclass(ModelFactory.class);
			return cls.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
