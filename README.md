# IncrementalAnalyses

<!-- ![Build Status](https://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=TODO) -->

Support for incremental anlyses in [KernelHaven](https://github.com/KernelHaven/KernelHaven).

*This plugin is currently under development. More information will be published here shortly.*

If you are interested in evaluation results concerning this plugin, check out [IncrementalAnalysesEvaluation](https://github.com/moritzfl/IncrementalAnalysesEvaluation). The release section contains the actual results of analysis executions on our reference system. You can expect the full results to be published there in the release section as the project progresses.

## Usage
Export the project as a jar in eclipse and place the resulting file in the plugins folder of KernelHaven.

To use the preparation, set `preparation.class.0` to `net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparation` in the KernelHaven properties.

`analysis.class` needs to be set to a class that can handle HybridCache as input.
An existing example is:

``
analysis.class = net.ssehub.kernel_haven.incremental.analysis.IncrementalDeadCodeAnalysis
``

The IncrementalDeadCodeAnalyis-example also shows you how to wrap an existing analysis so that it can accept HybridCache as input via the HybridCacheAdapter.

Additionally at least the following parameters need to be defined:

``
incremental.hybrid_cache.dir = hybrid_cache/
``

``
incremental.input.source_tree_diff = git.diff
``

For more information check out [IncrementalAnalysisSettings.java](https://github.com/KernelHaven/ModelStoragePipeline/blob/master/src/net/ssehub/kernel_haven/incremental/settings/IncrementalAnalysisSettings.java)

In addition to KernelHaven, this plugin has the following dependencies:
* [UnDeadAnalyzer](https://github.com/KernelHaven/UnDeadAnalyzer) (only for IncrementalDeadCodeAnalysis)
* [ComAn](https://github.com/KernelHaven/ComAn)

## License

This plugin is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).
