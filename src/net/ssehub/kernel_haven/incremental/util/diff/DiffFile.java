import net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer;
	public DiffFile(@NonNull DiffAnalyzer analyzer) throws IOException {
		this.changeSet = analyzer.parse();
	public Collection<FileEntry> getEntries() {
		return changeSet;