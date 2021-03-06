package net.coderbot.iris.shaderpack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ShaderPreprocessor {
	public static String process(Path shaderPath, String source) throws IOException {
		StringBuilder processed = new StringBuilder();

		for (String line : processInternal(shaderPath, source)) {
			processed.append(line);
			processed.append('\n');
		}

		return processed.toString();
	}

	private static List<String> processInternal(Path shaderPath, String source) throws IOException {
		List<String> lines = new ArrayList<>();

		// Match any valid newline sequence
		// https://stackoverflow.com/a/31060125
		for (String line: source.split("\\R")) {
			if (line.startsWith("#include ")) {
				try {
					lines.addAll(include(shaderPath, line));
				} catch (IOException e) {
					throw new IOException("Failed to read file from #include directive", e);
				}

				continue;
			}

			lines.add(line);
		}

		return lines;
	}

	private static List<String> include(Path shaderPath, String directive) throws IOException {
		// Remove the "#include " part so that we just have the file path
		String target = directive.substring("#include ".length()).trim();

		// Remove quotes if they're present
		// All include directives should have quotes, but I
		if (target.startsWith("\"")) {
			target = target.substring(1);
		}

		if (target.endsWith("\"")) {
			target = target.substring(0, target.length() - 1);
		}

		Path included = shaderPath.getParent().resolve(target);
		String source = readFile(included);

		return processInternal(included, source);
	}

	private static String readFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
