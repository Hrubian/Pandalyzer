package analyzer

import pandas.DataFrame

data class AnalyzerMetadata(
    val dataFrames: Map<String, DataFrame>
)
