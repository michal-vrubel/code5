package cz.mv.code5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
	private static final Pattern RULE_PATTERN = Pattern.compile("^(\\d+)\\|(\\d+)$");

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

	private static final String ORDER_REGEX = "\\d+(,\\d+)+";

	public static void main(String[] args) throws IOException, URISyntaxException {
		String rulesFilePath = "./rules.txt";
		String pagesFilePath = "./pages.txt";
		
		for (String arg : args) {
		    if (arg.startsWith("--rules")) {
		        String[] parts = arg.substring(2).split("=", 2);
		        rulesFilePath = parts[1];
		    }
		    if (arg.startsWith("--pages")) {
		        String[] parts = arg.substring(2).split("=", 2);
		        pagesFilePath =parts[1];
		    }
		}

		File rulesFile = new File(rulesFilePath);
		File pagesFile = new File(pagesFilePath);

		if (!rulesFile.exists()) {
			System.err.println("Rules file does not exist: " + rulesFilePath);
			System.exit(1);
		}

		if (!pagesFile.exists()) {
			System.err.println("Pages file does not exist: " + pagesFilePath);
			System.exit(1);
		}

		TopoGraph topoGraph = new TopoGraph();

		loadRules(topoGraph, rulesFile);
		processPages(topoGraph, pagesFile);
	}

	private static void loadRules(TopoGraph topoGraph, File file) {
		try (
			InputStreamReader inputStreamReader = new InputStreamReader(
				new FileInputStream(file),
				StandardCharsets.UTF_8);
			BufferedReader reader = new BufferedReader(inputStreamReader);
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher matcher = RULE_PATTERN.matcher(line);

				if (matcher.matches()) {
					int from = Integer.parseInt(matcher.group(1));
					int to = Integer.parseInt(matcher.group(2));
					topoGraph.addRule(from, to);
				}
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to load rules", e);
		}
	}

	private static void processPages(TopoGraph topoGraph, File file) {
		try (
			InputStreamReader inputStreamReader = new InputStreamReader(
				new FileInputStream(file),
				StandardCharsets.UTF_8);
			BufferedReader reader = new BufferedReader(inputStreamReader);			
		) {
			long sumOfMiddle = 0L;

			String line;
			orders: while ((line = reader.readLine()) != null) {
				if (line.matches(ORDER_REGEX)) {

					Matcher matcher = NUMBER_PATTERN.matcher(line);

					Integer previous = null;
					int count = 0;

					Map<Integer, Integer> buffer = new HashMap<>();

					boolean isCorrect = true;

					while (matcher.find()) {
						count++;
						int current = Integer.parseInt(matcher.group());

						if (previous != null) {
							isCorrect = isCorrect && topoGraph.isCorrectOrder(previous, current);
							if (!isCorrect) {
								continue orders;
							}
						}

						buffer.put(2 * count - 1, current);
						buffer.remove(count - 1);

						previous = current;
					}

					int middleValue = buffer.get(count);
					sumOfMiddle += middleValue;

					System.out.printf("%s: %s, middle: %s%n", line, isCorrect ? "correct" : "incorrect", middleValue);
				}

				System.out.printf("Total: %s", sumOfMiddle);
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to process orders", e);
		}
	}
}
