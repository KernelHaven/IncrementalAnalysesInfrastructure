package net.ssehub.kernel_haven.incremental.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerformanceEvaluator {
	

	/**
	 * Extract date from log line.
	 *
	 * @param logLine the log line
	 * @return the local date time
	 */
	private LocalDateTime extractDateFromLogLine(String logLine) {
		Pattern pattern = Pattern.compile("\\[(\\d{4}-\\d{2}-\\d{2}\\s{1}\\d{2}:\\d{2}:\\d{2})\\]");
		Matcher matcher = pattern.matcher(logLine);

		LocalDateTime time = null;

		if (matcher.find()) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			String timeString = matcher.group(1);
			time = LocalDateTime.parse(timeString, formatter);
		}
		return time;
	}
	
	/**
	 * Extract times.
	 *
	 * @param logFile the log file
	 * @param result the result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unused")
	private void extractTimes(File logFile, QualityResult result) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			String currentLine = br.readLine();
			LocalDateTime startTime = extractDateFromLogLine(currentLine);
			LocalDateTime currentTime = startTime;
			LocalDateTime startPreparationPhase = null;
			LocalDateTime finishPreparationPhase = null;
			LocalDateTime startExtractionPhase = null;
			LocalDateTime endExtractionPhase = null;
			LocalDateTime startAnalysisPhase = null;
			LocalDateTime endAnalysisPhase = null;
			LocalDateTime endTime = null;

			for (String nextLine; (nextLine = br.readLine()) != null;) {
				// Update the time to always reflect the most recent timestamp
				LocalDateTime timeFromCurrentLine = extractDateFromLogLine(currentLine);
				if (timeFromCurrentLine != null) {
					currentTime = timeFromCurrentLine;
				}

				if (currentLine.contains("[Setup] Running preparation")) {
					startPreparationPhase = currentTime;
				} else if (currentLine.contains("IncrementalPreparation duration:")) {
					finishPreparationPhase = currentTime;
				} else if (startExtractionPhase == null && currentLine.contains("ExtractorThread]")) {
					startExtractionPhase = currentTime;
				} else if (currentLine.contains("ExtractorThread] All threads done")) {
					endExtractionPhase = currentTime;
				} else if (startAnalysisPhase == null
						&& currentLine.contains("[info   ] [OrderPreservingParallelizer-Worker")) {
					startAnalysisPhase = currentTime;
				} else if (currentLine.contains("[info   ] [Setup] Analysis has finished")) {
					endAnalysisPhase = currentTime;
				}
				if (currentLine.matches(".*Analysis component .* done")) {
					Pattern componentPattern = Pattern.compile(".*Analysis component (.*) done");
					Matcher componentMatcher = componentPattern.matcher(currentLine);
					componentMatcher.find();
					String finishedComponent = componentMatcher.group(1);
					Pattern timePattern = Pattern.compile(".\\s*Execution took (\\d*)");
					Matcher timeMatcher = timePattern.matcher(nextLine);
					timeMatcher.find();
					long componentTime = Long.parseLong(timeMatcher.group(1));
					//result.addAnalysisComponentTime(finishedComponent, componentTime);
				}

				currentLine = nextLine;
			}
			endTime = currentTime;

		}

	}

}
