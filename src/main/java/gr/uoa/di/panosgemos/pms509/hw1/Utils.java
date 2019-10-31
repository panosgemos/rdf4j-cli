package gr.uoa.di.panosgemos.pms509.hw1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {
	public static String readTextFile(String path) throws IOException {
		return new String(Files.readAllBytes(
					Paths.get(path)), StandardCharsets.UTF_8);
	}
}
