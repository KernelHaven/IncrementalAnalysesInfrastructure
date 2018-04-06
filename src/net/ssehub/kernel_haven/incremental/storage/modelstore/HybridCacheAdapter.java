package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.IOException;
import java.util.Collection;

import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;

/**
 * Special adapter class to enable any pipeline-analysis to run as an incremental analysis.
 * 
 * Usage Example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * HybridCacheAdapter hca = new HybridCacheAdapter(config,
 * 		new IncrementalPostExtraction(config, getCmComponent(), getBmComponent(), getVmComponent()));
 *
 * DeadCodeFinder dcf = new DeadCodeFinder(config, hca.getVmComponent(), hca.getBmComponent(), hca.getCmComponent());
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * @author Moritz
 */
public final class HybridCacheAdapter extends AnalysisComponent<Void> {

	private @NonNull Configuration config;

	private @NonNull AnalysisComponent<HybridCache> inputComponent;

	private OutputComponent<BuildModel> bmComponent;
	private OutputComponent<VariabilityModel> vmComponent;
	private OutputComponent<SourceFile> cmComponent;

	/**
	 * Creates this double analysis component with the given input component.
	 * 
	 * @param config
	 *            The global configuration.
	 * @param inputComponent
	 *            The component to get the results to pass to both other components.
	 */
	public HybridCacheAdapter(@NonNull Configuration config, @NonNull AnalysisComponent<HybridCache> inputComponent) {
		super(config);
		this.config = config;
		this.inputComponent = inputComponent;
	}

	@Override
	protected void execute() {
		HybridCache data;
		
		if ((data = inputComponent.getNextResult()) != null) {
			try {
				// Get models
				Collection<SourceFile> codeModel = data.getCodeModel();
				BuildModel buildModel = data.readBm();
				VariabilityModel varModel = data.readVm();

				// add Models to components
				for (SourceFile srcFile : codeModel) {
					cmComponent.myAddResult(srcFile);
				}
				if (buildModel != null) {
					bmComponent.myAddResult(buildModel);
				}

				if (varModel != null) {
					vmComponent.myAddResult(varModel);
				}
			} catch (IOException | FormatException e) {
				LOGGER.logException("Could not get code model from HybridCache", e);
			}
		}

		bmComponent.done = true;
		synchronized (bmComponent) {
			bmComponent.notifyAll();
		}

		vmComponent.done = true;
		synchronized (bmComponent) {
			bmComponent.notifyAll();
		}

		cmComponent.done = true;
		synchronized (bmComponent) {
			bmComponent.notifyAll();
		}

	}

	@Override
	public @NonNull String getResultName() {
		return "HybridSplitComponent";
	}

	/**
	 * The pseudo component that the next components will get as the input.
	 */
	private class OutputComponent<T> extends AnalysisComponent<T> {

		private volatile boolean done;
		private String name;

		/**
		 * Creates this output component.
		 * 
		 * @param config
		 *            The global configuration.
		 */
		public OutputComponent(@NonNull Configuration config, String name) {
			super(config);
			this.name = name;
		}

		@Override
		protected void execute() {
			// make sure that SplitComponent is started; multiple calls to start() will do
			// no harm
			HybridCacheAdapter.this.start();

			while (!done) {
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}

		public void myAddResult(T result) {
			this.addResult(result);
		}

		@Override
		public @NonNull String getResultName() {
			return this.name;
		}

	}

	public @NonNull AnalysisComponent<VariabilityModel> getVmComponent() {
		return this.vmComponent;
	}

	public @NonNull AnalysisComponent<BuildModel> getBmComponent() {
		return this.bmComponent;
	}

	public @NonNull AnalysisComponent<SourceFile> getCmComponent() {
		return this.cmComponent;
	}

}
