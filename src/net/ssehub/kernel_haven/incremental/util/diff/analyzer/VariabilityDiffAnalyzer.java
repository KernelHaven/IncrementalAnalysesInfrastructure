import net.ssehub.kernel_haven.incremental.util.diff.FileEntry.VariabilityChange;
			String currentLine = null;
			String nextLine = null;
			for (String line : lines) {
				currentLine = nextLine;
				nextLine = line;
				if (currentLine != null) {
					if (currentLine.startsWith(DIFF_START_PATTERN)) {
						filePath = currentLine.substring(currentLine.indexOf("a/") + "a/".length(),
								currentLine.indexOf(" b/"));
						LOGGER.logDebug("Analyzing commit entry for file " + filePath.toString());
						if (nextLine.startsWith("new file mode")) {
							type = FileEntry.Type.ADDITION;
							break;
						} else if (nextLine.startsWith("deleted file mode")) {
							type = FileEntry.Type.DELETION;
							break;
						} else {
							type = FileEntry.Type.MODIFICATION;
							break;
						}
			} else {
				// When the ComAn-Logic fails to determine the type of change it is better to
				// risk a false positive as this
				// should always result in a correct analysis of the artifacts within the
				// incremental infrastructure.
				// However false positives result in a more costly analysis.
				LOGGER.logWarning("variability change type could not be determined for enty: " + filePath,
						"Marking entry as " + VariabilityChange.CHANGE + " to ensure a correct extraction of the model.");