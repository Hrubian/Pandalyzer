package analyzer

import python.datastructures.pandas.DataFrame

data class AnalyzerMetadata(
    val dataFrames: Map<String, DataFrame>
)
