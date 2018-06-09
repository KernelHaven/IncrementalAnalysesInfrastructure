package net.ssehub.kernel_haven.incremental.evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import net.ssehub.kernel_haven.util.Logger;

public class Evaluator {

	public static final Path LOG_INCREMENTAL_DIR = Paths.get("log/incremental");
	public static final Path LOG_REFERENCE_DIR = Paths.get("log/reference");
	public static final Path RESULTS_INCREMENTAL_DIR = Paths.get("output/incremental");
	public static final Path RESULTS_REFERENCE_DIR = Paths.get("output/reference");
	private Path baseDir;

	private Map<String, Result> incrementalResults = new HashMap<String, Result>();
	private Map<String, Result> referenceResults = new HashMap<String, Result>();

	private static final Logger LOGGER = Logger.get();
	
	public Evaluator(Path path) {
		this.baseDir = path;

	}

	public static void main(String[] args) throws IOException {
		Path baseDir = Paths.get("/home/moritz/Schreibtisch/results-variability-change/");
		if (args.length == 1) {
			baseDir = Paths.get(args[0]);
		}

		if (baseDir != null) {
			Evaluator evaluator = new Evaluator(baseDir);
			List<String> extractedDiffFilenames = evaluator.extractDiffFilenamesFromReferenceResults();
			Collections.sort(extractedDiffFilenames);

			for (int i = 0; i < extractedDiffFilenames.size(); i++) {
				if (i == 0) {
					evaluator.compareForInputDiffName(extractedDiffFilenames.get(i), null);

				} else {
					evaluator.compareForInputDiffName(extractedDiffFilenames.get(i), extractedDiffFilenames.get(i - 1));
				}

			}
		}

	}

	public List<String> extractDiffFilenamesFromReferenceResults() {
		List<String> diffFileNames = new ArrayList<String>();
		for (File file : baseDir.resolve(RESULTS_REFERENCE_DIR).toFile().listFiles()) {
			diffFileNames.add(file.getName().substring("output-".length(), file.getName().length() - ".csv".length()));
		}

		return diffFileNames;
	}

	public void compareForInputDiffName(String diffFileName, String previousDiffFileName) throws IOException {

		Result referenceResult = new Result(diffFileName);
		Result incrementalResult = new Result(diffFileName);

		File previousReferenceOutputFile = null;

		if (previousDiffFileName != null) {
			previousReferenceOutputFile = baseDir.resolve(RESULTS_REFERENCE_DIR)
					.resolve("output-" + previousDiffFileName + ".csv").toFile();
		}

		File referenceOutputFile = baseDir.resolve(RESULTS_REFERENCE_DIR).resolve("output-" + diffFileName + ".csv")
				.toFile();
		File incrementalResultFile = baseDir.resolve(RESULTS_INCREMENTAL_DIR).resolve("output-" + diffFileName + ".csv")
				.toFile();

		referenceResult.setResultQuality(Result.ResultQuality.BASELINE);
		incrementalResult.setResultQuality(Result.ResultQuality.DIFFERENT);

		if (contentIdentical(referenceOutputFile, incrementalResultFile)) {
			incrementalResult.setResultQuality(Result.ResultQuality.SAME);
			LOGGER.logInfo("Marked " + incrementalResult.getResultFileName() + " as SAME");
		} else if (contentEquivalent(referenceOutputFile, previousReferenceOutputFile, incrementalResultFile)) {
			incrementalResult.setResultQuality(Result.ResultQuality.EQUIVALENT);
			LOGGER.logInfo("Marked " + incrementalResult.getResultFileName() + " as EQUIVALENT");
		} else {
			LOGGER.logInfo("Marked " + incrementalResult.getResultFileName() + " as DIFFERENT");
		}

		incrementalResults.put(diffFileName, incrementalResult);
		referenceResults.put(diffFileName, incrementalResult);

	}

	private List<String> reduceToFileName(Collection<String> listOfResults) {
		List<String> newList = new ArrayList<String>();
		for (String entry : listOfResults) {
			String[] entryParts = entry.split(";");
			if (entryParts.length == 5) {
				entry = entryParts[0] + ";" + entryParts[1] + ";" + entryParts[4];
			}
			newList.add(entry);
		}
		return newList;
	}

	private boolean contentIdentical(File referenceResult, File incrementalResult) throws IOException {
		List<String> referenceLines = reduceToFileName(Files.readAllLines(referenceResult.toPath()));
		List<String> incrementalLines = reduceToFileName(Files.readAllLines(incrementalResult.toPath()));

		return referenceLines.containsAll(incrementalLines) && incrementalLines.containsAll(referenceLines);
	}

	private boolean contentEquivalent(File referenceResult, File previousReferenceResult, File incrementalResult)
			throws IOException {

		List<String> referenceLines = reduceToFileName(Files.readAllLines(referenceResult.toPath()));
		List<String> previousReferenceLines = null;
		if (previousReferenceResult != null) {
			previousReferenceLines = reduceToFileName(Files.readAllLines(previousReferenceResult.toPath()));
		}

		List<String> incrementalLines = reduceToFileName(Files.readAllLines(incrementalResult.toPath()));

		// first make sure that the result of the reference analysis contains all
		// entries that the incremental analysis produced
		boolean isEquivalent = referenceLines.containsAll(incrementalLines);

		if (isEquivalent) {
			// check if the result of the incremental analysis covers all lines that changed
			// within the reference analysis compared to its predecessor
			List<String> referenceChanges = new ArrayList<String>(referenceLines);
			if (previousReferenceLines != null) {
				referenceChanges.removeAll(previousReferenceLines);
			}
			isEquivalent = incrementalLines.containsAll(referenceChanges);
			if (!isEquivalent) {
				List<String> referenceWithoutIncrementalLines = new ArrayList<String>(referenceChanges);
				referenceWithoutIncrementalLines.removeAll(incrementalLines);
				StringJoiner joiner = new StringJoiner("\n");
				referenceWithoutIncrementalLines.forEach(line -> joiner.add(line));
				LOGGER.logInfo("Results in reference analysis for " + referenceResult.getName()
				+ " contained new results (compared to the previous reference) that were not present for the incremental result : ", joiner.toString());
			}
		} else {
			List<String> incrementalWithoutRefLines = new ArrayList<String>(incrementalLines);
			incrementalWithoutRefLines.removeAll(referenceLines);
			StringJoiner joiner = new StringJoiner("\n");
			incrementalWithoutRefLines.forEach(line -> joiner.add(line));
			LOGGER.logInfo("Results in incremental analysis for " + referenceResult.getName()
			+ " contained results that were not present for the reference : ", joiner.toString());
		}

		return isEquivalent;
	}

}
