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
