package com.github.motyka.textprocessor.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceUtils {
	public static String readResource(String resourceName) {
		URL url = ResourceUtils.class.getClassLoader().getResource(resourceName);
		try {
			return new String(Files.readAllBytes(Paths.get(url.toURI())));
		} catch (IOException | URISyntaxException e) {
			return null;
		}
	}
}
