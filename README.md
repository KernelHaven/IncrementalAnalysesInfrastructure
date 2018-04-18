# IncrementalAnalyses

<!-- ![Build Status](https://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=TODO) -->

Support for incremental anlyses in [KernelHaven](https://github.com/KernelHaven/KernelHaven).

This plugin is currently under development. More information will be published here shortly.

## Usage
Place [`IncrementalAnalyses.jar`](TODO: jenkins URL) in the plugins folder of KernelHaven.

To use the preparation, set `preparation.class.0` to `net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparation` in the KernelHaven properties.

Alternatively `analysis.class` needs to be set to a class that can handle HybridCache as input.
An existing example is:
* analysis.class = net.ssehub.kernel_haven.incremental.analysis.IncrementalDeadCodeAnalysis

The IncrementalDeadCodeAnalyis-example also shows you how to wrap an existing analysis so that it can accept HybridCache as input via the HybridCacheAdapter.


In addition to KernelHaven, this plugin has the following dependencies:
* [UnDeadAnalyzer](https://github.com/KernelHaven/UnDeadAnalyzer)
* [ComAn](https://github.com/KernelHaven/ComAn)

## License

This plugin is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).
