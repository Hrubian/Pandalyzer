package analyzer

import dataframe.DataFrame

data class AnalyzerMetadata(
    val dataFrames: Map<String, DataFrame>
)
