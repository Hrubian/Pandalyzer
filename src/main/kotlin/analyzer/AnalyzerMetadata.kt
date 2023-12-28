package analyzer

import python.datastructures.DataFrame

data class AnalyzerMetadata(
    val dataFrames: Map<String, DataFrame>
)
