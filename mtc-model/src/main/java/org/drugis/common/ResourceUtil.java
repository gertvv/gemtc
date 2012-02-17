package org.drugis.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ResourceUtil {
	/**
	 * Read a UTF-8 encoded class resource file into a String.
	 * @param clazz The class the resource belongs to.
	 * @param path The (relative) resource path.
	 * @return A String containing the file's contents.
	 */
	public static String read(Class<?> clazz, String path) throws IOException {
		return read(clazz, path, "UTF-8");
	}
	
	/**
	 * Read a class resource file into a String.
	 * @param clazz The class the resource belongs to.
	 * @param path The (relative) resource path.
	 * @param encoding The file's encoding.
	 * @return A String containing the file's contents.
	 */
	public static String read(Class<?> clazz, String path, String encoding) throws IOException {
		InputStream is = clazz.getResourceAsStream(path);
		StringBuilder str = new StringBuilder();
		Reader reader = new InputStreamReader(is, encoding);
		char[] buffer = new char[2048];
		int read;
		while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
			str.append(buffer, 0, read);
		}
		reader.close();
		return str.toString();
	}
}
