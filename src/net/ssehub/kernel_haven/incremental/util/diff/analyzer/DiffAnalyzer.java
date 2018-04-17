package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.IOException;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;

public abstract class DiffAnalyzer {


	public abstract Collection<FileEntry> parse() throws IOException;

}
